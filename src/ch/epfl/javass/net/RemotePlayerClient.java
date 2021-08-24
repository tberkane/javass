package ch.epfl.javass.net;

import java.io.IOException;
import java.util.Map;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;

/**
 * Represents the client of a player in a game of Jass, considered as a normal
 * player by the game
 * 
 * @author Thomas Berkane (297780)
 */
public final class RemotePlayerClient implements Player, AutoCloseable {
    /**
     * Communicator used to communicate with the server
     */
    private final RemoteCommunicator clientCommunicator;

    /**
     * Constructor
     * 
     * @param hostName
     *            (String): host name
     * @throws IOException:
     *             if the connection fails
     */
    public RemotePlayerClient(String hostName) throws IOException {
        clientCommunicator = RemoteCommunicator
                .createClientCommunicator(hostName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.javass.jass.Player#setPlayers(ch.epfl.javass.jass.PlayerId,
     * java.util.Map)
     */
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        String[] serializedNames = new String[PlayerId.COUNT];
        for (PlayerId id : PlayerId.ALL)
            serializedNames[id.ordinal()] = StringSerializer
                    .serializeString(playerNames.get(id));

        clientCommunicator.writeLine("PLRS " + ownId.ordinal() + " "
                + StringSerializer.combine(',', serializedNames));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.javass.jass.Player#updateHand(ch.epfl.javass.jass.CardSet)
     */
    @Override
    public void updateHand(CardSet newHand) {
        clientCommunicator.writeLine(
                "HAND " + StringSerializer.serializeLong(newHand.packed()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.javass.jass.Player#setTrump(ch.epfl.javass.jass.Card.Color)
     */
    @Override
    public void setTrump(Color trump) {
        clientCommunicator.writeLine("TRMP " + trump.ordinal());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.javass.jass.Player#updateScore(ch.epfl.javass.jass.Score)
     */
    @Override
    public void updateScore(Score score) {
        clientCommunicator.writeLine(
                "SCOR " + StringSerializer.serializeLong(score.packed()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ch.epfl.javass.jass.Player#setWinningTeam(ch.epfl.javass.jass.TeamId)
     */
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        clientCommunicator.writeLine("WINR " + winningTeam.ordinal());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.javass.jass.Player#updateTrick(ch.epfl.javass.jass.Trick)
     */
    @Override
    public void updateTrick(Trick newTrick) {
        clientCommunicator.writeLine(
                "TRCK " + StringSerializer.serializeInt(newTrick.packed()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState,
     * ch.epfl.javass.jass.CardSet)
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        clientCommunicator.writeLine("CARD " + StringSerializer.combine(',',
                StringSerializer.serializeLong(state.packedScore()),
                StringSerializer.serializeLong(state.packedUnplayedCards()),
                StringSerializer.serializeInt(state.packedTrick()) + " "
                        + StringSerializer.serializeLong(hand.packed())));

        String serializedCard = clientCommunicator.readLine();

        return Card.ofPacked(StringSerializer.deserializeInt(serializedCard));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        clientCommunicator.close();
    }

}
