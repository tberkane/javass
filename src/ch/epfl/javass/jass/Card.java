package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javass.Preconditions;

/**
 * A card from a deck of 36 cards
 * 
 * @author Thomas Berkane (297780)
 */
public final class Card {

    /**
     * Packed version of the card
     */
    private final int packedRepresentation;

    /**
     * Private constructor, 2 static methods are instead used to create
     * instances of Card
     * 
     * @param packedRepresentation
     *            (int): packed version of the card
     */
    private Card(int packedRepresentation) {
        this.packedRepresentation = packedRepresentation;
    }

    /**
     * Represents a card's 4 possible colors
     * 
     * @author Thomas Berkane (297780)
     */
    public enum Color {
        /**
         * The 4 possible card colors, with their associated symbols
         */
        SPADE("\u2660"), HEART("\u2661"), DIAMOND("\u2662"), CLUB("\u2663");

        /**
         * Unmodifiable list of all colors
         */
        public static final List<Color> ALL = Collections
                .unmodifiableList(Arrays.asList(values()));

        /**
         * The number of values in the enum
         */
        public static final int COUNT = ALL.size();

        /**
         * The symbol representing the color
         */
        private final String symbol;

        /**
         * Private constructor, associates a symbol to each color
         * 
         * @param symbol
         *            (String): The symbol representing the color
         */
        private Color(String symbol) {
            this.symbol = symbol;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return symbol;
        }
    }

    /**
     * Represents a card's rank
     * 
     * @author ThomasBerkane
     */
    public enum Rank {
        /**
         * The different card ranks, with their associated compact
         * representation and trump ordinal
         */
        SIX("6", 0), SEVEN("7", 1), EIGHT("8", 2), NINE("9", 7), TEN("10",
                3), JACK("J", 8), QUEEN("Q", 4), KING("K", 5), ACE("A", 6);

        /**
         * Unchangeable list of all ranks
         */
        public static final List<Rank> ALL = Collections
                .unmodifiableList(Arrays.asList(values()));
        /**
         * The number of values in the enum
         */
        public static final int COUNT = ALL.size();

        /**
         * A single letter/digit representing a card's rank
         */
        private final String compactRepresentation;

        /**
         * The position (between 0 and 8) of a trump card
         */
        private final int trumpOrdinal;

        /**
         * Private constructor, associates a compactRepresentation and
         * trumpOrdinal to each rank
         * 
         * @param compactRepresentation
         *            (String): A single letter/digit representing a card's rank
         * @param trumpOrdinal
         *            (int): The position (between 0 and 8) of a trump card
         */
        private Rank(String compactRepresentation, int trumpOrdinal) {
            this.compactRepresentation = compactRepresentation;
            this.trumpOrdinal = trumpOrdinal;
        }

        /**
         * @return (int): The position (between 0 and 8) of a trump card
         */
        public int trumpOrdinal() {
            return trumpOrdinal;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return compactRepresentation;
        }
    }

    /**
     * Creates an instance of Card with color c and rank r
     * 
     * @param c
     *            (Color): The color of the card to return
     * @param r
     *            (Rank): The rank of the card to return
     * @return (Card): The card with color c and rank r
     */
    public static Card of(Color c, Rank r) {
        return new Card(PackedCard.pack(c, r));
    }

    /**
     * Returns the card of which packed is the packed version
     * 
     * @param packed
     *            (int): The packed version of the card to return
     * @return (Card): The card of which packed is the packed version
     */
    public static Card ofPacked(int packed) {
        // Checks if packed is a valid packed card
        Preconditions.checkArgument(PackedCard.isValid(packed));
        return new Card(packed);
    }

    /**
     * @return (int): Packed version of the card
     */
    public int packed() {
        return packedRepresentation;
    }

    /**
     * @return (Color): The card's color
     */
    public Color color() {
        return PackedCard.color(packedRepresentation);
    }

    /**
     * @return (Rank): The card's rank
     */
    public Rank rank() {
        return PackedCard.rank(packedRepresentation);
    }

    /**
     * Compares two cards, based on their rank and color
     * 
     * @param trump
     *            (Color): The trump color
     * @param that
     *            (Card): The card to compare this to
     * @return (boolean): true iff this is better than that, knowing trump;
     *         returns false if the cards are not compatible
     */
    public boolean isBetter(Color trump, Card that) {
        return PackedCard.isBetter(trump, packedRepresentation, that.packed());
    }

    /**
     * Gives how many points a card is worth, knowing what the trump color is
     * 
     * @param trump
     *            (Card.Color): the trump color
     * @return (int): the points value of the packed card
     */
    public int points(Color trump) {
        return PackedCard.points(trump, packedRepresentation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Card))
            return false;
        Card other = (Card) obj;
        return packedRepresentation == other.packedRepresentation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return packedRepresentation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return PackedCard.toString(packedRepresentation);
    }

}
