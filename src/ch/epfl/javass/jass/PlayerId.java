package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Identifies each of the 4 players
 * 
 * @author Thomas Berkane (297780)
 *
 */
public enum PlayerId {
    PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

    /**
     * List of all values in PlayerId
     */
    public static final List<PlayerId> ALL = Collections
            .unmodifiableList(Arrays.asList(values()));

    /**
     * Number of values in PlayerId
     */
    public static final int COUNT = ALL.size();

    /**
     * @return (TeamId): the team to which the player belongs (team 1 for
     *         players 1 and 3; team 2 for players 2 and 4)
     */
    public TeamId team() {
        return TeamId.ALL.get(this.ordinal() % TeamId.COUNT);
    }
}
