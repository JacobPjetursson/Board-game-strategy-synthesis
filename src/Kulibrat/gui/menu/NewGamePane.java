package gui.menu;

import game.Controller;
import game.State;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

public class NewGamePane extends AnchorPane {
    private int choiceWidth = Config.WIDTH / 4;
    private int textFieldWidth = choiceWidth - 125;
    private String human = "Human";
    private String mcts = "Monte Carlo Tree Search";
    private String minimax = "Minimax";
    private String lookup = "Lookup Table";
    private String fft = "Fast and Frugal Tree";
    private TextField blackDelayField;
    private TextField redDelayField;
    private HBox blackDelayBox;
    private HBox redDelayBox;
    private ChoiceBox<String> playerBlackChoices;
    private ChoiceBox<String> playerRedChoices;
    private CheckBox overwriteDB;
    private VBox finalBox;
    private Label AIDelayLabelBlack;
    private Label AIDelayLabelRed;

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

        playerBlackChoices = new ChoiceBox<String>();
        playerBlackChoices.setValue(human);
        playerBlackChoices.setItems(FXCollections.observableArrayList(human, mcts, minimax, fft, lookup));
        playerBlackChoices.setMinWidth(choiceWidth);
        playerBlackChoices.setMaxWidth(choiceWidth);
        playerBlackChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!playerBlackChoices.getItems().get((Integer) newValue).equals(human)) {
                if (!finalBox.getChildren().contains(blackDelayBox)) {
                    finalBox.getChildren().add(1, blackDelayBox);
                }
                if (playerBlackChoices.getItems().get((Integer) newValue).equals(lookup) &&
                        !finalBox.getChildren().contains(overwriteDB)) {
                    int index = finalBox.getChildren().contains(redDelayBox) ? 4 : 3;
                    finalBox.getChildren().add(index, overwriteDB);
                } else if (!playerRedChoices.getValue().equals(lookup)) {
                    finalBox.getChildren().remove(overwriteDB);
                }
            } else {
                finalBox.getChildren().remove(blackDelayBox);
                if (!playerRedChoices.getValue().equals(lookup)) {
                    finalBox.getChildren().remove(overwriteDB);
                }
            }

            if (playerBlackChoices.getItems().get((Integer) newValue).equals(fft) ||
                    playerBlackChoices.getItems().get((Integer) newValue).equals(lookup))
                AIDelayLabelBlack.setText("AI Move delay in ms");
            else
                AIDelayLabelBlack.setText("AI Calculation time in ms");

        });

        Label playerBlackLabel = new Label("Player Black: ");
        playerBlackLabel.setFont(Font.font("Verdana", 15));
        playerBlackLabel.setPadding(new Insets(0, 10, 0, 0));
        playerBlackLabel.setTextFill(Color.WHITE);
        HBox playerBlack = new HBox(playerBlackLabel, playerBlackChoices);
        playerBlack.setAlignment(Pos.CENTER);

        playerRedChoices = new ChoiceBox<String>();
        playerRedChoices.setValue(human);
        playerRedChoices.setItems(FXCollections.observableArrayList(human, mcts, minimax, fft, lookup));
        playerRedChoices.setMinWidth(choiceWidth);
        playerRedChoices.setMaxWidth(choiceWidth);
        playerRedChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!playerRedChoices.getItems().get((Integer) newValue).equals(human)) {
                if (!finalBox.getChildren().contains(redDelayBox)) {
                    int index = finalBox.getChildren().contains(blackDelayBox) ? 3 : 2;
                    finalBox.getChildren().add(index, redDelayBox);
                }
                if (playerRedChoices.getItems().get((Integer) newValue).equals(lookup) &&
                        !finalBox.getChildren().contains(overwriteDB)) {
                    int index = finalBox.getChildren().contains(blackDelayBox) ? 4 : 3;
                    finalBox.getChildren().add(index, overwriteDB);
                } else if (!playerBlackChoices.getValue().equals(lookup)) {
                    finalBox.getChildren().remove(overwriteDB);
                }

            } else {
                finalBox.getChildren().remove(redDelayBox);
                if (!playerBlackChoices.getValue().equals(lookup)) {
                    finalBox.getChildren().remove(overwriteDB);
                }
            }
            if (playerRedChoices.getItems().get((Integer) newValue).equals(fft) ||
                    playerRedChoices.getItems().get((Integer) newValue).equals(lookup))
                AIDelayLabelRed.setText("AI Move delay in ms");
            else
                AIDelayLabelRed.setText("AI Calculation time in ms");

        });

        Label playerRedLabel = new Label("Player Red: ");
        playerRedLabel.setFont(Font.font("Verdana", 15));
        playerRedLabel.setPadding(new Insets(0, 10, 0, 0));
        playerRedLabel.setTextFill(Color.WHITE);
        HBox playerRed = new HBox(playerRedLabel, playerRedChoices);
        playerRed.setAlignment(Pos.CENTER);

        ChoiceBox<Integer> scoreLimitChoices = new ChoiceBox<>();
        scoreLimitChoices.setValue(5);
        scoreLimitChoices.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        scoreLimitChoices.setMinWidth(choiceWidth);
        scoreLimitChoices.setMaxWidth(choiceWidth);

        Label scoreLimitLabel = new Label("Score limit: ");
        scoreLimitLabel.setFont(Font.font("Verdana", 15));
        scoreLimitLabel.setPadding(new Insets(0, 10, 0, 0));
        scoreLimitLabel.setTextFill(Color.WHITE);
        HBox scoreLimitBox = new HBox(scoreLimitLabel, scoreLimitChoices);
        scoreLimitBox.setAlignment(Pos.CENTER);

        blackDelayField = new TextField("1000");
        blackDelayField.setMinWidth(textFieldWidth);
        blackDelayField.setMaxWidth(textFieldWidth);
        blackDelayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                blackDelayField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.isEmpty()) {
                blackDelayField.setText(newValue.replaceAll("", "0"));
            }
        });
        blackDelayField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                this.requestFocus();
            }
        });
        AIDelayLabelBlack = new Label();
        AIDelayLabelBlack.setFont(Font.font("Verdana", 15));
        AIDelayLabelBlack.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelBlack.setTextFill(Color.WHITE);
        AIDelayLabelBlack.setAlignment(Pos.CENTER);
        blackDelayBox = new HBox(AIDelayLabelBlack, blackDelayField);
        blackDelayBox.setAlignment(Pos.CENTER);
        redDelayField = new TextField("1000");
        redDelayField.setMinWidth(textFieldWidth);
        redDelayField.setMaxWidth(textFieldWidth);
        redDelayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                redDelayField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.isEmpty()) {
                redDelayField.setText(newValue.replaceAll("", "0"));
            }
        });
        redDelayField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                this.requestFocus();
            }
        });

        AIDelayLabelRed = new Label();
        AIDelayLabelRed.setFont(Font.font("Verdana", 15));
        AIDelayLabelRed.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelRed.setTextFill(Color.WHITE);
        AIDelayLabelRed.setAlignment(Pos.CENTER);
        redDelayBox = new HBox(AIDelayLabelRed, redDelayField);
        redDelayBox.setAlignment(Pos.CENTER);

        overwriteDB = new CheckBox("Overwrite Database\n (Takes a lot of time)");
        overwriteDB.setFont(Font.font("Verdana", 15));
        overwriteDB.setTextFill(Color.WHITE);

        Button startGame = new Button("Start Game");
        startGame.setMinWidth(Config.WIDTH / 4);
        startGame.setOnMouseClicked(event -> {
            String blackValue = playerBlackChoices.getValue();
            String redValue = playerRedChoices.getValue();
            Stage stage = (Stage) getScene().getWindow();

            int playerBlackMode = (blackValue.equals(human)) ? Config.HUMAN :
                    (blackValue.equals(minimax)) ? Config.MINIMAX :
                    (blackValue.equals(mcts)) ? Config.MONTE_CARLO :
                    (blackValue.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            int playerRedMode = (redValue.equals(human)) ? Config.HUMAN :
                    (redValue.equals(minimax)) ? Config.MINIMAX :
                    (redValue.equals(mcts)) ? Config.MONTE_CARLO :
                    (redValue.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            Config.SCORELIMIT = scoreLimitChoices.getValue();
            new Controller(stage, playerRedMode,
                    playerBlackMode, new State(),
                    Integer.parseInt(redDelayField.getText()),
                    Integer.parseInt(blackDelayField.getText()), overwriteDB.isSelected());
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

        finalBox = new VBox(playerBlack, playerRed, scoreLimitBox, btnBox);
        finalBox.setAlignment(Pos.CENTER);
        finalBox.setSpacing(30);

        AnchorPane.setTopAnchor(finalBox, 0.0);
        AnchorPane.setRightAnchor(finalBox, 0.0);
        AnchorPane.setLeftAnchor(finalBox, 0.0);
        AnchorPane.setBottomAnchor(finalBox, 0.0);

        getChildren().addAll(title, finalBox);
    }
}
