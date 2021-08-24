package ch.epfl.javass.jass;

import java.util.List;

import ch.epfl.javass.Preconditions;

/**
 * An immutable set of cards
 * 
 * @author Thomas Berkane (297780)
 */
public final class CardSet {

    /**
     * Empty card set
     */
    public static final CardSet EMPTY = new CardSet(PackedCardSet.EMPTY);

    /**
     * Card set containing all cards
     */
    public static final CardSet ALL_CARDS = new CardSet(
            PackedCardSet.ALL_CARDS);

    /*
     * The long representing the packed version of the card set
     */
    private final long packedRepresentation;

    /**
     * Private constructor, static methods are used instead as constructors
     * 
     * @param packedRepresentation
     *            (long): represents the packed version of the card set
     */
    private CardSet(long packedRepresentation) {
        this.packedRepresentation = packedRepresentation;
    }

    /**
     * Constructs a new CardSet from a list of cards
     * 
     * @param cards
     *            (List<Card>): list of cards contained in the set
     * @return (CardSet): composed of all the cards in the list
     */
    public static CardSet of(List<Card> cards) {
        long packedCardSet = PackedCardSet.EMPTY;
        for (Card card : cards) {
            packedCardSet = PackedCardSet.add(packedCardSet, card.packed());
        }
        return new CardSet(packedCardSet);
    }

    /**
     * Constructs a new CardSet from a packed card set
     * 
     * @param packed
     *            (long): the packed version of the card set
     * @return (CardSet): composed of all the cards in packed
     */
    public static CardSet ofPacked(long packed) {
        Preconditions.checkArgument(PackedCardSet.isValid(packed));
        return new CardSet(packed);
    }

    /**
     * @return (long): the packed representation of the card set
     */
    public long packed() {
        return packedRepresentation;
    }

    /**
     * @return (boolean): whether the card set is empty
     */
    public boolean isEmpty() {
        return PackedCardSet.isEmpty(packedRepresentation);
    }

    /**
     * @return (int): number of elements in the card set
     */
    public int size() {
        return PackedCardSet.size(packedRepresentation);
    }

    /**
     * @param index
     *            (int): the index of the card to get from the card set
     * @return (Card): the card at index index in the card set
     */
    public Card get(int index) {
        Preconditions.checkIndex(index, size());
        return Card.ofPacked(PackedCardSet.get(packedRepresentation, index));
    }

    /**
     * @param card
     *            (Card): the card to add
     * @return (CardSet): the CardSet to which card has been added
     */
    public CardSet add(Card card) {
        return ofPacked(PackedCardSet.add(packedRepresentation, card.packed()));
    }

    /**
     * @param card
     *            (Card): the card to remove
     * @return (CardSet): the CardSet from which card has been removed
     */
    public CardSet remove(Card card) {
        return ofPacked(
                PackedCardSet.remove(packedRepresentation, card.packed()));

    }

    /**
     * @param card
     *            (Card): the card we want to know whether this contains
     * @return (boolean): whether this contains card
     */
    public boolean contains(Card card) {
        return PackedCardSet.contains(packedRepresentation, card.packed());
    }

    /**
     * @return (CardSet): the card set composed of all the cards which are not
     *         in this
     */
    public CardSet complement() {
        return ofPacked(PackedCardSet.complement(packedRepresentation));
    }

    /**
     * @param that
     *            (CardSet): other card set
     * @return (CardSet): the card set composed of all the cards which are
     *         either in this or (inclusive) in that
     */
    public CardSet union(CardSet that) {
        return ofPacked(
                PackedCardSet.union(packedRepresentation, that.packed()));
    }

    /**
     * @param that
     *            (CardSet): other card set
     * @return (CardSet): the card set composed of all the cards which are both
     *         in this and in that
     */
    public CardSet intersection(CardSet that) {
        return ofPacked(PackedCardSet.intersection(packedRepresentation,
                that.packed()));
    }

    /**
     * @param that
     *            (CardSet): other card set
     * @return (CardSet): the card set composed of the cards which are in this
     *         but not in that
     */
    public CardSet difference(CardSet that) {
        return ofPacked(
                PackedCardSet.difference(packedRepresentation, that.packed()));
    }

    /**
     * @param color
     *            (Color): the color we want to get the subset of
     * @return (CardSet): card set composed of cards from this whose color is
     *         color
     */
    public CardSet subsetOfColor(Card.Color color) {
        return ofPacked(
                PackedCardSet.subsetOfColor(packedRepresentation, color));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Long.hashCode(packedRepresentation);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardSet other = (CardSet) obj;
        return (packedRepresentation == other.packedRepresentation);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return PackedCardSet.toString(packedRepresentation);

    }

}
