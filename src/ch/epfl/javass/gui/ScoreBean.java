package ch.epfl.javass.gui;

import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Bean of score
 * 
 * @author Yingxuan Duan (282512)
 */
public final class ScoreBean {
    /**
     * Contains both teams' turn points
     */
    private final IntegerProperty[] turnPoints;
    /**
     * Contains both teams' game points
     */
    private final IntegerProperty[] gamePoints;
    /**
     * Contains both teams' total points
     */
    private final IntegerProperty[] totalPoints;
    /**
     * Contains the id of the winning team
     */
    private final ObjectProperty<TeamId> winningTeam;

    /**
     * Public constructor, creates a new score bean whose properties can be read
     * and written
     */
    public ScoreBean() {
        turnPoints = new IntegerProperty[] { new SimpleIntegerProperty(),
                new SimpleIntegerProperty() };
        gamePoints = new IntegerProperty[] { new SimpleIntegerProperty(),
                new SimpleIntegerProperty() };
        totalPoints = new IntegerProperty[] { new SimpleIntegerProperty(),
                new SimpleIntegerProperty() };
        winningTeam = new SimpleObjectProperty<>();
    }

    /**
     * @param team
     *            (TeamId): the Id of the team whose turn points we want to know
     * @return (ReadOnlyIntegerProperty): a read only property containing the
     *         turn points of the given team
     */
    public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
        return turnPoints[team.ordinal()];
    }

    /**
     * Modifies the property turnPoints's value of a given team
     * 
     * @param team
     *            (TeamId): Id of the team whose turn points we want to set
     * @param newTurnPoints
     *            (int): new value of the property turnPoints of a given team
     */
    public void setTurnPoints(TeamId team, int newTurnPoints) {
        turnPoints[team.ordinal()].set(newTurnPoints);
    }

    /**
     * @param team
     *            (TeamId): the Id of the team whose game points we want to know
     * @return (ReadOnlyIntegerProperty): a read only property containing the
     *         game points of the given team
     */
    public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
        return gamePoints[team.ordinal()];
    }

    /**
     * Modifies the game points of a given team
     * 
     * @param team
     *            (TeamId): Id of the team whose game points we want to set
     * @param newGamePoints
     *            (int): new points to be given to the property game points of
     *            the team
     */
    public void setGamePoints(TeamId team, int newGamePoints) {
        gamePoints[team.ordinal()].set(newGamePoints);

    }

    /**
     * @param team
     *            (TeamId): the Id of the team whose total points we want to
     *            know
     * @return (ReadOnlyIntegerProperty): a read only property containing the
     *         total points of the given team
     */
    public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
        return totalPoints[team.ordinal()];
    }

    /**
     * Modifies the total points of a given team
     * 
     * @param team
     *            (TeamId): Id of the team whose total points we want to set
     * @param newGamePoints
     *            (int): new value to be put in the property totalPoints of the
     *            given team
     */
    public void setTotalPoints(TeamId team, int newTotalPoints) {
        totalPoints[team.ordinal()].set(newTotalPoints);

    }

    /**
     * @return (ReadOnlyObjectProperty): a read only property containing the
     *         winning team of the game
     */
    public ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
        return winningTeam;
    }

    /**
     * Sets the winning team of the game
     * 
     * @param winningTeam
     *            (TeamId): the Id of the winning team we want to put in the
     *            corresponding property
     */
    public void setWinningTeam(TeamId winningTeam) {
        this.winningTeam.set(winningTeam);
    }

}
