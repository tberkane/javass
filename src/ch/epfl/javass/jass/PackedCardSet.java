package ch.epfl.javass.jass;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits64;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

/**
 * Contains methods for manipulating sets of cards packed in longs
 * 
 * @author Yingxuan Duan (282512)
 */
public final class PackedCardSet {

    /**
     * Empty card set
     */
    public static final long EMPTY = 0L;

    /**
     * Card set containing all cards
     */
    public static final long ALL_CARDS = 0b0000000_111111111_0000000_111111111_0000000_111111111_0000000_111111111L;

    private static final int UNUSED_BITS_START = 9, UNUSED_BITS_SIZE = 7;
    private static final int COLOR_SUBSET_SIZE = Long.SIZE / Color.COUNT;

    /*
     * Array containing, for each card, at position [color][rank], the set of
     * cards which are above it, given that the card is trump
     */
    private static final long[][] TRUMP_ABOVE = computeTrumpAbove();

    /*
     * Array containing, for each color, at position [color], the set of cards
     * of that color
     */
    private static final long[] SUBSET_OF_COLOR = computeSubsetOfColor();

    /*
     * Private constructor because PackedCardSet is not instantiable
     */
    private PackedCardSet() {
    }

    /**
     * Checks if the given value is a valid packed card set, that is if the
     * unused 28 bits are 0
     * 
     * @param pkCardSet
     *            (long): the packed card set for which we check the validity
     * @return (boolean): it is true if and only if the set is valid
     */
    public static boolean isValid(long pkCardSet) {
        return (pkCardSet | ALL_CARDS) == ALL_CARDS;
    }

    /**
     * @param pkCard
     *            (int): the given packed card whose above cards we want to find
     * @return (long): the set of cards above pkCard, given that it is a trump
     *         card
     */
    public static long trumpAbove(int pkCard) {
        // Checks that pkCard is a valid card
        assert (PackedCard.isValid(pkCard));
        return TRUMP_ABOVE[PackedCard.color(pkCard).ordinal()][PackedCard
                .rank(pkCard).ordinal()];

    }

    /**
     * Returns a packed card set which contains only the given packed card
     * 
     * @param pkCard
     *            (int): an integer that represents the packed card
     * @return (long): packed card set containing only pkCard
     */
    public static long singleton(int pkCard) {
        assert (PackedCard.isValid(pkCard));

        return Bits64.mask(pkCard, 1);
    }

    /**
     * @param pkCardSet
     *            (long): the set whose emptiness we want to check
     * @return (boolean): whether pkCardSet is empty
     */
    public static boolean isEmpty(long pkCardSet) {
        // Checks that pkCardSet is a valid card set
        assert (isValid(pkCardSet));
        return pkCardSet == EMPTY;
    }

    /**
     * Returns the number of cards that packed card set contains
     * 
     * @param pkCardSet
     *            (long): the packed card set whose size we want to know
     * @return (int): size of the packed card set
     */
    public static int size(long pkCardSet) {
        assert (isValid(pkCardSet));

        return Long.bitCount(pkCardSet);
    }

    /**
     * Gets the index'th card from pkCardSet
     * 
     * @param pkCardSet
     *            (long): the card set from which to extract the card
     * @param index
     *            (int): the index of the card in pkCardSet
     * @return (int): the card at index in pkCardSet
     */
    public static int get(long pkCardSet, int index) {
        assert (isValid(pkCardSet));
        // Checks that the index is valid
        assert (index >= 0 && index < size(pkCardSet));

        // Replaces the index rightmost 1s in pkCardSet by 0s
        for (int i = 0; i < index; i++) {
            pkCardSet &= ~Long.lowestOneBit(pkCardSet);
        }
        // Returns the number of 0s after the rightmost 1, that is the index of
        // the last 1 and thus the index'th card
        return Long.numberOfTrailingZeros(pkCardSet);

    }

    /**
     * Adds a given card to a packed card set, if the set already contains that
     * card, does nothing
     * 
     * @param pkCardSet
     *            (long): the set in which we want to add a card
     * @param pkCard
     *            (int): the card to be added in its integer representation
     * @return (long): the given packed card set with the given card added in it
     */
    public static long add(long pkCardSet, int pkCard) {
        assert (isValid(pkCardSet));
        assert (PackedCard.isValid(pkCard));

        return pkCardSet | singleton(pkCard);
    }

    /**
     * Removes a card from a packed card set
     * 
     * @param pkCardSet
     *            (long): the packed card set from which to remove
     * @param pkCard
     *            (int): the card to remove
     * @return (long): the packed card set from which pkCard has been removed
     */
    public static long remove(long pkCardSet, int pkCard) {
        assert (isValid(pkCardSet));
        assert (PackedCard.isValid(pkCard));

        return pkCardSet & ~singleton(pkCard);
    }

    /**
     * Tells whether the given card set contains a certain card
     * 
     * @param pkCardSet
     *            (long): the packed card set to be checked
     * @param pkCard
     *            (int): the card whose existence in packed card set is to be
     *            checked
     * @return (boolean): true if and only if the given set contains the card
     */
    public static boolean contains(long pkCardSet, int pkCard) {
        assert (isValid(pkCardSet));
        assert (PackedCard.isValid(pkCard));

        long singleton = singleton(pkCard);
        return (pkCardSet & singleton) == singleton;
    }

    /**
     * Returns the packed card set composed of all the cards which are not in
     * pkCardSet
     * 
     * @param pkCardSet
     *            (long): the packed card set
     * @return (long): the complement of pkCardSet
     */
    public static long complement(long pkCardSet) {
        assert (isValid(pkCardSet));

        return ~pkCardSet & ALL_CARDS;
    }

    /**
     * Returns the packed card set composed of the cards which are in pkCardSet1
     * or (inclusive) in pkCardSet2
     * 
     * @param pkCardSet1
     *            (long): 1st packed card set
     * @param pkCardSet2
     *            (long): 2nd packed card set
     * @return (long): the union of pkCardSet1 and pkCardSet2
     */
    public static long union(long pkCardSet1, long pkCardSet2) {
        assert (isValid(pkCardSet1));
        assert (isValid(pkCardSet2));

        return pkCardSet1 | pkCardSet2;
    }

    /**
     * Returns the packed card set composed of the cards which are both in
     * pkCardSet1 and in pkCardSet2
     * 
     * @param pkCardSet1
     *            (long): 1st packed card set
     * @param pkCardSet2
     *            (long): 2nd packed card set
     * @return (long): the intersection of pkCardSet1 and pkCardSet2
     */
    public static long intersection(long pkCardSet1, long pkCardSet2) {
        assert (isValid(pkCardSet1));
        assert (isValid(pkCardSet2));

        return pkCardSet1 & pkCardSet2;
    }

    /**
     * Returns the packed card set composed of the cards which are in the first
     * set but not the second set
     * 
     * @param pkCardSet1
     *            (long): 1st packed card set
     * @param pkCardSet2
     *            (long): 2nd packed card set
     * @return (long): the difference of pkCardSet1 and pkCardSet2
     */
    public static long difference(long pkCardSet1, long pkCardSet2) {
        assert (isValid(pkCardSet1));
        assert (isValid(pkCardSet2));

        return (pkCardSet1 ^ pkCardSet2) & pkCardSet1;
    }

    /**
     * @param pkCardSet
     *            (long): the packed card set
     * @param color
     *            (Color): the color of the cards we want to retain in the set
     * @return (long): the subset of pkCardSet composed of the cards whose color
     *         is color
     */
    public static long subsetOfColor(long pkCardSet, Card.Color color) {
        assert (isValid(pkCardSet));
        return pkCardSet & SUBSET_OF_COLOR[color.ordinal()];
    }

    /**
     * Returns a string representation of the packed card set, which consists of
     * a chain formed by all cards contained in the set, in the order of their
     * location in pkCardset
     * 
     * @param pkCardSet
     *            (long): the packed card set whose textual representation we
     *            want to know
     * @return (String): curly braces containing all cards of pkCardSet
     *         represented by the symbol of their color and the compact
     *         representation of rank
     */
    public static String toString(long pkCardSet) {
        assert (isValid(pkCardSet));
        StringJoiner j = new StringJoiner(",", "{", "}");

        for (int i = 0; i < size(pkCardSet); i++) {
            j.add(PackedCard.toString(get(pkCardSet, i)));
        }
        return j.toString();
    }

    /*
     * Fills TRUMP_ABOVE
     * 
     * @return (long[][]): two-dimensional array containing all cards above each
     * card, first index is ordinal of color, second index is ordinal of rank
     */
    private static long[][] computeTrumpAbove() {
        // Initializes two-dimensional array for the card sets
        long[][] trumpAboveArray = new long[Color.COUNT][Rank.COUNT];
        // Iterates over all cards
        for (Color color : Color.ALL) {
            for (Rank rank : Rank.ALL) {
                // The set of cards above
                long cardsAbove = EMPTY;
                // The card we will compare to all the others
                int card = PackedCard.pack(color, rank);
                // Iterates over all cards to compare them to card
                for (Rank r : Rank.ALL) {
                    int otherCard = PackedCard.pack(color, r);
                    if (PackedCard.isBetter(color, otherCard, card))
                        cardsAbove = PackedCardSet.add(cardsAbove, otherCard);
                }
                trumpAboveArray[color.ordinal()][rank.ordinal()] = cardsAbove;
            }
        }
        return trumpAboveArray;
    }

    /*
     * @return (long[]): array containing for each color, at position [color],
     * the set of cards of that color
     */
    private static long[] computeSubsetOfColor() {
        long[] colorSubsetArray = new long[Color.COUNT];
        long subset = 0b111111111L;
        for (int i = 0; i < Color.COUNT; i++) {
            // Shifting the subset appropriately for each color
            colorSubsetArray[i] = subset << (COLOR_SUBSET_SIZE * i);
        }
        return colorSubsetArray;
    }
}
