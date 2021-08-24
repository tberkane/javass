package ch.epfl.javass.net;

/**
 * Messages exchanged between client and server
 * 
 * @author Thomas Berkane (297780)
 */
public enum JassCommand {
    PLRS, TRMP, HAND, TRCK, CARD, SCOR, WINR;

    public static final int COMMAND_LENGTH = 4;
}
