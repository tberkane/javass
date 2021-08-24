package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Allows to identify each of the two teams
 * 
 * @author Thomas Berkane (297780)
 *
 */
public enum TeamId {
    TEAM_1, TEAM_2;

    /**
     * List of all values in TeamId
     */
    public static final List<TeamId> ALL = Collections
            .unmodifiableList(Arrays.asList(values()));

    /**
     * Number of values in TeamId
     */
    public static final int COUNT = ALL.size();

    /**
     * @return (TeamId): the other team than the one this method was applied on
     */
    public TeamId other() {
        return this == TEAM_1 ? TEAM_2 : TEAM_1;
    }

}
