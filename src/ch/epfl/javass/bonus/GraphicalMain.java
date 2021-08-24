package ch.epfl.javass.bonus;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.net.RemotePlayerClient;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class GraphicalMain extends Application {
    private final String[] defaultPlayerNames = new String[] { "Aline",
            "Bastien", "Colette", "David" };

    private final TextField[] nameFields = new TextField[PlayerId.COUNT];
    private final ToggleGroup[] typeGroup = new ToggleGroup[PlayerId.COUNT];
    private final RadioButton[] buttonH = new RadioButton[PlayerId.COUNT];
    private final RadioButton[] buttonR = new RadioButton[PlayerId.COUNT];
    private final RadioButton[] buttonS = new RadioButton[PlayerId.COUNT];
    private final TextField[] IPFields = new TextField[PlayerId.COUNT];
    private final List<ChoiceBox<String>> iterationChoiceBoxes = new ArrayList<>(
            PlayerId.COUNT);
    private final TextField[] iterationFields = new TextField[PlayerId.COUNT];
    private final TextField seedField = new TextField();
    private final Button remoteLauncherB = new Button(
            "Lancer un joueur distant");
    private final BorderPane messagePane = new BorderPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Team texts and alignment
        Text[] teamTexts = new Text[TeamId.COUNT];
        for (int i = 0; i < TeamId.COUNT; i++) {
            teamTexts[i] = new Text("EQUIPE " + (i + 1));
            teamTexts[i].setStyle("-fx-font-weight: bold;");
            GridPane.setHalignment(teamTexts[i], HPos.CENTER);
        }

        // 4 player panes
        GridPane[] playerPanes = new GridPane[PlayerId.COUNT];
        for (PlayerId id : PlayerId.ALL)
            playerPanes[id.ordinal()] = createPlayerPane(id);

        // Main pane
        GridPane mainPane = new GridPane();
        mainPane.addColumn(0, teamTexts[0], playerPanes[0], playerPanes[2]);
        mainPane.addColumn(1, teamTexts[1], playerPanes[1], playerPanes[3]);
        mainPane.add(createBottomPane(), 0, 3, 2, 1);

        // Style
        mainPane.setStyle(
                "-fx-background-color: whitesmoke;-fx-padding: 5px;-fx-alignment: center;");

        Pane mainAndMessagePanes = new StackPane(mainPane,
                createRemoteMessagePane());
        // Scene
        Scene scene = new Scene(mainAndMessagePanes);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Javass - Configuration de la partie");
        primaryStage.show();
    }

    private GridPane createPlayerPane(PlayerId id) {
        // Player's ordinal
        int i = id.ordinal();

        // Creating pane
        GridPane playerPane = new GridPane();
        playerPane.setStyle(
                "-fx-background-color: whitesmoke;-fx-padding: 5px 5px 5px -15px;-fx-hgap: 5;-fx-vgap: 5;-fx-alignment: center;");

        // Player text
        Text playerText = new Text("JOUEUR " + (id.ordinal() + 1));
        playerText.setStyle("-fx-font-weight: bold;-fx-rotate: -90;");
        playerPane.add(playerText, 0, 0, 1, 4);

        // Player name
        Label nameLabel = new Label("Nom : ");
        nameFields[i] = new TextField();
        nameFields[i].setPromptText(defaultPlayerNames[id.ordinal()]);
        nameFields[i].setPrefWidth(10);
        nameFields[i].textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String oV,
                    String nV) {
                if (!nV.matches(".{0,24}"))
                    nameFields[i].setText(oV);
            }
        });

        // Player type
        Label typeLabel = new Label("Type de\njoueur : ");
        typeGroup[i] = new ToggleGroup();
        buttonH[i] = new RadioButton("Joueur humain");
        buttonH[i].setToggleGroup(typeGroup[i]);
        buttonH[i].setSelected(true);
        buttonR[i] = new RadioButton("Joueur distant");
        buttonR[i].setToggleGroup(typeGroup[i]);
        buttonS[i] = new RadioButton("Joueur simulé");
        buttonS[i].setToggleGroup(typeGroup[i]);
        VBox typeButtonBox = new VBox(buttonH[i], buttonR[i], buttonS[i]);
        typeButtonBox.setSpacing(5);

        // Adding columns
        playerPane.addColumn(1, nameLabel, typeLabel);
        playerPane.addColumn(2, nameFields[i], typeButtonBox);

        // IP
        IPFields[i] = new TextField();
        IPFields[i].setPromptText("localhost");
        IPFields[i].setPrefWidth(100);
        VBox IPBox = new VBox(new Text("IP :"), IPFields[i]);
        IPBox.setAlignment(Pos.TOP_CENTER);
        IPBox.visibleProperty().bind(buttonR[i].selectedProperty());
        playerPane.add(IPBox, 3, 1, 1, 1);

        // Iterations choice box
        iterationChoiceBoxes.add(new ChoiceBox<>());
        iterationChoiceBoxes.get(i).getItems().addAll("100", "1000", "10000",
                "100000", "Autre (> 9)");
        iterationChoiceBoxes.get(i).setValue("10000");
        // Iterations field
        iterationFields[i] = new TextField();
        iterationFields[i].setPromptText("10000");
        iterationFields[i].setPrefWidth(100);
        iterationFields[i].textProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> v,
                            String oV, String nV) {
                        if (!nV.matches("\\d{0,9}"))
                            iterationFields[i].setText(oV);
                    }
                });
        iterationFields[i].visibleProperty().bind(iterationChoiceBoxes.get(i)
                .valueProperty().isEqualTo("Autre (> 9)"));
        VBox iterationBox = new VBox(new Text("Itérations :"),
                iterationChoiceBoxes.get(i), iterationFields[i]);
        iterationBox.setAlignment(Pos.TOP_CENTER);
        iterationBox.visibleProperty().bind(buttonS[i].selectedProperty());
        playerPane.add(iterationBox, 3, 1, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(55);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(65);
        playerPane.getColumnConstraints().addAll(col1, col2);

        return playerPane;
    }

    private GridPane createBottomPane() {
        GridPane bottomPane = new GridPane();
        bottomPane.setAlignment(Pos.CENTER);

        Label seedLabel = new Label("Graine aléatoire :");
        seedField.setPromptText("Optionnel");
        seedField.setMaxWidth(112);
        seedField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String oV,
                    String nV) {
                if (!nV.matches("([\\-])?\\d{0,10}"))
                    seedField.setText(oV);
            }
        });

        IntegerProperty[] iterationValues = new IntegerProperty[PlayerId.COUNT];

        for (int i = 0; i < PlayerId.COUNT; i++) {
            int integer = i;
            iterationValues[i] = new SimpleIntegerProperty();
            iterationFields[i].textProperty().addListener((o, oV, nV) -> {
                if (!nV.isEmpty()) {
                    iterationValues[integer].setValue(Integer.parseInt(nV));
                }
            });
        }

        Button startButton = new Button("Lancer la partie");
        startButton.disableProperty().bind(iterationChoiceBoxes.get(0)
                .valueProperty().isEqualTo("Autre (> 9)")
                .and(typeGroup[0].selectedToggleProperty()
                        .isEqualTo(buttonS[0]))
                .and(iterationFields[0].textProperty().isNotEmpty())
                .and(iterationValues[0].lessThan(10))
                .or(iterationChoiceBoxes.get(1).valueProperty()
                        .isEqualTo("Autre (> 9)")
                        .and(typeGroup[1].selectedToggleProperty()
                                .isEqualTo(buttonS[1]))
                        .and(iterationFields[1].textProperty().isNotEmpty())
                        .and(iterationValues[1].lessThan(10)))
                .or(iterationChoiceBoxes.get(2).valueProperty()
                        .isEqualTo("Autre (> 9)")
                        .and(typeGroup[2].selectedToggleProperty()
                                .isEqualTo(buttonS[2]))
                        .and(iterationFields[2].textProperty().isNotEmpty())
                        .and(iterationValues[2].lessThan(10)))
                .or(iterationChoiceBoxes.get(3).valueProperty()
                        .isEqualTo("Autre (> 9)")
                        .and(typeGroup[3].selectedToggleProperty()
                                .isEqualTo(buttonS[3]))
                        .and(iterationFields[3].textProperty().isNotEmpty())
                        .and(iterationValues[3].lessThan(10)))
                .or(seedField.textProperty().isEqualTo("-")));
        startButton.setOnMouseClicked(s -> {
            startGame();
        });
        VBox seedBox = new VBox(seedLabel, seedField, startButton);
        seedBox.setAlignment(Pos.CENTER);

        remoteLauncherB.setOnMouseClicked(s -> {
            startRemote();
            messagePane.setVisible(true);
        });

        GridPane.setHalignment(seedBox, HPos.CENTER);
        GridPane.setHalignment(remoteLauncherB, HPos.CENTER);

        bottomPane.add(seedBox, 0, 0, 1, 1);
        bottomPane.add(remoteLauncherB, 1, 0, 1, 1);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(300);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(300);
        bottomPane.getColumnConstraints().addAll(col1, col2);

        return bottomPane;
    }

    private BorderPane createRemoteMessagePane() {
        Text text = new Text(
                "La partie commencera à la connexion du client...");
        messagePane.setCenter(text);

        messagePane
                .setStyle("-fx-font: 16 Optima;-fx-background-color: white;");
        messagePane.setVisible(false);
        return messagePane;
    }

    private void startRemote() {
        // Creates local player which will be controlled remotely
        GraphicalPlayerAdapter localPlayer = new GraphicalPlayerAdapter();
        // Creates new thread for remote player
        Thread gameThread = new Thread(() -> {
            RemotePlayerServer server = new RemotePlayerServer(localPlayer);
            try {
                server.run();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Connexion fermée.");
                System.exit(1);
            }
        });

        // Starts thread
        gameThread.setDaemon(true);
        gameThread.start();
    }

    private void startGame() {
        if (!buttonH[0].isSelected() && !buttonH[1].isSelected()
                && !buttonH[2].isSelected() && !buttonH[3].isSelected()) {
            Platform.setImplicitExit(false);
        }

        Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);
        Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);

        Random rng;
        long gameSeed;
        long[] playerSeeds = new long[PlayerId.COUNT];

        // Optional random seed argument
        if (!seedField.getText().isEmpty())
            rng = new Random(Long.parseLong(seedField.getText()));
        else
            rng = new Random();

        // Initializing all seeds with rng
        gameSeed = rng.nextLong();
        for (int i = 0; i < PlayerId.COUNT; i++)
            playerSeeds[i] = rng.nextLong();

        // Players
        for (PlayerId id : PlayerId.ALL) {
            int i = id.ordinal();
            String name = defaultPlayerNames[i];
            if (!nameFields[i].getText().isEmpty())
                name = nameFields[i].getText();
            playerNames.put(id, name);

            int iterations = 10000;
            if (!iterationChoiceBoxes.get(i).getValue().equals("Autre (> 9)"))
                iterations = Integer
                        .parseInt(iterationChoiceBoxes.get(i).getValue());
            else if (!iterationFields[i].getText().isEmpty())
                iterations = Integer.parseInt(iterationFields[i].getText());
            else
                iterations = 10000;

            String IP = "localhost";
            if (!IPFields[i].getText().isEmpty())
                IP = IPFields[i].getText();

            // Creating players
            if (buttonH[i].isSelected())
                players.put(id, new GraphicalPlayerAdapter());

            if (buttonS[i].isSelected()) {
                MctsPlayer mctsPlayer = new MctsPlayer(PlayerId.ALL.get(i),
                        playerSeeds[i], iterations);
                players.put(id, new PacedPlayer(mctsPlayer, 2));
            }

            if (buttonR[i].isSelected()) {
                RemotePlayerClient remotePlayer = null;
                try {
                    remotePlayer = new RemotePlayerClient(IP);
                } catch (Exception e) {
                    System.err.println(
                            "Erreur :  la connexion au serveur d'un joueur distant a échoué : "
                                    + id);
                    System.exit(1);
                }
                players.put(id, remotePlayer);
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
                System.err.println("Une erreur est survenue.");
                System.exit(1);
            }
            Platform.exit();

        });
        
        // Starts thread
        gameThread.setDaemon(true);
        gameThread.start();
        
        // Closes game configuration screen
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        Stage stage = (Stage) seedField.getScene().getWindow();
        stage.close();

    }
}
