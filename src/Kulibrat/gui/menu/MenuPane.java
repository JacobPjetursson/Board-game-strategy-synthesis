package gui.menu;

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
import misc.Config;

import java.util.ArrayList;

public class MenuPane extends AnchorPane {
    private ArrayList<Button> buttons;
    private Button continueGame;
    private Application app;

    public MenuPane() {
        setPrefSize(Config.WIDTH, Config.HEIGHT);
        setPadding(new Insets(30, 0, 0, 0));
        setStyle("-fx-background-color: black;");

        buttons = new ArrayList<>();

        Label title = new Label("Kulibrat");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(title, 40.0);
        AnchorPane.setRightAnchor(title, 0.0);
        AnchorPane.setLeftAnchor(title, 0.0);

        Button newGame = new Button("New Game");
        buttons.add(newGame);
        newGame.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new NewGamePane(), Config.WIDTH, Config.HEIGHT));
        });

        continueGame = new Button("Continue Game");
        buttons.add(continueGame);
        continueGame.setDisable(true);

        Button rules = new Button("Rules");
        buttons.add(rules);
        rules.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new RulesPane(), Config.WIDTH, Config.HEIGHT));

        });
        Button readme = new Button("README");
        buttons.add(readme);
        readme.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new ReadMePane(), Config.WIDTH, Config.HEIGHT));
        });
        Button exit = new Button("Exit");
        buttons.add(exit);
        exit.setOnMouseClicked(event -> System.exit(0));
        for (Button button : buttons) {
            button.setMinWidth(Config.WIDTH / 4);
        }
        VBox box = new VBox(newGame, continueGame, rules, readme, exit);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(30);
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
