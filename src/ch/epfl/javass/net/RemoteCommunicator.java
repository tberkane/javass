package ch.epfl.javass.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Allows a server and a client to communicate remotely by writing and reading
 * lines through a Writer and a Reader. Implements AutoCloseable so that it can
 * be used in a try-with-resources.
 * 
 * @author Thomas Berkane (297780)
 */
public final class RemoteCommunicator implements AutoCloseable {
    private static final int PORT_NUMBER = 6000;
    /**
     * The socket used to communicate by either the server or the client
     */
    private final Socket socket;
    /**
     * Reader so that server and client can read each other's messages
     */
    private final BufferedReader reader;
    /**
     * Writer so that server and client can send each other messages
     */
    private final BufferedWriter writer;

    /**
     * Private constructor, factory methods are used instead to better
     * distinguish between creating a RemoteCommunicator used by a client and
     * creating a RemoteCommunicator used by a server
     * 
     * @param socket
     *            (Socket): The socket used by the client/server to communicate
     *            with the other
     * @throws IOException:
     *             if the socket fails to create the InputStream or the
     *             OutputStream
     */
    private RemoteCommunicator(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), US_ASCII));
        this.writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), US_ASCII));
    }

    /**
     * Returns a new RemoteCommunicator meant to be used by the client
     * 
     * @throws IOException:
     *             if the socket fails to be created
     */
    public static RemoteCommunicator createClientCommunicator(String hostName)
            throws IOException {
        Socket socket = new Socket(hostName, PORT_NUMBER);
        return new RemoteCommunicator(socket);
    }

    /**
     * Returns a new RemoteCommunicator meant to be used by the server
     * 
     * @throws IOException:
     *             if the socket fails to be created
     */
    public static RemoteCommunicator createServerCommunicator()
            throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        Socket socket = serverSocket.accept();
        return new RemoteCommunicator(socket);

    }

    /**
     * The writer writes a string followed by a line return, and then flushes
     */
    public void writeLine(String line) throws UncheckedIOException {
        try {
            writer.write(line + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * The reader reads a line and returns it
     */
    public String readLine() throws UncheckedIOException{
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return line;
    }

    /* (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }

}
