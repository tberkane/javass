package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import ch.epfl.javass.Preconditions;

/**
 * A simulated player using the MCTS to decide which card to play
 * 
 * @author Yingxuan Duan (282512)
 *
 */
public final class MctsPlayer implements Player {

    /*
     * Id of this MctsPlayer
     */
    private final PlayerId ownId;
    /*
     * Total number of turns we aim to simulate
     */
    private final int iterations;
    /*
     * Splittable RNG used to finish the turn randomly
     */
    private final SplittableRandom rng;

    private final static int EMPIRICAL_CONSTANT = 40;

    /**
     * Creates a new MctsPlayer
     * 
     * @param ownId
     *            (PlayerId): Id of the simulated player
     * @param rngSeed
     *            (SplittableRandom): Splittable RNG
     * @param iterations
     *            (int): total number of turns we aim to simulate
     */
    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {

        Preconditions.checkArgument(iterations >= Jass.HAND_SIZE);

        this.ownId = ownId;
        this.rng = new SplittableRandom(rngSeed);
        this.iterations = iterations;
    }

    /*
     * @param pId (PlayerId): id of player for whom we want to determine the
     * playable cards
     * 
     * @param pkHand (long): packed hand
     * 
     * @param state (TurnState): current state of the turn
     * 
     * @return (long): packed version of the set of playable cards for given
     * player id, state, and hand of this player
     */
    private long playableCardsforState(PlayerId pId, long pkHand,
            TurnState state) {
        // Making a copy of the state so that we don't modify it;
        TurnState stateCopy = state;
        int trick = stateCopy.packedTrick();
        long unplayed = stateCopy.packedUnplayedCards();

        if (PackedTrick.isFull(trick))
            stateCopy = stateCopy.withTrickCollected();
        if (stateCopy.isTerminal())
            return PackedCardSet.EMPTY;

        // If we want the playable cards for this MctsPlayer, we take the
        // intersection of its hand with set of unplayed cards
        if (pId == ownId)
            return PackedTrick.playableCards(trick,
                    PackedCardSet.intersection(pkHand, unplayed));

        // If we want playable cards for other players, we take playable
        // cards from unplayed set excluding hand of this simulated
        // player
        return PackedTrick.playableCards(trick,
                PackedCardSet.difference(unplayed, pkHand));
    }

    /*
     * Adds when possible a new node at the necessary place in the tree, returns
     * the path from the root (excluded) to the added node
     * 
     * @param root (Node): the root of the tree
     * 
     * @param pkHand (long): the packed hand of cards of this simulated player
     * 
     * @return (List<Node>): a list of nodes forming the path from the root to
     * the node
     */
    private List<Node> findPath(Node root, long pkHand) {

        Node currentNode = root;
        int best = 0;
        List<Node> path = new ArrayList<Node>();

        // while current node has all its children and bottom of the tree has
        // not been reached
        while (PackedCardSet.isEmpty(currentNode.inexistentChildNodes)
                && currentNode.childNodes.length > 0) {
            // index of the child we want to explore
            best = currentNode.bestChild(EMPIRICAL_CONSTANT);
            // changes currentNode to a node further down the tree
            currentNode = currentNode.childNodes[best];
            path.add(currentNode);
        }

        // turn state of the node we are going to add to the tree
        TurnState newState = currentNode.turnState;
        if (PackedTrick.isFull(newState.packedTrick()))
            newState = newState.withTrickCollected();

        // if we have reached the bottom of the tree, then return the path
        // directly
        if (currentNode.childNodes.length == 0)
            return path;

        // newState has a new card played in it
        newState = newState.withNewCardPlayed(Card.ofPacked(
                PackedCardSet.get(currentNode.inexistentChildNodes, 0)));

        // who plays next depends on whether the trick is over
        PlayerId nextPlayer = PackedTrick.isFull(newState.packedTrick())
                ? PackedTrick.winningPlayer(newState.packedTrick())
                : newState.nextPlayer();

        // set of playable cards for the next state
        long newPlayableCards = playableCardsforState(nextPlayer, pkHand,
                newState);

        // number of children the current node already has, used to know at
        // which position to create the new child node
        int count = currentNode.childNodes.length
                - PackedCardSet.size(currentNode.inexistentChildNodes);

        // creates the new node as a child of the current node
        currentNode.childNodes[count] = new Node(newState, newPlayableCards);

        // updates the current node's inexistantChildNodes
        currentNode.inexistentChildNodes = PackedCardSet.remove(
                currentNode.inexistentChildNodes,
                PackedCardSet.get(currentNode.inexistentChildNodes, 0));

        // adds the new node to the path
        path.add(currentNode.childNodes[count]);

        return path;
    }

    /*
     * Finishes a turn randomly and returns the final score
     * 
     * @param turnState (TurnState): the state from which we play randomly to
     * finish
     * 
     * @param pkHand (long): this player's hand
     * 
     * @return (long): final score of a randomly finished turn from given turn
     * state
     */
    private long finishTurn(TurnState turnState, long pkHand) {

        if (PackedTrick.isFull(turnState.packedTrick())
                && !turnState.isTerminal())
            turnState = turnState.withTrickCollected();

        // while the turn is not over, each player plays random cards
        while (!turnState.isTerminal()) {
            // cards the player whose turn it is can play
            long playableCards = playableCardsforState(turnState.nextPlayer(),
                    pkHand, turnState);
            // card it chooses
            int card = PackedCardSet.get(playableCards,
                    rng.nextInt(PackedCardSet.size(playableCards)));
            // adds card to state
            turnState = turnState
                    .withNewCardPlayedAndTrickCollected(Card.ofPacked(card));
        }
        return turnState.packedScore();
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long pkHand = hand.packed();
        // representing last card played before it is simulated player's turn
        Node root = new Node(state,
                playableCardsforState(state.nextPlayer(), pkHand, state));
        List<Node> path;

        // simulates the desired number of turns and distributes corresponding
        // points
        while (root.totalTurns < iterations) {
            path = findPath(root, pkHand);
            long finalScore = finishTurn(path.get(path.size() - 1).turnState,
                    pkHand);
            root.totalTurns++;
            for (Node node : path) {
                // update total turns simulated and propagate the points for
                // each node on the path
                node.totalTurns++;
                int trick = node.turnState.packedTrick();
                node.totalPoints += PackedScore.totalPoints(finalScore,
                        PackedTrick.player(trick, PackedTrick.size(trick) - 1)
                                .team());
            }
        }

        Trick trick = root.childNodes[root.bestChild(0)].turnState.trick();
        // return the optimal card to play thanks to the calculation of best
        // child
        return trick.card(trick.size() - 1);
    }

    /**
     * A node of the tree
     *
     */
    private static final class Node {
        /**
         * TurnState corresponding to the node
         */
        private final TurnState turnState;
        /**
         * array containing all the node's children
         */
        private final Node[] childNodes;
        /**
         * the set of cards corresponding to the node's inexistent child nodes
         */
        private long inexistentChildNodes;
        /**
         * total points gained by the team who played the last card of the
         * state's trick
         */
        private int totalPoints;
        /**
         * total number of turns simulated using this node
         */
        private int totalTurns;

        /**
         * Creates a new node
         * 
         * @param turnState
         *            (TurnState): TurnState corresponding to the node
         * @param playableCards
         *            (long): set of playable cards for the current state,
         *            corresponding to all possible children of the node
         */
        private Node(TurnState turnState, long playableCards) {
            this.turnState = turnState;
            this.childNodes = new Node[PackedCardSet.size(playableCards)];
            this.inexistentChildNodes = playableCards;
            this.totalPoints = 0;
            this.totalTurns = 0;
        }

        /*
         * Calculates the index of the best child node, i.e. the most
         * interesting one to explore among its siblings
         * 
         * @param c (int): constant which determines the importance of the fact
         * that one child node is less explored in the choice of best child
         * 
         * @return (int): index of the best child node
         */
        private int bestChild(int c) {
            // value we will calculate for each node using formula
            double value;
            double maxValue = 0;
            // index of child with maxValue
            int maxIndex = 0;
            double parentLog = 2 * Math.log(totalTurns);

            for (int i = 0; i < childNodes.length; i++) {
                Node child = childNodes[i];
                int childTotalTurns = child.totalTurns;

                if (childTotalTurns <= 0)
                    return i;

                value = ((double) child.totalPoints / childTotalTurns)
                        + c * Math.sqrt(parentLog / childTotalTurns);

                if (value > maxValue) {
                    maxValue = value;
                    maxIndex = i;
                }
            }
            return maxIndex;
        }
    }
}