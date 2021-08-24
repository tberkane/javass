package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.jass.Card.Color;

/**
 * A player
 * 
 * @author Yingxuan Duan (282512)
 *
 */
public interface Player {

    /**
     * @param state
     *            (TurnState): the state of the current turn
     * @param hand
     *            (CardSet): the player's hand
     * @return (Card): the card which the player wishes to play
     */
    Card cardToPlay(TurnState state, CardSet hand);

    /**
     * Informs the player of his/her id and of the players' names
     * 
     * @param ownId
     *            (PlayerId): this player's id
     * @param playerNames
     *            (Map<PlayerId, String>): the players' names
     */
    default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {

    }

    /**
     * Called each time that this player's hand changes
     * 
     * @param newHand
     *            (CardSet): this player's new hand
     */
    default void updateHand(CardSet newHand) {

    }

    /**
     * Called each time the trump changes
     * 
     * @param trump
     *            (Color): new trump color
     */
    default void setTrump(Color trump) {

    }

    /**
     * Called each time the trick changes
     * 
     * @param newTrick
     *            (Trick): new trick
     */
    default void updateTrick(Trick newTrick) {

    }

    /**
     * Called each time the score changes
     * 
     * @param score
     *            (Score): new score
     */
    default void updateScore(Score score) {

    }

    /**
     * Called once when a team reaches 1000 points
     * 
     * @param winningTeam
     *            (TeamId): Id of the team which has won the game
     */
    default void setWinningTeam(TeamId winningTeam) {

    }
}
