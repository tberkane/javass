package ch.epfl.javass.jass;

import static ch.epfl.javass.bits.Bits32.extract;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Color;

/**
 * Methods for manipulating tricks packed in ints
 * 
 * @author Thomas Berkane (297780)
 */
public final class PackedTrick {

    /**
     * An invalid packed trick
     */
    public static final int INVALID = -1;

    private static final int CARD_SIZE = 6;
    private static final int CARDS_PER_TRICK = 4;
    private static final int INDEX_START = 24, INDEX_SIZE = 4;
    private static final int TRUMP_START = 30, TRUMP_SIZE = 2;
    private static final int FIRST_PLAYER_START = 28, FIRST_PLAYER_SIZE = 2;

    /**
     * Private constructor because PackedTrick is non instantiable
     */
    private PackedTrick() {
    }

    /**
     * Checks if pkTrick is a valid packed trick, that is if index is between 0
     * and 8, and the eventual invalid cards are grouped in the superior indices
     * 
     * @param pkTrick
     *            (int): packed trick
     * @return (boolean): true iff pkTrick is valid
     */
    public static boolean isValid(int pkTrick) {
        int index = extract(pkTrick, INDEX_START, INDEX_SIZE);
        boolean[] cardValid = new boolean[CARDS_PER_TRICK];
        boolean[] cardInvalid = new boolean[CARDS_PER_TRICK];

        for (int i = 0; i < CARDS_PER_TRICK; i++) {
            int card = extract(pkTrick, i * CARD_SIZE, CARD_SIZE);
            cardValid[i] = PackedCard.isValid(card);
            cardInvalid[i] = card == PackedCard.INVALID;
        }

        // Checks that there is either a unique invalid card at index 3; 2
        // invalid cards at indices 3 and 2; 3 invalid cards at indices 3, 2 and
        // 1; or that all cards are invalid

        return index >= 0 && index < Jass.TRICKS_PER_TURN
                && ((cardInvalid[0] && cardInvalid[1] && cardInvalid[2]
                        && cardInvalid[3])
                        || (cardValid[0] && cardInvalid[1] && cardInvalid[2]
                                && cardInvalid[3])
                        || (cardValid[0] && cardValid[1] && cardInvalid[2]
                                && cardInvalid[3])
                        || (cardValid[0] && cardValid[1]
                                && (cardValid[2] && cardInvalid[3])
                                || (cardValid[0] && cardValid[1] && cardValid[2]
                                        && cardValid[3])));
    }

    /**
     * @param trump
     *            (Color): the trump color for the trick
     * @param firstPlayer
     *            (PlayerId): the player who will play first in the trick
     * @return (int): an empty packed trick with index 0 and given trump color
     *         and first player
     */
    public static int firstEmpty(Color trump, PlayerId firstPlayer) {
        return (Bits32.pack(firstPlayer.ordinal(), FIRST_PLAYER_SIZE,
                trump.ordinal(), TRUMP_SIZE) << FIRST_PLAYER_START)
                | (Bits32.mask(0, INDEX_START));
    }

    /**
     * Returns the empty next packed trick, or INVALID if it is the last trick
     * of the turn
     * 
     * @param pkTrick
     *            (int): the current pkTrick
     * @return (int): an empty trick with the same trump as pkTrick, the next
     *         index, and the first player is the winner of pkTrick
     */
    public static int nextEmpty(int pkTrick) {
        assert isValid(pkTrick);

        if (isLast(pkTrick))
            return INVALID;

        return firstEmpty(trump(pkTrick), winningPlayer(pkTrick))
                | ((index(pkTrick) + 1) << INDEX_START);
    }

    /**
     * @param pkTrick
     *            (int): the packed trick
     * @return (boolean): whether pkTrick is the last trick of the turn
     */
    public static boolean isLast(int pkTrick) {
        assert isValid(pkTrick);
        return index(pkTrick) == Jass.TRICKS_PER_TURN - 1;
    }

    /**
     * @param pkTrick
     *            (int): the packed trick
     * @return (boolean): whether pkTrick contains no cards
     */
    public static boolean isEmpty(int pkTrick) {
        assert isValid(pkTrick);
        return size(pkTrick) == 0;
    }

    /**
     * @param pkTrick
     *            (int): the packed trick
     * @return (boolean): whether all cards have been played in pkTrick
     */
    public static boolean isFull(int pkTrick) {
        assert isValid(pkTrick);
        return size(pkTrick) == CARDS_PER_TRICK;
    }

    /**
     * @param pkTrick
     *            (int): the packed trick
     * @return (int): the number of cards contained by the trick
     */
    public static int size(int pkTrick) {
        assert isValid(pkTrick);
        // Finds the first invalid card in the trick and returns its index as
        // the size of the trick
        for (int i = 0; i < CARDS_PER_TRICK; i++) {
            if (card(pkTrick, i) == PackedCard.INVALID)
                return i;
        }
        return CARDS_PER_TRICK;
    }

    /**
     * @param pkTrick
     *            (int): the packed trick
     * @return (Color): the trump color of the trick
     */
    public static Color trump(int pkTrick) {
        assert isValid(pkTrick);
        return Color.ALL.get(extract(pkTrick, TRUMP_START, TRUMP_SIZE));
    }

    /**
     * @param pkTrick
     *            (int): the packed trick
     * @param index
     *            (int): index of the player in the trick
     * @return (PlayerId): the player of given index in the trick, given that
     *         the player at index 0 is the first
     */
    public static PlayerId player(int pkTrick, int index) {
        assert isValid(pkTrick);
        // The index of the player to retrieve, starts at firstPlayer, to which
        // we add index and apply modulo 4 to get an index between 0 and 3
        int playerIndex = (extract(pkTrick, FIRST_PLAYER_START,
                FIRST_PLAYER_SIZE) + index) % PlayerId.COUNT;
        return PlayerId.ALL.get(playerIndex);
    }

    /**
     * @param pkTrick
     *            (int): packed trick
     * @return (int): the index of the trick
     */
    public static int index(int pkTrick) {
        assert isValid(pkTrick);
        return Bits32.extract(pkTrick, INDEX_START, INDEX_SIZE);
    }

    /**
     * @param pkTrick
     *            (int): packed trick
     * @param index
     *            (int): the index of the card in the trick
     * @return (int): the index'th card played
     */
    public static int card(int pkTrick, int index) {
        assert isValid(pkTrick);
        return Bits32.extract(pkTrick, index * CARD_SIZE, CARD_SIZE);
    }

    /**
     * Returns the packed trick with the given packed card added in it
     * 
     * @param pkTrick
     *            (int): packed trick
     * @param pkCard
     *            (int):the packed card which we want to add to pkTrick
     * @return (int): pktrick with pkCard in it
     */
    public static int withAddedCard(int pkTrick, int pkCard) {
        assert isValid(pkTrick);
        assert PackedCard.isValid(pkCard);

        // Sets the 6 bits for the card we are going to add to 0, then sets them
        // to the bits of pkCard
        return (pkTrick & ~Bits32.mask(size(pkTrick) * CARD_SIZE, CARD_SIZE))
                | (pkCard << size(pkTrick) * CARD_SIZE);
    }

    /**
     * @param pkTrick
     *            (int): packed trick
     * @return (Color): the base color, that is the color of first card played
     *         in the given trick
     */
    public static Color baseColor(int pkTrick) {
        assert isValid(pkTrick);
        // The card played first during the trick
        int firstCard = card(pkTrick, 0);
        return PackedCard.color(firstCard);
    }

    /**
     * Returns a subset of the packed card set pkHand, containing cards which
     * can be played as the next card of the given trick, following the rules of
     * Jass
     * 
     * @param pkTrick
     *            (int): packed trick
     * @param pkHand
     *            (long): hand of cards
     * @return (long): packed version of a subset of pkHand, in which all cards
     *         are playable
     */
    public static long playableCards(int pkTrick, long pkHand) {
        assert isValid(pkTrick);
        assert PackedCardSet.isValid(pkHand);

        // If we play first, then we can play any card in our hand
        if (isEmpty(pkTrick))
            return pkHand;

        // The trump color
        Color trump = trump(pkTrick);
        // The base color
        Color base = baseColor(pkTrick);
        // Trump cards in hand
        long trumpInHand = PackedCardSet.subsetOfColor(pkHand, trump);
        // Cards of base color in hand
        long baseInHand = PackedCardSet.subsetOfColor(pkHand, base);
        // The best card played yet
        int best = bestCard(pkTrick);
        // Whether the trick has been cut yet, that is if the color of the best
        // card is trump
        boolean trickHasBeenCut = PackedCard.color(best) == trump;
        // If we have trumps to play which are better than bestCard
        boolean hasBetterTrumps = !PackedCardSet.isEmpty(PackedCardSet
                .intersection(pkHand, PackedCardSet.trumpAbove(best)));

        // If the base color is trump
        if (base == trump) {
            // No trumps in hand or only bour -> play anything
            if (PackedCardSet.isEmpty(trumpInHand)
                    || (trumpInHand == PackedCardSet.singleton(
                            PackedCard.pack(trump, Card.Rank.JACK)))) {
                return pkHand;
            }

            // Else play any trump
            return trumpInHand;
        }

        // If no base colors in hand
        if (PackedCardSet.isEmpty(baseInHand)) {
            if (trickHasBeenCut) {
                // Hand is only weaker trumps -> play anything
                if (pkHand == trumpInHand && !hasBetterTrumps)
                    return pkHand;

                // Else play any card except for weaker trump
                return PackedCardSet.difference(pkHand,
                        PackedCardSet.subsetOfColor(
                                PackedCardSet.complement(
                                        PackedCardSet.trumpAbove(best)),
                                trump));
            }
            return pkHand;
        }

        // If trick hasn't been cut, play a base color or a trump
        if (!trickHasBeenCut)
            return PackedCardSet.union(trumpInHand, baseInHand);

        // If it has been cut, play a base color or a better trump
        return PackedCardSet.union(PackedCardSet.intersection(pkHand,
                PackedCardSet.trumpAbove(best)), baseInHand);
    }

    /**
     * @param pkTrick
     *            (int): packed trick
     * @return (int): value of the given trick, includes the 5 extra-points when
     *         it is the last trick of a turn
     */
    public static int points(int pkTrick) {
        assert isValid(pkTrick);

        int total = 0;
        Color trump = trump(pkTrick);
        // Adds up all the points of the cards played during the trick
        for (int i = 0; i < CARDS_PER_TRICK; i++)
            total += PackedCard.points(trump, card(pkTrick, i));

        // Bonus points if this is last trick of turn
        if (isLast(pkTrick))
            total += Jass.LAST_TRICK_ADDITIONAL_POINTS;

        return total;
    }

    /**
     * @param pkTrick
     *            (int): packed trick
     * @return (PlayerId): Id of the player winning the given trick
     */
    public static PlayerId winningPlayer(int pkTrick) {
        assert isValid(pkTrick);

        int bestCard = bestCard(pkTrick);
        // Winning player is the one who has played the best card
        for (int i = 0; i < CARDS_PER_TRICK - 1; i++) {
            if (card(pkTrick, i) == bestCard)
                return player(pkTrick, i);
        }
        return player(pkTrick, CARDS_PER_TRICK - 1);
    }

    /*
     * Auxiliary method, returns the index of the best card in a given trick
     * 
     * @param pkTrick (int): packed trick
     * 
     * @return (int): index of the strongest card in pkTrick
     */
    private static int bestCard(int pkTrick) {
        assert isValid(pkTrick);

        int card;
        int bestCard = card(pkTrick, 0);
        for (int i = 1; i < size(pkTrick); i++) {
            card = card(pkTrick, i);
            if (PackedCard.isBetter(trump(pkTrick), card, bestCard))
                bestCard = card;
        }
        return bestCard;
    }

    /**
     * @param pkTrick
     *            (int): packed trick
     * @return (String): a textual representation of pkTrick, listing all cards
     *         contained, its index and the first player of the trick
     */
    public static String toString(int pkTrick) {
        assert (isValid(pkTrick));
        StringJoiner j = new StringJoiner(",");

        for (int i = 0; i < size(pkTrick); i++) {
            j.add(Card.ofPacked(card(pkTrick, i)).toString());
        }

        return "Pli " + index(pkTrick) + ", commencé par "
                + PlayerId.ALL.get(
                        extract(pkTrick, FIRST_PLAYER_START, FIRST_PLAYER_SIZE))
                + ": " + j;
    }

}
