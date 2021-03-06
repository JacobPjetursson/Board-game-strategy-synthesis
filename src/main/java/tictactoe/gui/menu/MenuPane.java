package tictactoe.gui.menu;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import misc.Globals;

import java.util.ArrayList;

public class MenuPane extends AnchorPane {
    private ArrayList<Button> buttons;
    private Button continueGame;
    private Application app;

    public MenuPane() {
        setPrefSize(Globals.WIDTH, Globals.HEIGHT);
        setPadding(new Insets(30, 0, 0, 0));
        setStyle("-fx-background-color: black;");

        buttons = new ArrayList<>();

        Label title = new Label("TicTacToe");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(title, 0.0);
        AnchorPane.setRightAnchor(title, 0.0);
        AnchorPane.setLeftAnchor(title, 0.0);

        Button newGame = new Button("New Game");
        buttons.add(newGame);
        newGame.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new NewGamePane(), Globals.WIDTH, Globals.HEIGHT));
        });

        continueGame = new Button("Continue Game");
        buttons.add(continueGame);
        continueGame.setDisable(true);

        Button exit = new Button("Exit");
        buttons.add(exit);
        exit.setOnMouseClicked(event -> System.exit(0));
        for (Button button : buttons) {
            button.setMinWidth(Globals.WIDTH / 4);
            button.setFont(Font.font("Verdana", 20));
        }
        VBox box = new VBox(newGame, continueGame, exit);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(40);
        AnchorPane.setTopAnchor(box, 0.0);
        AnchorPane.setRightAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
        AnchorPane.setBottomAnchor(box, 0.0);
        getChildren().addAll(title, box);
    }

    public MenuPane(Scene prevScene) {
        this();
        continueGame.setDisable(false);
        continueGame.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });
    }

    public MenuPane(Application app) {
        this();
        this.app = app;
    }
}
