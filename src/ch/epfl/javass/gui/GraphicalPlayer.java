package ch.epfl.javass.gui;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Graphical interface for a human player
 *
 * @author Yingxuan Duan (282512)
 *
 */
public final class GraphicalPlayer {
    private static final int SMALL_IMAGE_WIDTH = 160;
    private static final int LARGE_IMAGE_WIDTH = 240;
    private static final int TRUMP_WIDTH_AND_HEIGHT = 101;
    private static final int CARD_WIDTH = 120;
    private static final int CARD_HEIGHT = 180;
    private static final int HAND_CARD_WIDTH = 80;
    private static final int HAND_CARD_HEIGHT = 120;

    private static final int ROWSPAN_SIDE_PLAYER = 3;
    private static final int COLSPAN_SIDE_PLAYER = 1;
    private static final int GAUSSIAN_BLUR_RADIUS = 4;

    private static final int PLAYABLE_OPACITY = 1;
    private static final double UNPLAYABLE_OPACITY = 0.2;
    /**
     * Id of the player using this graphical interface
     */
    private final PlayerId ownId;
    /*
     * A map associating the players' id to their name
     */
    private final Map<PlayerId, String> playerNames;
    /*
     * An observable map associating each card to its image of size 240×360
     * pixels
     */
    private static final ObservableMap<Card, Image> LARGE_CARD_IMAGES = computeCardImages(
            LARGE_IMAGE_WIDTH);
    /*
     * An observable map associating each card to its image of size 160×240
     * pixels
     */
    private static final ObservableMap<Card, Image> SMALL_CARD_IMAGES = computeCardImages(
            SMALL_IMAGE_WIDTH);
    /*
     * Queue containing the chosen card to be played
     */
    private final ArrayBlockingQueue<Card> queue;
    /*
     * Scene for this graphical interface
     */
    private final Scene scene;

    /**
     * Creates a graphical interface for the given player
     *
     * @param ownId
     *            (PlayerId): id of the player using this graphical interface
     * @param playerNames
     *            (Map<PlayerId, String>): map associating the players' id to
     *            their name
     * @param scoreBean
     *            (ScoreBean): bean containing the properties of score
     * @param trickBean
     *            (TrickBean): bean containing the properties of trick
     * @param handBean
     *            (HandBean): bean containing the properties of hand
     * @param queue
     *            (ArrayBlockingQueue<Card>): queue containing the card to play
     */
    public GraphicalPlayer(PlayerId ownId, Map<PlayerId, String> playerNames,
            ScoreBean scoreBean, TrickBean trickBean, HandBean handBean,
            ArrayBlockingQueue<Card> queue) {
        this.ownId = ownId;
        this.playerNames = Collections
                .unmodifiableMap(new EnumMap<>(playerNames));
        this.queue = queue;

        BorderPane mainPane = new BorderPane();

        Pane scorePane = createScorePane(scoreBean);
        mainPane.setTop(scorePane);
        Pane trickPane = createTrickPane(trickBean);
        mainPane.setCenter(trickPane);
        Pane handPane = createHandPane(handBean);
        mainPane.setBottom(handPane);

        Pane victoryPane1 = createVictoryPane(TeamId.TEAM_1, scoreBean);
        Pane victoryPane2 = createVictoryPane(TeamId.TEAM_2, scoreBean);

        Pane mainAndVictoryPanes = new StackPane(mainPane, victoryPane1,
                victoryPane2);

        this.scene = new Scene(mainAndVictoryPanes);
    }

    /**
     * @return a new stage which represents the window, with a title indicating
     *         the player's name
     */
    public Stage createStage() {
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Javass - " + playerNames.get(ownId));
        return stage;
    }

    /*
     * @param cardWidth (int): the width of a card's image
     *
     * @return (ObservableMap<Card, Image>): associates each card to its image
     */
    private static ObservableMap<Card, Image> computeCardImages(int cardWidth) {
        Preconditions.checkArgument(cardWidth == SMALL_IMAGE_WIDTH
                || cardWidth == LARGE_IMAGE_WIDTH);
        ObservableMap<Card, Image> cardImages = FXCollections
                .observableHashMap();
        for (Color color : Color.ALL) {
            for (Rank rank : Rank.ALL)
                cardImages.put(Card.of(color, rank),
                        new Image("/card_" + color.ordinal() + "_"
                                + rank.ordinal() + "_" + cardWidth + ".png"));
        }
        return cardImages;
    }

    /*
     * Creates a score pane displaying the players' names and points
     *
     * @param scoreBean (ScoreBean): bean containing the properties of score
     *
     * @return (GridPane): the score pane
     */
    private GridPane createScorePane(ScoreBean scoreBean) {
        GridPane scorePane = new GridPane();

        for (TeamId team : TeamId.ALL) {
            // Names of players in each team
            Text names = new Text(
                    playerNames.get(PlayerId.ALL.get(team.ordinal())) + " et "
                            + playerNames.get(PlayerId.ALL
                                    .get(team.ordinal() + TeamId.COUNT))
                            + " : ");
            names.setTextAlignment(TextAlignment.RIGHT);

            // Turn points for each team
            Text turnPoints = new Text();
            turnPoints.textProperty()
                    .bind(Bindings.convert(scoreBean.turnPointsProperty(team)));
            turnPoints.setTextAlignment(TextAlignment.RIGHT);

            // Last trick points obtained by computing the difference between
            // the new value and old value of turn points property
            StringProperty lastTrickPointsProperty = new SimpleStringProperty();
            scoreBean.turnPointsProperty(team)
                    .addListener(
                            (v, oV, nV) -> lastTrickPointsProperty
                                    .setValue(" (+"
                                            + Math.max(0,
                                                    (nV.intValue()
                                                            - oV.intValue()))
                                            + ")"));
            Text lastTrickPoints = new Text();
            lastTrickPoints.textProperty()
                    .bind(Bindings.convert(lastTrickPointsProperty));

            // Total points for each team
            Text total = new Text(" / Total : ");
            Text totalPoints = new Text();
            totalPoints.textProperty().bind(
                    Bindings.convert(scoreBean.totalPointsProperty(team)));
            totalPoints.setTextAlignment(TextAlignment.RIGHT);

            // Putting everything together
            scorePane.addRow(team.ordinal(), names, turnPoints, lastTrickPoints,
                    total, totalPoints);
        }

        // Style
        scorePane.setStyle(
                "-fx-font: 16 Optima; -fx-background-color: lightgray; -fx-padding: 5px; -fx-alignment: center;");

        return scorePane;
    }

    /*
     * Creates a trick pane displaying the cards played during the trick and the
     * trump color
     *
     * @param trickBean (TrickBean): bean containing the properties of trick
     *
     * @return (GridPane): the trick pane
     */
    private GridPane createTrickPane(TrickBean trickBean) {
        GridPane trickPane = new GridPane();

        VBox[] boxes = new VBox[PlayerId.COUNT];

        for (int i = 0; i < PlayerId.COUNT; i++) {
            // Player's id
            PlayerId id = PlayerId.ALL
                    .get((ownId.ordinal() + i) % PlayerId.COUNT);
            // Player's name
            Text name = new Text(playerNames.get(id));

            ImageView cardImage = new ImageView();
            cardImage.setFitWidth(CARD_WIDTH);
            cardImage.setFitHeight(CARD_HEIGHT);
            // Binds cardImage's imageProperty to the right image in
            // largeCardImages
            cardImage.imageProperty().bind(Bindings.valueAt(LARGE_CARD_IMAGES,
                    Bindings.valueAt(trickBean.trick(), id)));

            // Visible halo on the strongest card of the trick
            Rectangle halo = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            halo.setStyle(
                    "-fx-arc-width: 20;-fx-arc-height: 20;-fx-fill: transparent;-fx-stroke: lightpink;-fx-stroke-width: 5;-fx-opacity: 0.5;");
            halo.setEffect(new GaussianBlur(GAUSSIAN_BLUR_RADIUS));
            halo.visibleProperty()
                    .bind(trickBean.winningPlayerProperty().isEqualTo(id));
            // Stacks the card and its halo
            StackPane cardImageAndHalo = new StackPane(cardImage, halo);

            // This player's name under his card, other players' name on top of
            // their card
            VBox nameAndCardImage = (i == 0) ? new VBox(cardImageAndHalo, name)
                    : new VBox(name, cardImageAndHalo);

            // Style
            nameAndCardImage.setStyle(
                    "-fx-font: 14 Optima;-fx-padding: 5px; -fx-alignment: center;");

            boxes[i] = nameAndCardImage;
        }

        // Displays trump color image
        ImageView trumpImage = new ImageView();
        trickBean.trumpProperty().addListener((v, oV, nV) -> trumpImage
                .setImage(new Image("/trump_" + nV.ordinal() + ".png")));
        trumpImage.setFitWidth(TRUMP_WIDTH_AND_HEIGHT);
        trumpImage.setFitHeight(TRUMP_WIDTH_AND_HEIGHT);
        GridPane.setHalignment(trumpImage, HPos.CENTER);

        // Adding everything to trick pane
        trickPane.add(boxes[3], 0, 0, COLSPAN_SIDE_PLAYER, ROWSPAN_SIDE_PLAYER);
        trickPane.addColumn(1, boxes[2], trumpImage, boxes[0]);
        trickPane.add(boxes[1], 2, 0, COLSPAN_SIDE_PLAYER, ROWSPAN_SIDE_PLAYER);

        // Style
        trickPane.setStyle(
                "-fx-background-color: whitesmoke;-fx-padding: 5px;-fx-border-width: 3px 0px;-fx-border-style: solid;-fx-border-color: gray;-fx-alignment: center;");

        return trickPane;
    }

    /*
     * Creates a victory pane for the given team which is visible only if that
     * team has won
     *
     * @param team (TeamId): Id of the team for this pane represents the winning
     * team
     *
     * @param scoreBean (ScoreBean): bean of score
     *
     * @return (BorderPane): the desired victory pane
     */
    private BorderPane createVictoryPane(TeamId team, ScoreBean scoreBean) {
        BorderPane victoryPane = new BorderPane();

        Text text = new Text();
        text.textProperty().bind(Bindings.format(
                "%s et %s ont gagné avec %s points contre %s.",
                playerNames.get(PlayerId.ALL.get(team.ordinal())),
                playerNames
                        .get(PlayerId.ALL.get(team.ordinal() + TeamId.COUNT)),
                scoreBean.totalPointsProperty(team),
                scoreBean.totalPointsProperty(team.other())));

        victoryPane.setCenter(text);
        victoryPane.visibleProperty()
                .bind(scoreBean.winningTeamProperty().isEqualTo(team));

        victoryPane
                .setStyle("-fx-font: 16 Optima;-fx-background-color: white;");

        return victoryPane;
    }

    /*
     * Creates the hand pane showing cards in this player's hand
     *
     * @param handBean (HandBean): bean of hand
     *
     * @return (HBox): the desired hand pane
     */
    private HBox createHandPane(HandBean handBean) {
        HBox handPane = new HBox();

        for (int i = 0; i < Jass.HAND_SIZE; i++) {
            ImageView cardImage = new ImageView();
            cardImage.setFitWidth(HAND_CARD_WIDTH);
            cardImage.setFitHeight(HAND_CARD_HEIGHT);
            int cardIndex = i;

            // Binds cardImage's imageProperty to the right image in
            // smallCardImages
            cardImage.imageProperty().bind(Bindings.valueAt(SMALL_CARD_IMAGES,
                    Bindings.valueAt(handBean.hand(), cardIndex)));
            // If card is clicked, it is placed in queue to be played
            cardImage.setOnMouseClicked(
                    c -> queue.add(handBean.hand().get(cardIndex)));

            // Decides whether card is playable by considering hand and
            // playableCards of hand bean
            BooleanBinding isPlayable = Bindings.createBooleanBinding(
                    () -> handBean.playableCards()
                            .contains(handBean.hand().get(cardIndex)),
                    handBean.playableCards(), handBean.hand());

            // Cards are transparent when they can't be played currently
            cardImage.opacityProperty().bind(Bindings.when(isPlayable)
                    .then(PLAYABLE_OPACITY).otherwise(UNPLAYABLE_OPACITY));

            // Card can't be clicked if it is not playable
            cardImage.disableProperty().bind(isPlayable.not());
            handPane.getChildren().add(cardImage);
        }

        // Style
        handPane.setAlignment(Pos.CENTER);
        handPane.setStyle(
                "-fx-background-color: lightgray;-fx-spacing: 5px;-fx-padding: 5px;");

        return handPane;
    }

}
