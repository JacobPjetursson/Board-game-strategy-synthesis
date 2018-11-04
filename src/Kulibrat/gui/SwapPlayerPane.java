package gui;

import game.Controller;
import gui.board.Player;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import misc.Config;

import static misc.Config.RED;


public class SwapPlayerPane extends AnchorPane {
    private String human = "Human";
    private String mcts = "Monte Carlo Tree Search";
    private String minimax = "Minimax";
    private String lookup = "Lookup Table";
    private String fft = "Fast and Frugal Tree";
    private ChoiceBox<String> playerChoices;
    private int choiceWidth = Config.WIDTH / 4;
    private VBox finalBox;

    private Label AIDelayLabel;
    private HBox delayBox;
    private TextField delayField;
    private int textFieldWidth = choiceWidth - 125;

    public SwapPlayerPane(Controller cont, Player currPlayer) {
        int team = currPlayer.getTeam();
        Label title = new Label("Swap Player " + ((team == RED) ? "RED" : "BLACK"));
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
        title.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(title, 20.0);
        AnchorPane.setRightAnchor(title, 0.0);
        AnchorPane.setLeftAnchor(title, 0.0);

        Label currPlayerLabel = new Label("Current Player: " + currPlayer.getTypeLabel().getText());
        currPlayerLabel.setFont(Font.font("Verdana", 15));
        Label newPlayerLabel = new Label("New Player: ");

        playerChoices = new ChoiceBox<String>();
        playerChoices.setValue(human);
        playerChoices.setItems(FXCollections.observableArrayList(human, mcts, minimax, fft, lookup));
        playerChoices.setMinWidth(choiceWidth);
        playerChoices.setMaxWidth(choiceWidth);
        playerChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!playerChoices.getItems().get((Integer) newValue).equals(human)) {
                if (!finalBox.getChildren().contains(delayBox)) {
                    finalBox.getChildren().add(2, delayBox);
                }
                if (playerChoices.getItems().get((Integer) newValue).equals(minimax) ||
                        playerChoices.getItems().get((Integer) newValue).equals(mcts)) {
                    AIDelayLabel.setText("AI Calculation time in ms");
                } else {
                    AIDelayLabel.setText("AI Move Delay in ms");
                }
            } else {
                finalBox.getChildren().remove(delayBox);
            }
        });

        AIDelayLabel = new Label();
        AIDelayLabel.setFont(Font.font("Verdana", 15));
        AIDelayLabel.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabel.setAlignment(Pos.CENTER);

        delayField = new TextField("1000");
        delayField.setMinWidth(textFieldWidth);
        delayField.setMaxWidth(textFieldWidth);
        delayField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                delayField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.isEmpty()) {
                delayField.setText(newValue.replaceAll("", "0"));
            }
        });
        delayField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                this.requestFocus();
            }
        });

        delayBox = new HBox(AIDelayLabel, delayField);
        delayBox.setAlignment(Pos.CENTER);

        Button accept = new Button("Accept");
        accept.setOnMouseClicked(event -> {
            String value = playerChoices.getValue();
            int playerMode = (value.equals(human)) ? Config.HUMAN :
                    (value.equals(minimax)) ? Config.MINIMAX :
                            (value.equals(mcts)) ? Config.MONTE_CARLO :
                                    (value.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            if (playerMode == Config.LOOKUP_TABLE) {
                cont.setOverwriteDB(false);
            }
            if (playerMode != Config.HUMAN) {
                int time = Integer.parseInt(delayField.getText());
                cont.setPlayerCalcTime(team, time);
            }
            currPlayer.setTypeLabelText(playerMode);
            cont.setPlayerInstance(team, playerMode);
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });

        Button cancel = new Button("Cancel");
        cancel.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });
        HBox newPlayerBox = new HBox(newPlayerLabel, playerChoices);
        newPlayerBox.setAlignment(Pos.CENTER);

        HBox btnBox = new HBox(accept, cancel);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(20);

        finalBox = new VBox(currPlayerLabel, newPlayerBox, btnBox);
        finalBox.setAlignment(Pos.CENTER);
        finalBox.setSpacing(30);

        AnchorPane.setTopAnchor(finalBox, 0.0);
        AnchorPane.setRightAnchor(finalBox, 0.0);
        AnchorPane.setLeftAnchor(finalBox, 0.0);
        AnchorPane.setBottomAnchor(finalBox, 0.0);

        getChildren().addAll(title, finalBox);
    }
}
