package ch.epfl.javass.jass;

/**
 * An interface containing useful constants for the Jass game
 * 
 * @author Thomas Berkane (297780)
 */
public interface Jass {
    /** The number of cards in hand at the start of a turn */
    public static final int HAND_SIZE = 9;
    /** Number of tricks in a turn */
    public static final int TRICKS_PER_TURN = 9;
    /** Points to win */
    public static final int WINNING_POINTS = 1000;
    /** Points obtained if a team wins all the tricks of a turn */
    public static final int MATCH_ADDITIONAL_POINTS = 100;
    /** Points obtained for winning last trick */
    public static final int LAST_TRICK_ADDITIONAL_POINTS = 5;

}
