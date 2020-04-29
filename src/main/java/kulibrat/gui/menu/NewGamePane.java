package kulibrat.gui.menu;

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
import kulibrat.game.Controller;
import kulibrat.game.Node;
import misc.Config;
import misc.Globals;

import static misc.Globals.*;


public class NewGamePane extends AnchorPane {
    private int choiceWidth = Globals.WIDTH / 3;
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

    private VBox autogenExtraBox;
    private VBox autogenBox;

    NewGamePane() {
        setPrefSize(Globals.WIDTH, Globals.HEIGHT);
        setPadding(new Insets(30, 0, 0, 0));
        setStyle("-fx-background-color: black;");

        Label title = new Label("Game Options");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(title, -10.0);
        AnchorPane.setRightAnchor(title, 0.0);
        AnchorPane.setLeftAnchor(title, 0.0);

        playerBlackChoices = new ChoiceBox<>();
        playerBlackChoices.setValue(human);
        playerBlackChoices.setItems(FXCollections.observableArrayList(human, mcts, minimax, fft, lookup));
        playerBlackChoices.setMinWidth(choiceWidth);
        playerBlackChoices.setMaxWidth(choiceWidth);
        playerBlackChoices.setStyle("-fx-font: 20px \"Verdana\";");
        playerBlackChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            String val = playerBlackChoices.getItems().get((Integer) newValue);
            if (!val.equals(human)) {
                if (!finalBox.getChildren().contains(blackDelayBox)) {
                    finalBox.getChildren().add(1, blackDelayBox);
                }
                if (val.equals(lookup) &&
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

            if (val.equals(fft) || val.equals(lookup))
                AIDelayLabelBlack.setText("AI Move delay in ms");
            else
                AIDelayLabelBlack.setText("AI Calculation time in ms");

            if (val.equals(fft) && !finalBox.getChildren().contains(autogenBox)) {
                int idx = finalBox.getChildren().size() - 1;
                finalBox.getChildren().add(idx, autogenBox);
            }
            else if (!val.equals(fft) && !playerRedChoices.getValue().equals(fft))
                finalBox.getChildren().remove(autogenBox);

        });

        Label playerBlackLabel = new Label("Player Black: ");
        playerBlackLabel.setFont(Font.font("Verdana", 20));
        playerBlackLabel.setPadding(new Insets(0, 10, 0, 0));
        playerBlackLabel.setTextFill(Color.WHITE);
        HBox playerBlack = new HBox(playerBlackLabel, playerBlackChoices);
        playerBlack.setAlignment(Pos.CENTER);

        playerRedChoices = new ChoiceBox<>();
        playerRedChoices.setValue(human);
        playerRedChoices.setItems(FXCollections.observableArrayList(human, mcts, minimax, fft, lookup));
        playerRedChoices.setMinWidth(choiceWidth);
        playerRedChoices.setMaxWidth(choiceWidth);
        playerRedChoices.setStyle("-fx-font: 20px \"Verdana\";");
        playerRedChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            String val = playerRedChoices.getItems().get((Integer) newValue);
            if (!val.equals(human)) {
                if (!finalBox.getChildren().contains(redDelayBox)) {
                    int index = finalBox.getChildren().contains(blackDelayBox) ? 3 : 2;
                    finalBox.getChildren().add(index, redDelayBox);
                }
                if (val.equals(lookup) &&
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
            if (val.equals(fft) || val.equals(lookup))
                AIDelayLabelRed.setText("AI Move delay in ms");
            else
                AIDelayLabelRed.setText("AI Calculation time in ms");

            if (val.equals(fft) && !finalBox.getChildren().contains(autogenBox)) {
                int idx = finalBox.getChildren().size() - 1;
                finalBox.getChildren().add(idx, autogenBox);
            }
            else if (!val.equals(fft) && !playerBlackChoices.getValue().equals(fft))
                finalBox.getChildren().remove(autogenBox);

        });

        Label playerRedLabel = new Label("Player Red (first): ");
        playerRedLabel.setFont(Font.font("Verdana", 20));
        playerRedLabel.setPadding(new Insets(0, 10, 0, 0));
        playerRedLabel.setTextFill(Color.WHITE);
        HBox playerRed = new HBox(playerRedLabel, playerRedChoices);
        playerRed.setAlignment(Pos.CENTER);

        ChoiceBox<Integer> scoreLimitChoices = new ChoiceBox<>();
        scoreLimitChoices.setValue(5);
        scoreLimitChoices.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        scoreLimitChoices.setMinWidth(choiceWidth);
        scoreLimitChoices.setMaxWidth(choiceWidth);
        scoreLimitChoices.setStyle("-fx-font: 20px \"Verdana\";");

        Label scoreLimitLabel = new Label("Score limit: ");
        scoreLimitLabel.setFont(Font.font("Verdana", 20));
        scoreLimitLabel.setPadding(new Insets(0, 10, 0, 0));
        scoreLimitLabel.setTextFill(Color.WHITE);
        HBox scoreLimitBox = new HBox(scoreLimitLabel, scoreLimitChoices);
        scoreLimitBox.setAlignment(Pos.CENTER);

        blackDelayField = new TextField("1000");
        blackDelayField.setMinWidth(textFieldWidth);
        blackDelayField.setMaxWidth(textFieldWidth);
        blackDelayField.setStyle("-fx-font: 20px \"Verdana\";");
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
        AIDelayLabelBlack.setFont(Font.font("Verdana", 20));
        AIDelayLabelBlack.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelBlack.setTextFill(Color.WHITE);
        AIDelayLabelBlack.setAlignment(Pos.CENTER);
        blackDelayBox = new HBox(AIDelayLabelBlack, blackDelayField);
        blackDelayBox.setAlignment(Pos.CENTER);
        redDelayField = new TextField("1000");
        redDelayField.setMinWidth(textFieldWidth);
        redDelayField.setMaxWidth(textFieldWidth);
        redDelayField.setStyle("-fx-font: 20px \"Verdana\";");
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
        AIDelayLabelRed.setFont(Font.font("Verdana", 20));
        AIDelayLabelRed.setPadding(new Insets(0, 10, 0, 0));
        AIDelayLabelRed.setTextFill(Color.WHITE);
        AIDelayLabelRed.setAlignment(Pos.CENTER);
        redDelayBox = new HBox(AIDelayLabelRed, redDelayField);
        redDelayBox.setAlignment(Pos.CENTER);

        overwriteDB = new CheckBox("Overwrite Database\n (Takes a lot of time)");
        overwriteDB.setFont(Font.font("Verdana", 20));
        overwriteDB.setTextFill(Color.WHITE);




        // Autogen options
        // autogen
        CheckBox autogenCheck = new CheckBox("Autogenerate FFT");
        autogenCheck.setAlignment(Pos.CENTER);
        autogenCheck.setTextFill(Color.WHITE);
        autogenCheck.setFont(Font.font("Verdana", 20));
        autogenCheck.setSelected(true);
        autogenCheck.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            Globals.ENABLE_AUTOGEN = newValue;

            if (newValue && !autogenBox.getChildren().contains(autogenExtraBox))
                autogenBox.getChildren().add(autogenExtraBox);
            else if (!newValue)
                autogenBox.getChildren().remove(autogenExtraBox);
        });

        ChoiceBox<String> autogenChoices = new ChoiceBox<>();
        autogenChoices.setValue("Red");
        autogenChoices.setStyle("-fx-font: 20px \"Verdana\";");
        autogenChoices.setItems(FXCollections.observableArrayList("Red", "Black", "Both"));
        autogenChoices.setMinWidth(200);
        autogenChoices.setMaxWidth(200);
        autogenChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            String val = autogenChoices.getItems().get(newValue.intValue());
            switch (val) {
                case "Red":
                    Config.AUTOGEN_TEAM = PLAYER1;
                    break;
                case "Black":
                    Config.AUTOGEN_TEAM = PLAYER2;
                    break;
                default:
                    Config.AUTOGEN_TEAM = PLAYER_ANY;
                    break;
            }
        });

        Label autogenChoicesLabel = new Label("Choose perspective of autogenerated FFT");
        autogenChoicesLabel.setFont(Font.font("Verdana", 16));
        autogenChoicesLabel.setPadding(new Insets(0, 10, 0, 0));
        autogenChoicesLabel.setTextFill(Color.WHITE);

        HBox autogenChoiceBox = new HBox(autogenChoicesLabel, autogenChoices);
        autogenChoiceBox.setAlignment(Pos.CENTER);

        // Autogen options
        CheckBox autogenRandomCheck = new CheckBox("Randomize rule ordering");
        autogenRandomCheck.setAlignment(Pos.CENTER);
        autogenRandomCheck.setTextFill(Color.WHITE);
        autogenRandomCheck.setFont(Font.font("Verdana", 16));
        autogenRandomCheck.setSelected(false);
        autogenRandomCheck.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            Config.RULE_ORDERING = (newValue) ? RULE_ORDERING_RANDOM : RULE_ORDERING_TERMINAL_FIRST;
        });

        CheckBox autogenMinimizeCheck = new CheckBox("Minimize rule preconditions");
        autogenMinimizeCheck.setAlignment(Pos.CENTER);
        autogenMinimizeCheck.setTextFill(Color.WHITE);
        autogenMinimizeCheck.setFont(Font.font("Verdana", 16));
        autogenMinimizeCheck.setSelected(true);
        autogenMinimizeCheck.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            Config.MINIMIZE_PRECONDITIONS = newValue;
        });

        autogenExtraBox = new VBox(10, autogenChoiceBox, autogenRandomCheck, autogenMinimizeCheck);
        autogenExtraBox.setAlignment(Pos.CENTER);

        autogenBox = new VBox(10, autogenCheck, autogenExtraBox);
        autogenBox.setAlignment(Pos.CENTER);

        Button startGame = new Button("Start Game");
        startGame.setFont(Font.font("Verdana", 20));
        startGame.setMinWidth(Globals.WIDTH / 4);
        startGame.setOnMouseClicked(event -> {
            String blackValue = playerBlackChoices.getValue();
            String redValue = playerRedChoices.getValue();
            Stage stage = (Stage) getScene().getWindow();

            int playerBlackMode = (blackValue.equals(human)) ? Globals.HUMAN :
                    (blackValue.equals(minimax)) ? Globals.MINIMAX :
                            (blackValue.equals(mcts)) ? Globals.MONTE_CARLO :
                                    (blackValue.equals(fft)) ? Globals.FFT : Globals.LOOKUP_TABLE;
            int playerRedMode = (redValue.equals(human)) ? Globals.HUMAN :
                    (redValue.equals(minimax)) ? Globals.MINIMAX :
                            (redValue.equals(mcts)) ? Globals.MONTE_CARLO :
                                    (redValue.equals(fft)) ? Globals.FFT : Globals.LOOKUP_TABLE;
            Globals.SCORELIMIT = scoreLimitChoices.getValue();
            if (playerBlackMode != Globals.FFT && playerRedMode != Globals.FFT)
                Globals.ENABLE_AUTOGEN = false;

            new Controller(stage, playerRedMode,
                    playerBlackMode, new Node(),
                    Integer.parseInt(redDelayField.getText()),
                    Integer.parseInt(blackDelayField.getText()), overwriteDB.isSelected());
        });

        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 20));
        back.setMinWidth(Globals.WIDTH / 6);
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new MenuPane(),
                    Globals.WIDTH, Globals.HEIGHT));
        });

        HBox btnBox = new HBox(startGame, back);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(30);

        finalBox = new VBox(playerBlack, playerRed, scoreLimitBox, btnBox);
        finalBox.setAlignment(Pos.CENTER);
        finalBox.setSpacing(25);

        AnchorPane.setTopAnchor(finalBox, 0.0);
        AnchorPane.setRightAnchor(finalBox, 0.0);
        AnchorPane.setLeftAnchor(finalBox, 0.0);
        AnchorPane.setBottomAnchor(finalBox, 0.0);

        getChildren().addAll(title, finalBox);
    }
}
