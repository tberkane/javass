package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * @author Yingxuan Duan (282512)
 *
 */
public final class Trick {

    /**
     * An invalid trick whose packed representation is 1 in every bit
     */
    public final static Trick INVALID = new Trick(PackedTrick.INVALID);

    /*
     * packed representation of this trick
     */
    private final int packedT;

    /**
     * private constructor of the class, which makes it non instantiable
     * 
     * @param packedT
     *            (int): packed representation of the trick we want to construct
     */
    private Trick(int packedT) {
        this.packedT = packedT;
    }

    /**
     * @param packedT
     *            (int): packed representation of the trick we want to construct
     * @return (Trick): trick corresponding to the given packed representation
     */
    public static Trick ofPacked(int packedT) {
        Preconditions.checkArgument(PackedTrick.isValid(packedT));
        return new Trick(packedT);
    }

    /**
     * @param trump
     *            (Color): the trump color for the trick
     * @param firstPlayer
     *            (PlayerId): the player who will play first in the trick
     * @return (Trick): an empty trick with index 0 and given trump color and
     *         first player
     */
    public static Trick firstEmpty(Color trump, PlayerId firstPlayer) {
        return ofPacked(PackedTrick.firstEmpty(trump, firstPlayer));
    }
    
    /**
     * @return (int): packed representation of this trick
     * 
     */
    public int packed() {
        return packedT;
    }

    /**
     * Returns the empty next trick, or INVALID if it is the last trick of the
     * turn
     * 
     * @throws IllegalStateException
     *             if the trick is not full
     * @return (Trick): an empty trick with the same trump as packedT, the next
     *         index, and the first player is the winner of packedT
     */
    public Trick nextEmpty() {
        if (!isFull()) {
            throw new IllegalStateException();
        }
        return new Trick(PackedTrick.nextEmpty(packedT));

    }

    /**
     * @return (boolean): whether packedT contains no cards
     */
    public boolean isEmpty() {
        return PackedTrick.isEmpty(packedT);
    }

    /**
     * @return (boolean): whether packedT contains 4 cards
     */
    public boolean isFull() {
        return PackedTrick.isFull(packedT);
    }

    /**
     * @return (boolean): whether the index of packedT is 8
     */
    public boolean isLast() {
        return PackedTrick.isLast(packedT);
    }

    /**
     * @return (int): the number of cards in packedT
     */
    public int size() {
        return PackedTrick.size(packedT);
    }

    /**
     * @return (Color): packedT's trump color
     */
    public Color trump() {
        return PackedTrick.trump(packedT);
    }

    /**
     * @return (int): packedT's index
     */
    public int index() {
        return PackedTrick.index(packedT);
    }

    /**
     * @param index
     *            (int): index of the player in the trick
     * @return (PlayerId): the player of given index in the trick, given that
     *         the player at index 0 is the first
     */
    public PlayerId player(int index) {
        Preconditions.checkIndex(index, PlayerId.COUNT);
        return PackedTrick.player(packedT, index);
    }

    /**
     * @param index
     *            (int): the index of the card in the trick
     * @return (Card): the index'th card played
     */
    public Card card(int index) {
        Preconditions.checkIndex(index, size());
        return Card.ofPacked(PackedTrick.card(packedT, index));
    }

    /**
     * Checks if the trick is already full, if not returns the trick with the
     * given card added in it
     * 
     * @param c
     *            (Card):the card which we want to add to the trick
     * 
     * @return (Trick): trick with Card in it
     * @throws IllegalStateException:
     *             if the trick is already full
     */
    public Trick withAddedCard(Card c) {
        if (isFull()) {
            throw new IllegalStateException();
        }
        return ofPacked(PackedTrick.withAddedCard(packedT, c.packed()));
    }

    /**
     * @return (Color): the base color, the color of first card played in
     *         packedT
     * @throws IllegalStateException:
     *             if the trick is still empty
     */
    public Color baseColor() {
        if (isEmpty()) {
            throw new IllegalStateException();
        }
        return PackedTrick.baseColor(packedT);
    }

    /**
     * Returns a subset of hand, containing cards which can be played as next
     * card of the trick, following the rules of Jass
     * 
     * @param hand
     *            (CardSet): a card set representing a hand of cards
     * 
     * @return (CardSet): a subset of pkHand, in which all cards are playable
     * @throws IllegalStateException:
     *             if the trick is already full
     */
    public CardSet playableCards(CardSet hand) {
        if (isFull()) {
            throw new IllegalStateException();
        }
        return CardSet
                .ofPacked(PackedTrick.playableCards(packedT, hand.packed()));
    }

    /**
     * @return (int): value of the trick, including the 5 extra-points when it
     *         is the last trick of a turn
     */
    public int points() {
        return PackedTrick.points(packedT);
    }

    /**
     * @return (PlayerId): Id of the player winning the given trick
     * @throws IllegalStateException:
     *             if the trick is empty
     * 
     */
    public PlayerId winningPlayer() {
        if (isEmpty()) {
            throw new IllegalStateException();
        }
        return PackedTrick.winningPlayer(packedT);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        Trick other = (Trick) obj;
        return packedT == other.packedT;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return packedT;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return PackedTrick.toString(packedT);
    }

}