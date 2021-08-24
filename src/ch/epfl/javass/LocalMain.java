package ch.epfl.javass;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.net.RemotePlayerClient;
import ch.epfl.javass.net.StringSerializer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Contains the main program for playing Jass locally
 * 
 * @author Yingxuan Duan (282512)
 * 
 */
public final class LocalMain extends Application {

    private static final int MIN_TIME = 2;
    private static final int DEFAULT_ITERATIONS = 10_000;
    private static final int SEED_INDEX = 4;

    /**
     * Launches the application with the given args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage arg0) throws Exception {
        // Names and players
        String[] defaultPlayerNames = new String[] { "Aline", "Bastien",
                "Colette", "David" };
        Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);
        Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);

        // Seeds
        Random rng;
        long gameSeed;
        long[] playerSeeds = new long[PlayerId.COUNT];

        // Gets parameters
        List<String> arguments = getParameters().getRaw();

        // Wrong number of args (other than 4 or 5)
        if (!(arguments.size() == PlayerId.COUNT
                || arguments.size() == SEED_INDEX + 1))
            printError(
                    "Utilisation: java ch.epfl.javass.LocalMain <j1>…<j4> [<graine>]\noù :\n<jn> spécifie le joueur n, ainsi:\n  h:<nom>  un joueur humain nommé <nom>\n  s:<nom>:<itérations>  un joueur simulé nommé <nom> et s'éxécutant avec <itérations> itérations\n  r:<nom>:<IP>  un joueur distant nommé <nom> dont le serveur s'éxécute sur l'ordinateur dont l'adresse IP est <IP>\n[<graine>] spécifie la graine à utiliser pour générer les graines des différents générateurs aléatoires du programme");

        // Optional random seed argument
        if (arguments.size() == SEED_INDEX + 1) {
            long seed = 0L;
            try {
                seed = Long.parseLong(arguments.get(SEED_INDEX));
            } catch (NumberFormatException e) {
                printError(
                        "Erreur : la graine aléatoire doit être un entier long valide : "
                                + arguments.get(SEED_INDEX));
            }
            rng = new Random(seed);
        } else
            rng = new Random();

        // Initializing all seeds with rng
        gameSeed = rng.nextLong();
        for (int i = 0; i < PlayerId.COUNT; i++)
            playerSeeds[i] = rng.nextLong();

        for (int i = 0; i < PlayerId.COUNT; i++) {
            String[] argumentComponents = StringSerializer.split(':',
                    arguments.get(i));

            // Check if player type is valid (h, s or r)
            String playerType = argumentComponents[0];
            if (!(playerType.equals("h") || playerType.equals("s")
                    || playerType.equals("r")))
                printError(
                        "Erreur : la première composante de la spécification du joueur "
                                + (i + 1) + " n'est pas h, s ou r : "
                                + argumentComponents[0]);

            // Too many argument components, i.e. more than 2 for human player,
            // or more than 3 for others
            if (!((playerType.equals("h") && argumentComponents.length <= 2)
                    || (!playerType.equals("h")
                            && argumentComponents.length <= 3)))
                printError("Erreur : la spécification du joueur " + (i + 1)
                        + " comporte trop de composantes : "
                        + arguments.get(i));

            // put given player name in the map if it exists, otherwise use
            // default name
            String name = defaultPlayerNames[i];
            if (componentIsPresent(argumentComponents, 1))
                name = argumentComponents[1];
            playerNames.put(PlayerId.ALL.get(i), name);

            // use given Iterations or IP if they exist and are valid,
            // otherwise use default ones
            int iterations = DEFAULT_ITERATIONS;
            String ip = "localhost";

            if (componentIsPresent(argumentComponents, 2)) {
                if (playerType.equals("s")) {
                    try {
                        iterations = Integer.parseInt(argumentComponents[2]);
                    } catch (NumberFormatException e) {
                        printError("Erreur : le nombre d'itérations du joueur "
                                + (i + 1) + " doit être un entier : "
                                + argumentComponents[2]);
                    }
                    if (iterations <= Jass.HAND_SIZE)
                        printError("Erreur : le nombre d'itérations du joueur "
                                + (i + 1) + " doit être au moins 10 : "
                                + iterations);
                } else
                    ip = argumentComponents[2];

            }

            // Creating players with received information
            if (playerType.equals("h"))
                players.put(PlayerId.ALL.get(i), new GraphicalPlayerAdapter());

            if (playerType.equals("s")) {
                MctsPlayer mctsPlayer = new MctsPlayer(PlayerId.ALL.get(i),
                        playerSeeds[i], iterations);
                players.put(PlayerId.ALL.get(i),
                        new PacedPlayer(mctsPlayer, MIN_TIME));
            }

            if (playerType.equals("r")) {
                RemotePlayerClient remotePlayer = null;
                try {
                    remotePlayer = new RemotePlayerClient(ip);
                } catch (IOException e) {
                    printError(
                            "Erreur :  la connexion au serveur d'un joueur distant a échoué : "
                                    + PlayerId.ALL.get(i));
                }
                players.put(PlayerId.ALL.get(i), remotePlayer);
            }
        }

        // Creating new thread
        Thread gameThread = new Thread(() -> {
            JassGame g = new JassGame(gameSeed, players, playerNames);
            try {
                while (!g.isGameOver()) {
                    g.advanceToEndOfNextTrick();
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
            } catch (UncheckedIOException e) {
                printError("Une erreur est survenue.");
            }
        });

        // Starts thread
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /*
     * Prints error message to standard error output stream and exits program
     */
    private void printError(String errorMessage) {
        System.err.println(errorMessage);
        System.exit(1);
    }

    /*
     * Checks if components has a component at given index
     */
    private boolean componentIsPresent(String[] components, int index) {
        return (components.length > index && components[index].length() > 0);
    }

}
