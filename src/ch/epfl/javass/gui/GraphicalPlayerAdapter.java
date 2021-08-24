package ch.epfl.javass.gui;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;
import javafx.application.Platform;

/**
 * An adapter adapting a graphical interface to a player
 * 
 * @author Yingxuan Duan (282512)
 *
 */
public final class GraphicalPlayerAdapter implements Player {

    /*
     * Bean of hand containing properties
     */
    private final HandBean handBean;
    /*
     * Bean of trick
     */
    private final TrickBean trickBean;
    /*
     * Bean of score
     */
    private final ScoreBean scoreBean;
    /*
     * Graphical interface which we want to adapt
     */
    private GraphicalPlayer graphicalPlayer;
    /**
     * Queue containing the chosen card to be played
     */
    private final ArrayBlockingQueue<Card> queue;

    /**
     * Constructor
     */
    public GraphicalPlayerAdapter() {
        this.handBean = new HandBean();
        this.trickBean = new TrickBean();
        this.scoreBean = new ScoreBean();
        this.queue = new ArrayBlockingQueue<Card>(1);
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#setPlayers(ch.epfl.javass.jass.PlayerId, java.util.Map)
     */
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        graphicalPlayer = new GraphicalPlayer(ownId, playerNames, scoreBean,
                trickBean, handBean, queue);

        Platform.runLater(() -> {
            graphicalPlayer.createStage().show();
        });
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#updateHand(ch.epfl.javass.jass.CardSet)
     */
    @Override
    public void updateHand(CardSet newHand) {
        Platform.runLater(() -> {
            handBean.setHand(newHand);
        });
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#setTrump(ch.epfl.javass.jass.Card.Color)
     */
    @Override
    public void setTrump(Color trump) {
        Platform.runLater(() -> {
            trickBean.setTrump(trump);
        });
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#updateTrick(ch.epfl.javass.jass.Trick)
     */
    @Override
    public void updateTrick(Trick newTrick) {
        Platform.runLater(() -> {
            trickBean.setTrick(newTrick);
        });
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#updateScore(ch.epfl.javass.jass.Score)
     */
    @Override
    public void updateScore(Score score) {
        Platform.runLater(() -> {
            for (TeamId id : TeamId.ALL) {
                scoreBean.setGamePoints(id, score.gamePoints(id));
                scoreBean.setTurnPoints(id, score.turnPoints(id));
                scoreBean.setTotalPoints(id, score.totalPoints(id));
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#setWinningTeam(ch.epfl.javass.jass.TeamId)
     */
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        Platform.runLater(() -> {
            scoreBean.setWinningTeam(winningTeam);
        });
    }

    /* (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState, ch.epfl.javass.jass.CardSet)
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        Platform.runLater(() -> {
            handBean.setPlayableCards(state.trick().playableCards(hand));
        });

        Card chosenCard;
        try {
            chosenCard = queue.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }

        Platform.runLater(() -> {
            handBean.setPlayableCards(CardSet.EMPTY);
        });

        return chosenCard;
    }

}
