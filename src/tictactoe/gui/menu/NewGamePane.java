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
    private TextField crossDelayField;
    private TextField circleDelayField;
    private HBox crossDelayBox;
    private HBox PLAYER1DelayBox;
    private ChoiceBox<String> crossChoices;
    private ChoiceBox<String> circleChoices;
    private VBox finalBox;
    private Label AIDelayLabelCross;
    private Label AIDelayLabelCircle;

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

        crossChoices = new ChoiceBox<String>();
        crossChoices.setValue(human);
        crossChoices.setItems(FXCollections.observableArrayList(human, fft, lookup));
        crossChoices.setMinWidth(choiceWidth);
        crossChoices.setMaxWidth(choiceWidth);
        crossChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!crossChoices.getItems().get((Integer) newValue).equals(human)) {
                if (!finalBox.getChildren().contains(crossDelayBox)) {
                    finalBox.getChildren().add(1, crossDelayBox);
                }
            }
            if (crossChoices.getItems().get((Integer) newValue).equals(fft) ||
                    crossChoices.getItems().get((Integer) newValue).equals(lookup))
                AIDelayLabelCross.setText("AI Move delay in ms");

        });

        Label crossLabel = new Label("Cross");
        crossLabel.setFont(Font.font("Verdana", 15));
        crossLabel.setPadding(new Insets(0, 10, 0, 0));
        crossLabel.setTextFill(Color.WHITE);
        HBox cross = new HBox(crossLabel, crossChoices);
        cross.setAlignment(Pos.CENTER);

        circleChoices = new ChoiceBox<String>();
        circleChoices.setValue(human);
        circleChoices.setItems(FXCollections.observableArrayList(human, fft, lookup));
        circleChoices.setMinWidth(choiceWidth);
        circleChoices.setMaxWidth(choiceWidth);
        circleChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!circleChoices.getItems().get((Integer) newValue).equals(human)) {
                if (!finalBox.getChildren().contains(PLAYER1DelayBox)) {
                    int index = finalBox.getChildren().contains(crossDelayBox) ? 3 : 2;
                    finalBox.getChildren().add(index, PLAYER1DelayBox);
                }
            }
            if (circleChoices.getItems().get((Integer) newValue).equals(fft) ||
                    circleChoices.getItems().get((Integer) newValue).equals(lookup))
                AIDelayLabelCircle.setText("AI Move delay in ms");

        });

        Label circleLabel = new Label("Circle (first)");
        circleLabel.setFont(Font.font("Verdana", 15));
        circleLabel.setPadding(new Insets(0, 10, 0, 0));
        circleLabel.setTextFill(Color.WHITE);
        HBox circle = new HBox(circleLabel, circleChoices);
        circle.setAlignment(Pos.CENTER);

        crossDelayField = new TextField("1000");
        crossDelayField.setMinWidth(textFieldWidth);
        crossDelayField.setMaxWidth(textFieldWidth);
        crossDelayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                crossDelayField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.isEmpty()) {
                crossDelayField.setText(newValue.replaceAll("", "0"));
            }
        });
        crossDelayField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                this.requestFocus();
            }
        });
        AIDelayLabelCross = new Label();
        AIDelayLabelCross.setFont(Font.font("Verdana", 15));
        AIDelayLabelCross.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelCross.setTextFill(Color.WHITE);
        AIDelayLabelCross.setAlignment(Pos.CENTER);
        crossDelayBox = new HBox(AIDelayLabelCross, crossDelayField);
        crossDelayBox.setAlignment(Pos.CENTER);
        circleDelayField = new TextField("1000");
        circleDelayField.setMinWidth(textFieldWidth);
        circleDelayField.setMaxWidth(textFieldWidth);
        circleDelayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                circleDelayField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.isEmpty()) {
                circleDelayField.setText(newValue.replaceAll("", "0"));
            }
        });
        circleDelayField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                this.requestFocus();
            }
        });

        AIDelayLabelCircle = new Label();
        AIDelayLabelCircle.setFont(Font.font("Verdana", 15));
        AIDelayLabelCircle.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelCircle.setTextFill(Color.WHITE);
        AIDelayLabelCircle.setAlignment(Pos.CENTER);
        PLAYER1DelayBox = new HBox(AIDelayLabelCircle, circleDelayField);
        PLAYER1DelayBox.setAlignment(Pos.CENTER);

        Button startGame = new Button("Start Game");
        startGame.setMinWidth(Config.WIDTH / 4);
        startGame.setOnMouseClicked(event -> {
            String crossValue = crossChoices.getValue();
            String circleValue = circleChoices.getValue();
            Stage stage = (Stage) getScene().getWindow();

            int playerCrossMode = (crossValue.equals(human)) ? Config.HUMAN :
                    (crossValue.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            int playerCircleMode = (circleValue.equals(human)) ? Config.HUMAN :
                    (circleValue.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            new Controller(stage, playerCircleMode,
                    playerCrossMode, new State(),
                    Integer.parseInt(circleDelayField.getText()),
                    Integer.parseInt(crossDelayField.getText()));
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

        finalBox = new VBox(cross, circle, btnBox);
        finalBox.setAlignment(Pos.CENTER);
        finalBox.setSpacing(30);

        AnchorPane.setTopAnchor(finalBox, 0.0);
        AnchorPane.setRightAnchor(finalBox, 0.0);
        AnchorPane.setLeftAnchor(finalBox, 0.0);
        AnchorPane.setBottomAnchor(finalBox, 0.0);

        getChildren().addAll(title, finalBox);
    }
}
