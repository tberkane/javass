package ch.epfl.javass.gui;

import java.util.Collections;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

/**
 * Bean of the hand of a player
 * 
 * @author Duan Yingxuan (282512)
 *
 */
public final class HandBean {
    /**
     * Property representing the hand, this observable list always contains 9
     * elements of type card
     */
    final private ObservableList<Card> hand;
    /**
     * Observable set of playable cards, containing all cards actually playable,
     * is empty while it is not this player's turn to play
     */
    final private ObservableSet<Card> playableCards;

    /**
     * Public constructor of hand bean, creates a hand bean in which all cards
     * are null
     */
    public HandBean() {
        this.hand = FXCollections
                .observableArrayList(Collections.nCopies(Jass.HAND_SIZE, null));
        this.playableCards = FXCollections.observableSet();
    }

    /**
     * @return (ObservableList<Card>): an observable yet unmodifiable list
     *         representing the hand
     */
    public ObservableList<Card> hand() {
        return FXCollections.unmodifiableObservableList(hand);
    }

    /**
     * Modifies the contents of observable list hand, if newHand contains 9
     * cards, then all cards in the list are replaced; if newHand has less than
     * 9 cards, the cards not in newHand become null in the list
     * 
     * @param newHand
     *            (CardSet): the new hand containing at most 9 cards
     */
    public void setHand(CardSet newHand) {
        if (newHand.size() == Jass.HAND_SIZE) {
            for (int i = 0; i < Jass.HAND_SIZE; i++)
                hand.set(i, newHand.get(i));
        } else {
            for (int i = 0; i < Jass.HAND_SIZE; i++) {
                if (hand.get(i) != null && !newHand.contains(hand.get(i)))
                    hand.set(i, null);
            }
        }
    }

    /**
     * @return (ObservableSet<Card>): an unmodifiable view of the observable set
     *         representing playable cards
     */
    public ObservableSet<Card> playableCards() {
        return FXCollections.unmodifiableObservableSet(playableCards);
    }

    /**
     * Replaces the contents of playableCards by the cards in the given
     * newPlayableCards
     * 
     * @param newPlayableCards
     *            (CardSet): set of cards representing new playable cards for
     *            this player
     */
    public void setPlayableCards(CardSet newPlayableCards) {
        playableCards.clear();
        for (int i = 0; i < newPlayableCards.size(); i++)
            playableCards.add(newPlayableCards.get(i));
    }

}
