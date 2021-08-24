package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Bean of the current trick
 * 
 * @author Yingxuan Duan (282512)
 *
 */
public final class TrickBean {
    /**
     * Object property containing the current trump
     */
    private final ObjectProperty<Color> trump;
    /**
     * Observable map mapping the Id of a player to the card he played in this
     * trick, the card is null if he has not yet played
     */
    private final ObservableMap<PlayerId, Card> trick;
    /**
     * Object property containing Id of the player winning this trick
     */
    private final ObjectProperty<PlayerId> winningPlayer;

    public TrickBean() {
        trump = new SimpleObjectProperty<>();
        trick = FXCollections.observableHashMap();
        PlayerId.ALL.forEach(pId -> trick.put(pId, null));
        winningPlayer = new SimpleObjectProperty<>();
    }

    /**
     * @return (ReadOnlyObjectProperty<Color>): a read only object property
     *         containing the trump
     */
    public ReadOnlyObjectProperty<Color> trumpProperty() {
        return trump;
    }

    /**
     * Modifies the current trump
     * 
     * @param newTrump
     *            (Color): the new trump color that we want to set
     */
    public void setTrump(Color newTrump) {
        trump.set(newTrump);
    }

    /**
     * @return (ObservableMap<PlayerId, Card>): an unmodifiable view of the
     *         observable map trick
     */
    public ObservableMap<PlayerId, Card> trick() {
        return FXCollections
                .unmodifiableObservableMap(trick);
    }

    /**
     * Changes the value of the properties trick and winningPlayer
     * 
     * @param newTrick
     *            (Trick): with which we update the properties
     */
    public void setTrick(Trick newTrick) {
        // If the trick is empty, make all cards null
        if (newTrick.isEmpty()) {
            trick.replaceAll((k, v) -> null);
            winningPlayer.set(null);
            // Else, put the right cards for the players who have played and
            // null for the others
        } else {
            for (int i = 0; i < PlayerId.COUNT; i++)
                trick.put(newTrick.player(i),
                        i < newTrick.size() ? newTrick.card(i) : null);
            winningPlayer.set(newTrick.winningPlayer());
        }
    }

    /**
     * @return (ReadOnlyObjectProperty<PlayerId>): a read only object property
     *         containing the winner of the trick
     */
    public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty() {
        return winningPlayer;
    }

}
