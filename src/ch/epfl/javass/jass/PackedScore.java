package ch.epfl.javass.jass;

import static ch.epfl.javass.jass.TeamId.TEAM_1;
import static ch.epfl.javass.jass.TeamId.TEAM_2;

import java.util.EnumMap;
import java.util.Map;

import ch.epfl.javass.bits.Bits64;

/**
 * Non instantiable class with static methods for manipulating scores packed
 * into ints
 * 
 * @author Thomas Berkane (297780)
 */
public final class PackedScore {

    /**
     * Initial score of a game of Jass
     */
    public static final long INITIAL = 0L;

    private static final int TURN_TRICKS_START = 0, TURN_TRICKS_SIZE = 4;
    private static final int TURN_POINTS_START = 4, TURN_POINTS_SIZE = 9;
    private static final int GAME_POINTS_START = 13, GAME_POINTS_SIZE = 11;
    private static final int MAX_TURN_POINTS = 152
            + Jass.LAST_TRICK_ADDITIONAL_POINTS + Jass.MATCH_ADDITIONAL_POINTS;
    private static final int MAX_GAME_POINTS = 2 * Jass.WINNING_POINTS;
    private static final int UNUSED_BITS_START = 24, UNUSED_BITS_SIZE = 8;

    /**
     * Associates to each team a teamModifier which is used when shifting bits
     * for example
     */
    private static final Map<TeamId, Integer> TEAM_MODIFIERS = new EnumMap<TeamId, Integer>(
            TeamId.class) {
        {
            put(TEAM_1, 0);
            put(TEAM_2, 32);
        }
    };

    /**
     * Private constructor because PackedScore is non instantiable
     */
    private PackedScore() {
    }

    /**
     * Checks if the given value is a valid packed score, that is if the bits
     * containing the number of tricks contain a value between 0 and 9, those
     * for the turn points between 0 and 257, those for the game points between
     * 0 and 2000, and that the other bits are 0
     * 
     * @param pkScore
     *            (long): the packed score whose validity to check
     * @return (boolean): whether the packed score is valid
     */
    public static boolean isValid(long pkScore) {
        // Checks that both teams' scores are valid
        return isValid32(pkScore, TEAM_1) && isValid32(pkScore, TEAM_2);
    }

    /**
     * Packs 2 sets of turnTricks, turnPoints and gamePoints into a long
     * 
     * @param turnTricks1
     *            (int): First team's number of tricks won in the current turn
     * @param turnPoints1
     *            (int): First team's number of points won in the current turn
     * @param gamePoints1
     *            (int): First team's points won during the game
     * @param turnTricks2
     *            (int): Second team's number of tricks won in the current turn
     * @param turnPoints2
     *            (int): Second team's number of points won in the current turn
     * @param gamePoints2
     *            (int): Second team's points won during the game
     * @return (long): The packed score
     */
    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1,
            int turnTricks2, int turnPoints2, int gamePoints2) {
        return Bits64.pack(turnTricks1, TURN_TRICKS_SIZE, turnPoints1,
                TURN_POINTS_SIZE)
                | Bits64.pack(gamePoints1, GAME_POINTS_SIZE, 0,
                        UNUSED_BITS_SIZE) << GAME_POINTS_START
                | Bits64.pack(turnTricks2, TURN_TRICKS_SIZE, turnPoints2,
                        TURN_POINTS_SIZE) << TEAM_MODIFIERS.get(TEAM_2)
                | Bits64.pack(gamePoints2, GAME_POINTS_SIZE, 0,
                        UNUSED_BITS_SIZE) << TEAM_MODIFIERS.get(TEAM_2)
                                + GAME_POINTS_START;
    }

    /**
     * @param pkScore
     *            (long): The packed score from which to extract the turn tricks
     * @param t
     *            (TeamId): The team whose turn tricks we want
     * @return (int): Number of tricks won by team t during the turn
     */
    public static int turnTricks(long pkScore, TeamId t) {
        // Checking that the packed score is valid
        assert (isValid(pkScore));
        // Bits extracted depend on t
        return (int) Bits64.extract(pkScore, TEAM_MODIFIERS.get(t),
                TURN_TRICKS_SIZE);
    }

    /**
     * @param pkScore
     *            (long): The packed score from which to extract the turn points
     * @param t
     *            (TeamId): The team whose turn points we want
     * @return (int): Number of points won by team t during the turn
     */
    public static int turnPoints(long pkScore, TeamId t) {
        // Checking that the packed score is valid
        assert (isValid(pkScore));
        // Bits extracted depend on t
        return (int) Bits64.extract(pkScore,
                TEAM_MODIFIERS.get(t) + TURN_POINTS_START, TURN_POINTS_SIZE);
    }

    /**
     * @param pkScore
     *            (long): The packed score from which to extract the game points
     * @param t
     *            (TeamId): The team whose game points we want
     * @return (int): Number of points won by team t during the game
     */
    public static int gamePoints(long pkScore, TeamId t) {
        // Checking that the packed score is valid
        assert (isValid(pkScore));
        // Bits extracted depend on t
        return (int) Bits64.extract(pkScore,
                TEAM_MODIFIERS.get(t) + GAME_POINTS_START, GAME_POINTS_SIZE);
    }

    /**
     * @param pkScore
     *            (long): The packed score from which to extract the total
     *            points
     * @param t
     *            (TeamId): The team whose total points we want
     * @return (int): Total number of points won by team t during the game
     */
    public static int totalPoints(long pkScore, TeamId t) {
        // Checking that the packed score is valid
        assert (isValid(pkScore));
        // Total points = turn points + game points
        return turnPoints(pkScore, t) + gamePoints(pkScore, t);
    }

    /**
     * Updates the packed score with the points won by the winning team (100
     * additional points if they won all 9 tricks) and adds 1 to their won
     * tricks
     * 
     * @param pkScore
     *            (long): the packed score
     * @param winningTeam
     *            (TeamId): the team which won the trick
     * @param trickPoints
     *            (int): the number of points won by the winning team during the
     *            trick
     * @return (long): the updated packed score
     */
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam,
            int trickPoints) {
        // Checking that the packed score is valid
        assert (isValid(pkScore));
        // Used to know the range of bits we are going to work on
        int modifier = TEAM_MODIFIERS.get(winningTeam);

        // Increases the number of won tricks by one and adds trickPoints for
        // the winning team
        pkScore += (1L << modifier)
                + ((long) trickPoints << (TURN_POINTS_START + modifier));

        // If the winning team won all 9 tricks, they get additional points
        if (turnTricks(pkScore, winningTeam) == Jass.TRICKS_PER_TURN)
            pkScore += (long) Jass.MATCH_ADDITIONAL_POINTS << (TURN_POINTS_START
                    + modifier);
        return pkScore;
    }

    /**
     * Updates the packed score for the next turn, adding points earned during
     * the turn to game points, and resetting the former to 0
     * 
     * @param pkScore
     *            (long): the packed score
     * @return (long): the updated packed score
     */
    public static long nextTurn(long pkScore) {
        // Checking that the packed score is valid
        assert (isValid(pkScore));

        pkScore = pack(0, 0,
                turnPoints(pkScore, TEAM_1)
                        + gamePoints(pkScore, TeamId.TEAM_1),
                0, 0, turnPoints(pkScore, TEAM_2)
                        + gamePoints(pkScore, TeamId.TEAM_2));

        return pkScore;
    }

    /**
     * Returns a textual representation of both teams' score
     * 
     * @param pkScore
     *            (long): the packed score
     * @return (String): the score represented as a string
     */
    public static String toString(long pkScore) {
        // Checking that the packed score is valid
        assert (isValid(pkScore));

        return "(" + turnTricks(pkScore, TEAM_1) + ","
                + turnPoints(pkScore, TEAM_1) + ","
                + gamePoints(pkScore, TEAM_1) + ") / ("
                + turnTricks(pkScore, TEAM_2) + ","
                + turnPoints(pkScore, TEAM_2) + ","
                + gamePoints(pkScore, TEAM_2) + ")";
    }

    /*
     * Checks if the 32 bits containing a single team's score are valid, used by
     * isValid
     * 
     * @param pkScore (long): the packed score whose validity to check
     * 
     * @param team (TeamId): the Id of the team we are checking
     * 
     * @return (boolean): whether the relevant team's packed score is valid
     */
    private static boolean isValid32(long pkScore, TeamId team) {
        // Checks the 3 bounds and that the unused bits are 0
        int modifier = TEAM_MODIFIERS.get(team);
        return checkBounds(pkScore, TURN_TRICKS_START + modifier,
                TURN_TRICKS_SIZE, Jass.TRICKS_PER_TURN)
                && checkBounds(pkScore, TURN_POINTS_START + modifier,
                        TURN_POINTS_SIZE, MAX_TURN_POINTS)
                && checkBounds(pkScore, GAME_POINTS_START + modifier,
                        GAME_POINTS_SIZE, MAX_GAME_POINTS)
                && Bits64.extract(pkScore, UNUSED_BITS_START + modifier,
                        UNUSED_BITS_SIZE) == 0;
    }

    /*
     * An auxiliary method which extracts the value between start and start +
     * size from pkScore and checks that it is between 0 and bound
     * 
     * @param pkScore (long): the packed score from which to extract a value
     * 
     * @param start (int): the start index from which to extract
     * 
     * @param size (int): the size in bits of the value to extract
     * 
     * @param bound (int): the upper bound to compare the extracted value with
     * 
     * @return (boolean): whether the extracted value is between 0 and bound
     */
    private static boolean checkBounds(long pkScore, int start, int size,
            int bound) {
        long value = Bits64.extract(pkScore, start, size);
        return value <= bound;
    }
}
