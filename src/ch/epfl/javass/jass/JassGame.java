package ch.epfl.javass.jass;

import static ch.epfl.javass.jass.PlayerId.ALL;
import static ch.epfl.javass.jass.TeamId.TEAM_1;
import static ch.epfl.javass.jass.TeamId.TEAM_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.jass.Card.Color;

/**
 * A game of Jass
 * 
 * @author Thomas Berkane (297780)
 *
 */
public final class JassGame {
    /**
     * RNG used for shuffling the cards
     */
    private final Random shuffleRng;
    /**
     * RNG used for selecting the trump color
     */
    private final Random trumpRng;
    /**
     * Associates each player identity to a specific player
     */
    private final Map<PlayerId, Player> players;
    /**
     * Gives each player a name
     */
    private final Map<PlayerId, String> playerNames;
    /**
     * Stores each player's hand
     */
    private Map<PlayerId, CardSet> hands;
    /**
     * The player which plays first this turn
     */
    private PlayerId firstPlayer;
    /**
     * The state of the current turn of the game
     */
    private TurnState turnState;
    /**
     * Whether the game is over
     */
    private boolean gameOver;
    
    private static final Card SEVEN_DIAMOND = Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN);

    /**
     * Creates a new Jass game
     * 
     * @param rngSeed
     *            (long): used to initialize shuffleRng and trumpRng
     * @param players
     *            (Map<PlayerId, Player>): Associates each player identity to a
     *            specific player
     * @param playerNames
     *            (Map<PlayerId, String>): Gives each player a name
     */
    public JassGame(long rngSeed, Map<PlayerId, Player> players,
            Map<PlayerId, String> playerNames) {

        // players and playerNames are immutable
        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.playerNames = Collections
                .unmodifiableMap(new EnumMap<>(playerNames));

        Random rng = new Random(rngSeed);
        // rng is used to initialize the two other RNGs
        this.shuffleRng = new Random(rng.nextLong());
        this.trumpRng = new Random(rng.nextLong());

        hands = new EnumMap<PlayerId, CardSet>(PlayerId.class);
        // Initially, the players' hands are empty
        for (PlayerId id : ALL) {
            hands.put(id, CardSet.EMPTY);
        }

    }

    /**
     * @return (boolean): whether the game is over
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Advances the state of the game to the end of the next trick, or does
     * nothing if the game is over
     */
    public void advanceToEndOfNextTrick() {
        if (!gameOver) {
            // If this is not the very first trick of the game, collect the
            // trick
            if (turnState != null)
                turnState = turnState.withTrickCollected();

            // If this is the very first trick or if the previous turn is over,
            // start a new turn
            if (turnState == null || turnState.isTerminal())
                beginTurn();

            // Updates each player with the current scores
            for (PlayerId id : ALL)
                players.get(id).updateScore(turnState.score());

            // Checks if a team has won yet
            TeamId winningTeam;
            if (turnState.score()
                    .totalPoints(winningTeam = TEAM_1) >= Jass.WINNING_POINTS
                    || turnState.score().totalPoints(
                            winningTeam = TEAM_2) >= Jass.WINNING_POINTS) {
                gameOver = true;

                // Updates each player with the winning team
                for (PlayerId id : ALL)
                    players.get(id).setWinningTeam(winningTeam);
                return;
            }

            for (PlayerId id : ALL)
                players.get(id).updateTrick(turnState.trick());
            // Makes each player play
            for (int i = 0; i < PlayerId.COUNT; i++) {
                play(turnState.nextPlayer());
                for (PlayerId id : ALL)
                    players.get(id).updateTrick(turnState.trick());
            }
        }
    }

    /*
     * Shuffles the deck and deals 9 cards to each player
     */
    private void shuffleAndDeal() {
        List<Card> deck = new ArrayList<>();
        // Adds all cards to the deck
        for (int i = 0; i < CardSet.ALL_CARDS.size(); i++) {
            deck.add(CardSet.ALL_CARDS.get(i));
        }

        // Shuffles the deck
        Collections.shuffle(deck, shuffleRng);

        List<Card> cards;
        // Gives the 1st player the 1st quarter of the deck, the 2nd player the
        // 2nd quarter, etc.
        for (PlayerId id : ALL) {
            // Sublist containing a quarter of the deck's cards
            cards = deck.subList(id.ordinal() * Jass.HAND_SIZE,
                    Jass.HAND_SIZE + id.ordinal() * Jass.HAND_SIZE);
            hands.put(id, CardSet.of(cards));
        }
    }

    /*
     * Starts a new turn
     */
    private void beginTurn() {
        // Selects trump randomly
        Color trump = Color.ALL.get(trumpRng.nextInt(Color.COUNT));
        Score score;

        // Shuffles and deals cards
        shuffleAndDeal();

        // If this is the first turn
        if (turnState == null) {
            for (PlayerId id : ALL) {
                // Initialize players
                players.get(id).setPlayers(id, playerNames);
                // Find first player (seven of diamond)
                if (hands.get(id)
                        .contains(SEVEN_DIAMOND))
                    firstPlayer = id;
            }
            // Initialize score
            score = Score.INITIAL;
            // If this is not the first turn
        } else {
            // first player is the one after the previous first player
            firstPlayer = ALL.get((firstPlayer.ordinal() + 1) % PlayerId.COUNT);
            score = turnState.score().nextTurn();
        }

        turnState = TurnState.initial(trump, score, firstPlayer);

        // Notifies the players with their hands and the trump
        for (PlayerId id : ALL) {
            Player player = players.get(id);
            player.updateHand(hands.get(id));
            player.setTrump(trump);
        }
    }

    /*
     * Makes a player play a card
     * 
     * @param playerId (PlayerId): the player to make play
     */
    private void play(PlayerId playerId) {
        Player player = players.get(playerId);
        CardSet hand = hands.get(playerId);
        // Player chooses a card
        Card chosenCard = player.cardToPlay(turnState, hand);
        // Plays card
        turnState = turnState.withNewCardPlayed(chosenCard);
        // Removes card from hand
        hands.put(playerId, hand.remove(chosenCard));
        // Notifies player of new hand
        player.updateHand(hands.get(playerId));
    }
}