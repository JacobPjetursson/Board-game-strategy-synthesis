package tictactoe.gui.menu;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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

import static misc.Config.*;

public class NewGamePane extends AnchorPane {
    private int choiceWidth = Config.WIDTH / 3;
    private String human = "Human";
    private String lookup = "Lookup Table";
    private String fft = "Fast and Frugal Tree";
    private ChoiceBox<String> crossChoices;
    private ChoiceBox<String> circleChoices;
    private VBox finalBox;

    VBox autogenBox;
    CheckBox autogenCheck;
    CheckBox autogenRandomCheck;
    VBox autogenExtraBox;

    NewGamePane() {
        setPrefSize(Config.WIDTH, Config.HEIGHT);
        setPadding(new Insets(30, 0, 0, 0));
        setStyle("-fx-background-color: black;");

        Label title = new Label("Game Options");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(title, -10.0);
        AnchorPane.setRightAnchor(title, 0.0);
        AnchorPane.setLeftAnchor(title, 0.0);

        crossChoices = new ChoiceBox<String>();
        crossChoices.setValue(human);
        crossChoices.setItems(FXCollections.observableArrayList(human, fft, lookup));
        crossChoices.setMinWidth(choiceWidth);
        crossChoices.setMaxWidth(choiceWidth);
        crossChoices.setStyle("-fx-font: 20px \"Verdana\";");
        crossChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            String val = crossChoices.getItems().get((Integer) newValue);
            if (val.equals(fft) && !finalBox.getChildren().contains(autogenBox)) {
                int idx = finalBox.getChildren().size() - 1;
                finalBox.getChildren().add(idx, autogenBox);
            }
            else if (!val.equals(fft) && !circleChoices.getValue().equals(fft))
                finalBox.getChildren().remove(autogenBox);

        });

        Label crossLabel = new Label("Cross (first)");
        crossLabel.setFont(Font.font("Verdana", 20));
        crossLabel.setPadding(new Insets(0, 10, 0, 0));
        crossLabel.setTextFill(Color.WHITE);
        HBox cross = new HBox(crossLabel, crossChoices);
        cross.setAlignment(Pos.CENTER);

        circleChoices = new ChoiceBox<>();
        circleChoices.setValue(human);
        circleChoices.setStyle("-fx-font: 20px \"Verdana\";");
        circleChoices.setItems(FXCollections.observableArrayList(human, fft, lookup));
        circleChoices.setMinWidth(choiceWidth);
        circleChoices.setMaxWidth(choiceWidth);
        circleChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            String val = circleChoices.getItems().get((Integer) newValue);
            if (val.equals(fft) && !finalBox.getChildren().contains(autogenBox)) {
                int idx = finalBox.getChildren().size() - 1;
                finalBox.getChildren().add(idx, autogenBox);
            }
            else if (!val.equals(fft) && !crossChoices.getValue().equals(fft))
                finalBox.getChildren().remove(autogenBox);
        });

        Label circleLabel = new Label("Circle");
        circleLabel.setFont(Font.font("Verdana", 20));
        circleLabel.setPadding(new Insets(0, 10, 0, 0));
        circleLabel.setTextFill(Color.WHITE);
        HBox circle = new HBox(circleLabel, circleChoices);
        circle.setAlignment(Pos.CENTER);

        // Autogen options
        autogenCheck = new CheckBox("Autogenerate FFT");
        autogenCheck.setAlignment(Pos.CENTER);
        autogenCheck.setTextFill(Color.WHITE);
        autogenCheck.setFont(Font.font("Verdana", 20));
        autogenCheck.setSelected(true);
        autogenCheck.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            Config.USE_AUTOGEN = newValue;

            if (newValue && !autogenBox.getChildren().contains(autogenExtraBox))
                autogenBox.getChildren().add(autogenExtraBox);
            else if (!newValue) {
                autogenBox.getChildren().remove(autogenExtraBox);
            }
        });

        ChoiceBox<String> autogenChoices = new ChoiceBox<>();
        autogenChoices.setValue("Cross");
        autogenChoices.setStyle("-fx-font: 20px \"Verdana\";");
        autogenChoices.setItems(FXCollections.observableArrayList("Cross", "Circle", "Both"));
        autogenChoices.setMinWidth(200);
        autogenChoices.setMaxWidth(200);
        autogenChoices.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            String val = autogenChoices.getItems().get(newValue.intValue());
            switch (val) {
                case "Cross":
                    Config.AUTOGEN_PERSPECTIVE = PLAYER1;
                    break;
                case "Circle":
                    Config.AUTOGEN_PERSPECTIVE = PLAYER2;
                    break;
                default:
                    Config.AUTOGEN_PERSPECTIVE = PLAYER_ANY;
                    break;
            }
        });

        Label autogenChoicesLabel = new Label("Choose perspective of autogenerated FFT");
        autogenChoicesLabel.setFont(Font.font("Verdana", 16));
        autogenChoicesLabel.setTextFill(Color.WHITE);

        HBox autogenChoiceBox = new HBox(10, autogenChoicesLabel, autogenChoices);
        autogenChoiceBox.setAlignment(Pos.CENTER);


        // Autogen options
        autogenRandomCheck = new CheckBox("Randomize rule ordering");
        autogenRandomCheck.setAlignment(Pos.CENTER);
        autogenRandomCheck.setTextFill(Color.WHITE);
        autogenRandomCheck.setFont(Font.font("Verdana", 16));
        autogenRandomCheck.setSelected(false);
        autogenRandomCheck.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            Config.RANDOM_RULE_ORDERING = newValue;
        });

        autogenExtraBox = new VBox(10, autogenChoiceBox, autogenRandomCheck);
        autogenExtraBox.setAlignment(Pos.CENTER);

        autogenBox = new VBox(10, autogenCheck, autogenExtraBox);
        autogenBox.setAlignment(Pos.CENTER);

        Button startGame = new Button("Start Game");
        startGame.setMinWidth(Config.WIDTH / 4);
        startGame.setFont(Font.font("Verdana", 20));
        startGame.setOnMouseClicked(event -> {
            String crossValue = crossChoices.getValue();
            String circleValue = circleChoices.getValue();
            Stage stage = (Stage) getScene().getWindow();

            int playerCrossMode = (crossValue.equals(human)) ? Config.HUMAN :
                    (crossValue.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            int playerCircleMode = (circleValue.equals(human)) ? Config.HUMAN :
                    (circleValue.equals(fft)) ? Config.FFT : Config.LOOKUP_TABLE;
            new Controller(stage, playerCrossMode,
                    playerCircleMode, new State());
        });

        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 20));
        back.setMinWidth(Config.WIDTH / 6);
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new MenuPane(),
                    Config.WIDTH, Config.HEIGHT));
        });

        HBox btnBox = new HBox(startGame, back);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(30);

        finalBox = new VBox(cross, circle, btnBox);
        finalBox.setAlignment(Pos.CENTER);
        finalBox.setSpacing(40);

        AnchorPane.setTopAnchor(finalBox, 0.0);
        AnchorPane.setRightAnchor(finalBox, 0.0);
        AnchorPane.setLeftAnchor(finalBox, 0.0);
        AnchorPane.setBottomAnchor(finalBox, 0.0);

        getChildren().addAll(title, finalBox);
    }
}
