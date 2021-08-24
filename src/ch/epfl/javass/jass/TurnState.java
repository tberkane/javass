package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * Represents the state of a turn
 * 
 * @author Yingxuan Duan (282512)
 *
 */
public final class TurnState {

    /**
     * Score of the current state
     */
    private final long pkScore;

    /**
     * Set of unplayed cards
     */
    private final long pkUnplayedCards;

    /**
     * Current trick
     */
    private final int pkTrick;

    /**
     * Private constructor of the class, which makes it non instantiable
     * 
     * @param pkScore
     *            (long): Score of the current state
     * @param pkUnplayedCards
     *            (long): Set of unplayed cards
     * @param pkTrick
     *            (int): current trick
     */
    private TurnState(long pkScore, long pkUnplayedCards, int pkTrick) {
        this.pkScore = pkScore;
        this.pkUnplayedCards = pkUnplayedCards;
        this.pkTrick = pkTrick;
    }

    /**
     * @param trump
     *            (Color): trump color
     * @param score
     *            (Score): current score
     * @param firstPlayer
     *            (PlayerId): the first player
     * @return (TurnState):an initial TurnState with the given trump color,
     *         score and first player
     */
    public static TurnState initial(Color trump, Score score,
            PlayerId firstPlayer) {
        return new TurnState(score.packed(), PackedCardSet.ALL_CARDS,
                PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * Creates a TurnState corresponding to the given packed components if they
     * are all valid
     * 
     * @param pkScore
     *            (long): packed score
     * @param pkUnplayedCards
     *            (long): packed card set representing unplayed cards
     * @param pkTrick
     *            (int): packed trick
     * @return (TurnState) a TurnState with the given score, unplayed cards and
     *         trick
     */
    public static TurnState ofPackedComponents(long pkScore,
            long pkUnplayedCards, int pkTrick) {
        Preconditions.checkArgument(PackedScore.isValid(pkScore)
                && PackedCardSet.isValid(pkUnplayedCards)
                && PackedTrick.isValid(pkTrick));

        return new TurnState(pkScore, pkUnplayedCards, pkTrick);
    }

    /**
     * @return (long): current score of the state in its packed version
     */
    public long packedScore() {
        return pkScore;
    }

    /**
     * @return (long): unplayed cards of the current state in its packed version
     */
    public long packedUnplayedCards() {
        return pkUnplayedCards;
    }

    /**
     * @return (int): packed trick of the current state
     */
    public int packedTrick() {
        return pkTrick;
    }

    /**
     * @return (Score): current score of the state
     */
    public Score score() {
        return Score.ofPacked(pkScore);
    }

    /**
     * @return (CardSet): unplayed cards of the current state
     */
    public CardSet unplayedCards() {
        return CardSet.ofPacked(pkUnplayedCards);
    }

    /**
     * @return (Trick): trick of the current state
     */
    public Trick trick() {
        return Trick.ofPacked(pkTrick);
    }

    /**
     * @return (boolean): true iff the state is terminal, that is if the last
     *         trick of the turn has been played and collected
     */
    public boolean isTerminal() {
        return pkTrick == PackedTrick.INVALID;
    }

    /**
     * @throws IllegalStateException:
     *             if the trick is full
     * @return (PlayerId): player who should play the next card
     */
    public PlayerId nextPlayer() {
        if (PackedTrick.isFull(pkTrick))
            throw new IllegalStateException("Trick is full");

        return PackedTrick.player(pkTrick, PackedTrick.size(pkTrick));
    }

    /**
     * @param card
     *            (Card): the new card to play
     * @throws IllegalStateException:
     *             if the trick is full
     * @return (TurnState): updated turnState with the new card played
     */
    public TurnState withNewCardPlayed(Card card) {
        if (PackedTrick.isFull(pkTrick))
            throw new IllegalStateException("Trick is full");
        Preconditions.checkArgument(unplayedCards().contains(card));

        return new TurnState(pkScore,
                PackedCardSet.remove(pkUnplayedCards, card.packed()),
                PackedTrick.withAddedCard(pkTrick, card.packed()));
    }

    /**
     * @throws IllegalStateException:
     *             if the trick is not full
     * @return (TurnState): updated turnState with the current trick collected
     */
    public TurnState withTrickCollected() {
        if (!PackedTrick.isFull(pkTrick))
            throw new IllegalStateException("Trick is not full");
        return new TurnState(
                PackedScore.withAdditionalTrick(pkScore,
                        PackedTrick.winningPlayer(pkTrick).team(),
                        PackedTrick.points(pkTrick)),
                pkUnplayedCards, PackedTrick.nextEmpty(pkTrick));
    }

    /**
     * @param card
     *            (Card): the new card to play
     * @throws IllegalStateException
     *             if the trick is already full
     * @return (TurnState):updated turnState with the new card played, then if
     *         it becomes full, with the current trick collected
     */
    public TurnState withNewCardPlayedAndTrickCollected(Card card) {
        if (PackedTrick.isFull(pkTrick))
            throw new IllegalStateException("Trick is full");

        TurnState state = withNewCardPlayed(card);
        if (PackedTrick.isFull(state.pkTrick))
            state = state.withTrickCollected();

        return state;
    }
}
