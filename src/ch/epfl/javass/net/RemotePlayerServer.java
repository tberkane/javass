package ch.epfl.javass.net;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
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
 * Represents a player's server, which awaits a connection on port 5108 and
 * makes the local player play according to the messages received
 * 
 * @author Thomas Berkane (297780)
 */
public final class RemotePlayerServer {
    /**
     * Player which this server pilots
     */
    private final Player localPlayer;
    private static final int COMMAND_START = 0;
    private static final int ARGS_START = JassCommand.COMMAND_LENGTH + 1;
    private static final int OWN_ID_START = 0, OWN_ID_LENGTH = 1;
    private static final int NAMES_START = OWN_ID_LENGTH + 1;

    /**
     * Constructor
     * 
     * @param localPlayer
     *            (Player): player which this server pilots
     */
    public RemotePlayerServer(Player localPlayer) {
        this.localPlayer = localPlayer;
    }

    /**
     * In an infinite loop, this method: waits for a message from the client,
     * calls localPlayer's corresponding method, and in the case of cardToPlay,
     * sends the returned value back to the client
     */
    public void run() {
        // Creates a new remote communicator to communicate with the client
        try (RemoteCommunicator serverCommunicator = RemoteCommunicator
                .createServerCommunicator()) {
            while (true) {
                String line = serverCommunicator.readLine();
                // Extracts the command (4 letters) from the read line
                JassCommand command = JassCommand.valueOf(line
                        .substring(COMMAND_START, JassCommand.COMMAND_LENGTH));
                // Extracts the command's arguments from the read line
                String commandArgs = line.substring(ARGS_START);

                // Processes all possible commands
                switch (command) {
                case PLRS:
                    setPlayersLocal(commandArgs);
                    break;
                case TRMP:
                    setTrumpLocal(commandArgs);
                    break;
                case HAND:
                    updateHandLocal(commandArgs);
                    break;
                case TRCK:
                    updateTrickLocal(commandArgs);
                    break;
                case CARD:
                    serverCommunicator.writeLine(cardToPlayLocal(commandArgs));
                    break;
                case SCOR:
                    updateScoreLocal(commandArgs);
                    break;
                case WINR:
                    setWinningTeamLocal(commandArgs);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /*
     * The following methods each parse the arguments they receive by using
     * methods from StringSerializer, then call localPlayer's corresponding
     * method with these parsed arguments
     */

    private void setPlayersLocal(String commandArguments) {
        // Extracts the player's own id
        PlayerId ownId = PlayerId.ALL.get(Integer.parseInt(
                commandArguments.substring(OWN_ID_START, OWN_ID_LENGTH)));
        // Extracts the names
        String[] serializedNames = StringSerializer.split(',',
                commandArguments.substring(NAMES_START));

        Map<PlayerId, String> playerNames = new EnumMap<PlayerId, String>(
                PlayerId.class);
        // Deserializes all the names
        for (PlayerId id : PlayerId.ALL)
            playerNames.put(id, StringSerializer
                    .deserializeString(serializedNames[id.ordinal()]));

        localPlayer.setPlayers(ownId, playerNames);
    }

    private void setTrumpLocal(String commandArguments) {
        Color trump = Card.Color.ALL.get(Integer.parseInt(commandArguments));
        localPlayer.setTrump(trump);
    }

    private void updateScoreLocal(String commandArguments) {
        Score score = Score
                .ofPacked(StringSerializer.deserializeLong(commandArguments));
        localPlayer.updateScore(score);
    }

    private void updateTrickLocal(String commandArguments) {
        Trick trick = Trick
                .ofPacked(StringSerializer.deserializeInt(commandArguments));
        localPlayer.updateTrick(trick);
    }

    private void updateHandLocal(String commandArguments) {
        CardSet hand = CardSet
                .ofPacked(StringSerializer.deserializeLong(commandArguments));
        localPlayer.updateHand(hand);
    }

    private String cardToPlayLocal(String commandArguments) {
        // Extracts the turnState and the hand
        String[] turnStateAndHand = StringSerializer.split(' ',
                commandArguments);
        // Isolates the turnState's components
        String[] turnStateComponents = StringSerializer.split(',',
                turnStateAndHand[0]);

        // Deserializes all components of the turnState
        long pkScore = StringSerializer.deserializeLong(turnStateComponents[0]);
        long pkUnplayed = StringSerializer
                .deserializeLong(turnStateComponents[1]);
        int pkTrick = StringSerializer.deserializeInt(turnStateComponents[2]);

        // Assembles the turnState
        TurnState turnState = TurnState.ofPackedComponents(pkScore, pkUnplayed,
                pkTrick);
        // Isolates the hand
        CardSet hand = CardSet.ofPacked(
                StringSerializer.deserializeLong(turnStateAndHand[1]));

        Card card = localPlayer.cardToPlay(turnState, hand);
        return StringSerializer.serializeInt(card.packed());
    }

    private void setWinningTeamLocal(String commandArguments) {
        TeamId winningTeamId = TeamId.ALL
                .get(Integer.parseInt(commandArguments));
        localPlayer.setWinningTeam(winningTeamId);
    }
}
