package tictactoe.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import misc.Config;
import tictactoe.game.Controller;
import tictactoe.game.State;
import tictactoe.gui.menu.MenuPane;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


public class EndGamePane extends VBox {

    public EndGamePane(Stage primaryStage, int team, Controller cont) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        Label label = new Label();
        if (team == PLAYER1) label.setText("Congratulations to Cross!");
        else if (team == PLAYER2) label.setText(("Congratulations to Circle!"));
        else label.setText("It's a draw!");

        label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        getChildren().add(label);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(40);
        Button menuBtn = new Button("Menu");
        menuBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            primaryStage.setScene(new Scene(new MenuPane(),
                    Config.WIDTH, Config.HEIGHT));
        });
        menuBtn.setPrefWidth(110);
        hBox.getChildren().add(menuBtn);
        Button restartGameBtn = new Button("Restart Game");
        restartGameBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            new Controller(primaryStage, cont.getPlayerInstance(PLAYER1),
                    cont.getPlayerInstance(PLAYER2), new State(), cont.getTime(PLAYER1), cont.getTime(PLAYER2));
        });
        restartGameBtn.setPrefWidth(110);
        hBox.getChildren().add(restartGameBtn);
        getChildren().add(hBox);
    }
}
