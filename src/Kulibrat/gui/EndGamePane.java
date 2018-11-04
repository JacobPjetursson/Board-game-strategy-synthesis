package gui;

import game.Controller;
import game.State;
import gui.menu.MenuPane;
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
import misc.Config;

import static misc.Config.*;

public class EndGamePane extends VBox {

    public EndGamePane(Stage primaryStage, int team, Controller cont) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        Label label = new Label();
        if (team == RED) label.setText("Congratulations player Red!");
        else label.setText(("Congratulations player Black!"));

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
            new Controller(primaryStage, cont.getPlayerInstance(RED),
                    cont.getPlayerInstance(BLACK), new State(), cont.getTime(RED), cont.getTime(BLACK), cont.getOverwriteDB());
        });
        restartGameBtn.setPrefWidth(110);
        hBox.getChildren().add(restartGameBtn);
        getChildren().add(hBox);

        Button reviewGameBtn = new Button("Review Game");
        reviewGameBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(new ReviewPane(primaryStage, cont), 325, Config.HEIGHT - 50));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(cont.getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();

        });
        reviewGameBtn.setPrefWidth(120);
        if (cont.getMode() == HUMAN_VS_AI)
            getChildren().add(reviewGameBtn);
    }
}
