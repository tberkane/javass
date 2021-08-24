package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Contains the main program for playing Jass remotely
 *
 * @author ThomasBerkane (297780)
 *
 */
public class RemoteMain extends Application {

    /**
     * Launches the application with the given args
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Creates local player which will be controlled remotely
        GraphicalPlayerAdapter localPlayer = new GraphicalPlayerAdapter();
        // Creates new thread for remote player
        Thread gameThread = new Thread(() -> {
            RemotePlayerServer server = new RemotePlayerServer(localPlayer);
            try {
                server.run();
            } catch (Exception e) {
                System.err.println("Connection fermée.");
                System.exit(1);
            }
        });

        System.out.println("La partie commencera à la connexion du client...");

        // Starts thread
        gameThread.setDaemon(true);
        gameThread.start();
    }

}
