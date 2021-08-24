package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * A player which takes a minimum time to play
 * 
 * @author Yingxuan Duan (282512)
 *
 */
public final class PacedPlayer implements Player {
    /**
     * The player which this is going to behave as
     */
    private final Player underlyingPlayer;
    /**
     * The minimum time this takes to play a card
     */
    private final double minTime;

    /**
     * Creates a new PacedPlayer
     * 
     * @param underlyingPlayer
     *            (Player): The player which this is going to behave as
     * @param minTime
     *            (double): The minimum time this takes to play a card
     */
    public PacedPlayer(Player underlyingPlayer, double minTime) {
        Preconditions.checkArgument(minTime > 0);
        this.underlyingPlayer = underlyingPlayer;
        this.minTime = minTime;
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#setPlayers(ch.epfl.javass.jass.PlayerId, java.util.Map)
     */
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        underlyingPlayer.setPlayers(ownId, playerNames);
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#updateHand(ch.epfl.javass.jass.CardSet)
     */
    @Override
    public void updateHand(CardSet newHand) {
        underlyingPlayer.updateHand(newHand);
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#setTrump(ch.epfl.javass.jass.Card.Color)
     */
    @Override
    public void setTrump(Color trump) {
        underlyingPlayer.setTrump(trump);
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#updateTrick(ch.epfl.javass.jass.Trick)
     */
    @Override
    public void updateTrick(Trick newTrick) {
        underlyingPlayer.updateTrick(newTrick);
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#updateScore(ch.epfl.javass.jass.Score)
     */
    @Override
    public void updateScore(Score score) {
        underlyingPlayer.updateScore(score);
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#setWinningTeam(ch.epfl.javass.jass.TeamId)
     */
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        underlyingPlayer.setWinningTeam(winningTeam);
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState, ch.epfl.javass.jass.CardSet)
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        // Stores current time
        long time = System.currentTimeMillis();
        // Finds card to play
        Card cardToPlay = underlyingPlayer.cardToPlay(state, hand);

        long t = System.currentTimeMillis() - time;
        // If not enough time has elapsed, wait
        if (t < minTime * 1000) {
            try {
                Thread.sleep((long) minTime * 1000 - t);
            } catch (InterruptedException e) {
            }
        }
        return cardToPlay;
    }

}
