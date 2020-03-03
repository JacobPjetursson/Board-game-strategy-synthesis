package tictactoe.gui;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import misc.Globals;
import tictactoe.game.Controller;
import tictactoe.game.State;
import tictactoe.gui.menu.MenuPane;

import static misc.Globals.*;


public class EndGamePane extends VBox {

    public EndGamePane(Stage primaryStage, int team, Controller cont) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        Label label = new Label();
        if (team == PLAYER1) label.setText("Congratulations to Cross!");
        else if (team == PLAYER2) label.setText(("Congratulations to Circle!"));
        else label.setText("It's a draw!");

        label.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        getChildren().add(label);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(40);
        Button menuBtn = new Button("Menu");
        menuBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            primaryStage.setScene(new Scene(new MenuPane(),
                    Globals.WIDTH, Globals.HEIGHT));
        });
        menuBtn.setPrefWidth(110);
        hBox.getChildren().add(menuBtn);
        Button restartGameBtn = new Button("Restart Game");
        restartGameBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            new Controller(primaryStage, cont.getPlayerInstance(PLAYER1),
                    cont.getPlayerInstance(PLAYER2), new State());
        });
        restartGameBtn.setPrefWidth(110);
        hBox.getChildren().add(restartGameBtn);
        getChildren().add(hBox);

        Button reviewGameBtn = new Button("Review Game");
        reviewGameBtn.setFont(Font.font("Verdana", 16));
        reviewGameBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(new ReviewPane(primaryStage, cont), 325, Globals.HEIGHT - 50));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(cont.getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();

        });
        reviewGameBtn.setPrefWidth(180);
        if (cont.getMode() == HUMAN_VS_AI)
            getChildren().add(reviewGameBtn);
    }
}
