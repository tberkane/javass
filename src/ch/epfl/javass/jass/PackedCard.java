package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Rank;

/**
 * Non instantiable class with static methods for manipulating cards packed into
 * ints
 * 
 * @author Thomas Berkane (297780)
 *
 */
public final class PackedCard {

    /**
     * An invalid packed card, useful in certain cases
     */
    public static final int INVALID = 0b111111;

    private static final int RANK_START = 0, RANK_SIZE = 4;
    private static final int COLOR_START = 4, COLOR_SIZE = 2;

    /**
     * Private constructor because PackedCard is non instantiable
     */
    private PackedCard() {
    }

    /**
     * Checks if the given value is a valid packed card, that is if the bits
     * containing the rank contain a value between 0 and 8, and if the unused
     * bits are 0
     * 
     * @param pkCard
     *            (int): the packed card whose validity to check
     * @return (boolean): whether the packed card is valid
     */
    public static boolean isValid(int pkCard) {
        int rank = Bits32.extract(pkCard, RANK_START, RANK_SIZE);
        return rank >= 0 && rank < Rank.COUNT
                && (pkCard >>> RANK_SIZE + COLOR_SIZE) == 0;
    }

    /**
     * Packs a card's color and rank into an int
     * 
     * @param c
     *            (Card.Color): card's color
     * @param r
     *            (Card.Rank): card's rank
     * @return (int): The packed card
     */
    public static int pack(Card.Color c, Card.Rank r) {
        // Packs the card's rank's ordinal on 4 bits and its color's ordinal on
        // 2 bits
        return Bits32.pack(r.ordinal(), RANK_SIZE, c.ordinal(), COLOR_SIZE);
    }

    /**
     * Returns the color of the given packed card
     * 
     * @param pkCard
     *            (int): the packed card whose color to extract
     * @return (Card.Color): the card's color
     */
    public static Card.Color color(int pkCard) {
        // Checks if the card is valid
        assert (isValid(pkCard));
        return Card.Color.ALL
                .get(Bits32.extract(pkCard, COLOR_START, COLOR_SIZE));

    }

    /**
     * Returns the rank of the given packed card
     * 
     * @param pkCard
     *            (int): the packed card whose rank to extract
     * @return (Card.Rank): the card's rank
     */
    public static Card.Rank rank(int pkCard) {
        // Checks if the card is valid
        assert (isValid(pkCard));
        return Card.Rank.ALL.get(Bits32.extract(pkCard, RANK_START, RANK_SIZE));
    }

    /**
     * Compares two cards, based on their rank and color
     * 
     * @param trump
     *            (Card.Color): the trump color
     * @param pkCardL
     *            (int): first packed card
     * @param pkCardR
     *            (int): second packed card
     * @return (boolean): true iff the first given card is superior to the
     *         second, knowing trump; returns false if the cards are not
     *         compatible
     */
    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        // Checks if the cards are valid
        assert (isValid(pkCardL));
        assert (isValid(pkCardR));
        // 1st case: the 2 cards have the same color
        if (color(pkCardL).equals(color(pkCardR))) {
            if (color(pkCardL).equals(trump)) {
                // Subcase 1: the color is trump -> we use
                // trumpOrdinals
                return rank(pkCardL).trumpOrdinal() > rank(pkCardR)
                        .trumpOrdinal();
            }
            // Subcase 2: the color is not trump -> we directly extract
            // and compare their ranks
            return Bits32.extract(pkCardL, RANK_START, RANK_SIZE) > Bits32
                    .extract(pkCardR, RANK_START, RANK_SIZE);
        }
        // 2nd case: the two cards have different colors
        if (color(pkCardL).equals(trump) || color(pkCardR).equals(trump)) {
            // Subcase 1: one of the cards is trump -> it automatically wins
            return color(pkCardL).equals(trump);
        }
        // Subcase 2: none of the cards is trump -> they can't be compared
        return false;
    }

    /**
     * Gives how many points a card is worth, knowing what the trump color is
     * 
     * @param trump
     *            (Card.Color): the trump color
     * @param pkCard
     *            (int): the packed card
     * @return (int): the points value of the packed card
     */
    public static int points(Card.Color trump, int pkCard) {
        // Checks if the card is valid
        assert (isValid(pkCard));
        // 1st option: the card's color is trump
        if (color(pkCard).equals(trump)) {
            switch (rank(pkCard)) {
            case NINE:
                return 14;
            case TEN:
                return 10;
            case JACK:
                return 20;
            case QUEEN:
                return 3;
            case KING:
                return 4;
            case ACE:
                return 11;
            // SIX, SEVEN AND EIGHT are worth 0 points
            default:
                return 0;
            }
            // 2nd option: the card's color is not trump
        } else {
            switch (rank(pkCard)) {
            case TEN:
                return 10;
            case JACK:
                return 2;
            case QUEEN:
                return 3;
            case KING:
                return 4;
            case ACE:
                return 11;
            // SIX, SEVEN, EIGHT and NINE are worth 0 points
            default:
                return 0;
            }
        }
    }

    /**
     * @param pkCard
     *            (int): the packed card
     * @return (String): a representation of the packed card containing the
     *         color's symbol and the shortened name of the rank
     */
    public static String toString(int pkCard) {
        // Checks if the card is valid
        assert (isValid(pkCard));
        return color(pkCard).toString() + rank(pkCard).toString();
    }
}
