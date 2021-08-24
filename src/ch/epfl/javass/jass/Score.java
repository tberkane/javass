package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

/**
 * Non instantiable class which represents scores in a game of Jass
 * 
 * @author Yingxuan Duan (282512)
 *
 */
public final class Score {

    /** The initial score at the start of a game */
    public static final Score INITIAL = new Score(PackedScore.INITIAL);

    /**
     * Packed version of the score
     */
    private final long packedRepresentation;

    /**
     * Private constructor, which makes the class not instantiable
     * 
     * @param packedRepresentation
     *            (long): Packed version of the score
     */
    private Score(long packedRepresentation) {
        this.packedRepresentation = packedRepresentation;
    }

    /**
     * Checks if packed is a valid packed score, then creates a new Score
     * corresponding to packed
     * 
     * @param packed
     *            (long): Packed version of the score
     * @return (Score): for which packed is the packed representation
     */
    public static Score ofPacked(long packed) {
        Preconditions.checkArgument(PackedScore.isValid(packed));
        return new Score(packed);
    }

    /**
     * @return (long): Packed version of the score
     */
    public long packed() {
        return packedRepresentation;
    }

    /**
     * @param t
     *            (TeamId): the team's id
     * @return (int): the numbers of tricks won by team t during current turn
     */
    public int turnTricks(TeamId t) {
        return PackedScore.turnTricks(packedRepresentation, t);
    }

    /**
     * @param t
     *            (TeamId): the team's id
     * @return (int): the numbers of points won by team t during current turn
     */
    public int turnPoints(TeamId t) {
        return PackedScore.turnPoints(packedRepresentation, t);
    }

    /**
     * @param t
     *            (TeamId): the team's id
     * @return (int): the numbers of points won by team t during the game
     */
    public int gamePoints(TeamId t) {
        return PackedScore.gamePoints(packedRepresentation, t);
    }

    /**
     * @param t
     *            (TeamId): the team's id
     * @return (int): the sum of the points won by team t during the past turns
     *         and current turn
     */
    public int totalPoints(TeamId t) {
        return PackedScore.totalPoints(packedRepresentation, t);
    }

    /**
     * Checks if the trick points is at least 0 then returns the updated score
     * 
     * @param winningTeam
     *            (TeamId): the Id of winning team
     * @param trickPoints
     *            (int): the number of points won by the winning team during the
     *            trick
     * @return (Score): a updated score with the points won by the winning team
     *         (100 additional points if they won all tricks) and adds 1 to
     *         their won tricks
     */
    public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) {
        // Checking that trickPoints if non-negative
        Preconditions.checkArgument(trickPoints >= 0);
        return ofPacked(PackedScore.withAdditionalTrick(packedRepresentation,
                winningTeam, trickPoints));
    }

    /**
     * @return (Score): the updated score for the next turn, adding points
     *         earned during the turn to total points, and resetting the former
     *         to 0
     */
    public Score nextTurn() {
        return ofPacked(PackedScore.nextTurn(packedRepresentation));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Long.hashCode(packedRepresentation);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return PackedScore.toString(packedRepresentation);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        Score other = (Score) obj;
        return (packedRepresentation == other.packedRepresentation);
    }
}
