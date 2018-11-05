package tictactoe.gui.menu;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import misc.Config;
import tictactoe.game.Controller;
import tictactoe.game.State;

public class NewGamePane extends AnchorPane {
    private int choiceWidth = Config.WIDTH / 4;
    private int textFieldWidth = choiceWidth - 125;
    private String human = "Human";
    private String lookup = "Lookup Table";
    private String fft = "Fast and Frugal Tree";
    private TextField PLAYER2DelayField;
    private TextField PLAYER1DelayField;
    private HBox PLAYER2DelayBox;
    private HBox PLAYER1DelayBox;
    private ChoiceBox<String> playerPLAYER2Choices;
    private ChoiceBox<String> playerPLAYER1Choices;
    private VBox finalBox;
    private Label AIDelayLabelPLAYER2;
    private Label AIDelayLabelPLAYER1;

    NewGamePane() {
        setPrefSize(Config.WIDTH, Config.HEIGHT);
        setPadding(new Insets(30, 0, 0, 0));
        setStyle("-fx-background-color: black;");

        Label title = new Label("Game Options");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(title, 40.0);
        AnchorPane.setRightAnchor(title, 0.0);
        AnchorPane.setLeftAnchor(title, 0.0);

        playerPLAYER2Choices = new ChoiceBox<String>();
        playerPLAYER2Choices.setValue(human);
        playerPLAYER2Choices.setItems(FXCollections.observableArrayList(human, fft, lookup));
        playerPLAYER2Choices.setMinWidth(choiceWidth);
        playerPLAYER2Choices.setMaxWidth(choiceWidth);
        playerPLAYER2Choices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!playerPLAYER2Choices.getItems().get((Integer) newValue).equals(human)) {
                if (!finalBox.getChildren().contains(PLAYER2DelayBox)) {
                    finalBox.getChildren().add(1, PLAYER2DelayBox);
                }
            }
            if (playerPLAYER2Choices.getItems().get((Integer) newValue).equals(fft) ||
                    playerPLAYER2Choices.getItems().get((Integer) newValue).equals(lookup))
                AIDelayLabelPLAYER2.setText("AI Move delay in ms");

        });

        Label playerPLAYER2Label = new Label("Player PLAYER2: ");
        playerPLAYER2Label.setFont(Font.font("Verdana", 15));
        playerPLAYER2Label.setPadding(new Insets(0, 10, 0, 0));
        playerPLAYER2Label.setTextFill(Color.WHITE);
        HBox playerPLAYER2 = new HBox(playerPLAYER2Label, playerPLAYER2Choices);
        playerPLAYER2.setAlignment(Pos.CENTER);

        playerPLAYER1Choices = new ChoiceBox<String>();
        playerPLAYER1Choices.setValue(human);
        playerPLAYER1Choices.setItems(FXCollections.observableArrayList(human, fft, lookup));
        playerPLAYER1Choices.setMinWidth(choiceWidth);
        playerPLAYER1Choices.setMaxWidth(choiceWidth);
        playerPLAYER1Choices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!playerPLAYER1Choices.getItems().get((Integer) newValue).equals(human)) {
                if (!finalBox.getChildren().contains(PLAYER1DelayBox)) {
                    int index = finalBox.getChildren().contains(PLAYER2DelayBox) ? 3 : 2;
                    finalBox.getChildren().add(index, PLAYER1DelayBox);
                }
            }
            if (playerPLAYER1Choices.getItems().get((Integer) newValue).equals(fft) ||
                    playerPLAYER1Choices.getItems().get((Integer) newValue).equals(lookup))
                AIDelayLabelPLAYER1.setText("AI Move delay in ms");

        });

        Label playerPLAYER1Label = new Label("Player PLAYER1: ");
        playerPLAYER1Label.setFont(Font.font("Verdana", 15));
        playerPLAYER1Label.setPadding(new Insets(0, 10, 0, 0));
        playerPLAYER1Label.setTextFill(Color.WHITE);
        HBox playerPLAYER1 = new HBox(playerPLAYER1Label, playerPLAYER1Choices);
        playerPLAYER1.setAlignment(Pos.CENTER);

        PLAYER2DelayField = new TextField("1000");
        PLAYER2DelayField.setMinWidth(textFieldWidth);
        PLAYER2DelayField.setMaxWidth(textFieldWidth);
        PLAYER2DelayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                PLAYER2DelayField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.isEmpty()) {
                PLAYER2DelayField.setText(newValue.replaceAll("", "0"));
            }
        });
        PLAYER2DelayField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                this.requestFocus();
            }
        });
        AIDelayLabelPLAYER2 = new Label();
        AIDelayLabelPLAYER2.setFont(Font.font("Verdana", 15));
        AIDelayLabelPLAYER2.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelPLAYER2.setTextFill(Color.WHITE);
        AIDelayLabelPLAYER2.setAlignment(Pos.CENTER);
        PLAYER2DelayBox = new HBox(AIDelayLabelPLAYER2, PLAYER2DelayField);
        PLAYER2DelayBox.setAlignment(Pos.CENTER);
        PLAYER1DelayField = new TextField("1000");
        PLAYER1DelayField.setMinWidth(textFieldWidth);
        PLAYER1DelayField.setMaxWidth(textFieldWidth);
        PLAYER1DelayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                PLAYER1DelayField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.isEmpty()) {
                PLAYER1DelayField.setText(newValue.replaceAll("", "0"));
            }
        });
        PLAYER1DelayField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                this.requestFocus();
            }
        });

        AIDelayLabelPLAYER1 = new Label();
        AIDelayLabelPLAYER1.setFont(Font.font("Verdana", 15));
        AIDelayLabelPLAYER1.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelPLAYER1.setTextFill(Color.WHITE);
        AIDelayLabelPLAYER1.setAlignment(Pos.CENTER);
        PLAYER1DelayBox = new HBox(AIDelayLabelPLAYER1, PLAYER1DelayField);
        PLAYER1DelayBox.setAlignment(Pos.CENTER);

        Button startGame = new Button("Start Game");
        startGame.setMinWidth(Config.WIDTH / 4);
        startGame.setOnMouseClicked(event -> {
            String PLAYER2Value = playerPLAYER2Choices.getValue();
            String PLAYER1Value = playerPLAYER1Choices.getValue();
            Stage stage = (Stage) getScene().getWindow();

            int playerBlackMode = (PLAYER2Value.equals(human)) ? Config.HUMAN :
                    (PLAYER2Value.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            int playerRedMode = (PLAYER1Value.equals(human)) ? Config.HUMAN :
                    (PLAYER1Value.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            new Controller(stage, playerRedMode,
                    playerBlackMode, new State(),
                    Integer.parseInt(PLAYER1DelayField.getText()),
                    Integer.parseInt(PLAYER2DelayField.getText()));
        });

        Button back = new Button("Back");
        back.setMinWidth(Config.WIDTH / 6);
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new MenuPane(),
                    Config.WIDTH, Config.HEIGHT));
        });

        HBox btnBox = new HBox(startGame, back);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(20);

        finalBox = new VBox(playerPLAYER2, playerPLAYER1, btnBox);
        finalBox.setAlignment(Pos.CENTER);
        finalBox.setSpacing(30);

        AnchorPane.setTopAnchor(finalBox, 0.0);
        AnchorPane.setRightAnchor(finalBox, 0.0);
        AnchorPane.setLeftAnchor(finalBox, 0.0);
        AnchorPane.setBottomAnchor(finalBox, 0.0);

        getChildren().addAll(title, finalBox);
    }
}
