package tictactoe.gui;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import misc.Globals;
import tictactoe.game.Controller;
import tictactoe.gui.dialog.RestartGameDialog;
import tictactoe.gui.menu.MenuPane;

import java.util.ArrayList;

public class NavPane extends VBox {
    private int buttonWidth = Globals.WIDTH / 6 + 40;
    private ArrayList<Button> buttons;
    private Button startAIButton;
    private Button stopAIButton;
    private Button restartButton;
    private Button menuButton;
    private Button editFFTButton;
    private VBox FFTWidgets;
    private VBox AIWidgets;
    private CheckBox helpHuman;
    private HBox helpHumanBox;
    private Button reviewButton;

    NavPane(Controller cont) {
        setAlignment(Pos.CENTER);
        setSpacing(40);
        buttons = new ArrayList<>();
        restartButton = new Button("Restart Game");
        buttons.add(restartButton);
        restartButton.setOnMouseClicked(event -> restartGame(cont));

        startAIButton = new Button("Start AI vs. AI");
        buttons.add(startAIButton);

        stopAIButton = new Button("Stop AI vs. AI");
        buttons.add(stopAIButton);

        reviewButton = new Button("Review Game");
        buttons.add(reviewButton);

        AIWidgets = new VBox(startAIButton, stopAIButton);
        AIWidgets.setSpacing(10);
        AIWidgets.setAlignment(Pos.CENTER);

        helpHuman = new CheckBox();
        helpHuman.setPrefSize(20, 20);
        Label helpHumanLabel = new Label("Show perfect move");
        helpHumanLabel.setFont(Font.font("Verdana", 20));
        helpHumanLabel.setPadding(new Insets(0, 0, 0, 5));
        helpHumanBox = new HBox(helpHuman, helpHumanLabel);
        helpHumanBox.setAlignment(Pos.CENTER);

        menuButton = new Button("Menu");
        buttons.add(menuButton);
        menuButton.setOnMouseClicked(event -> goToMenu());

        editFFTButton = new Button("Edit FFT");
        buttons.add(editFFTButton);


        FFTWidgets = new VBox(editFFTButton);
        FFTWidgets.setSpacing(10);
        FFTWidgets.setAlignment(Pos.CENTER);

        for (Button button : buttons) {
            button.setMinWidth(buttonWidth);
            button.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            button.setFont(Font.font("Verdana", 20));
        }
        getChildren().addAll(restartButton, menuButton);
    }

    private void restartGame(Controller cont) {
        Stage prevStage = (Stage) getScene().getWindow();
        Stage newStage = new Stage();
        String labelText = "Are you sure you want to restart this game?";
        newStage.setScene(new Scene(new RestartGameDialog(labelText, prevStage, cont), 400, 150));
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(getScene().getWindow());
        newStage.setOnCloseRequest(Event::consume);
        newStage.show();
    }

    private void goToMenu() {
        Stage stage = (Stage) getScene().getWindow();
        stage.setScene(new Scene(new MenuPane(getScene()), Globals.WIDTH, Globals.HEIGHT));
    }

    public void addAIWidgets() {
        getChildren().add(AIWidgets);
    }


    public void removeWidgets() {
        getChildren().remove(helpHumanBox);
        getChildren().remove(AIWidgets);
        getChildren().remove(FFTWidgets);
        getChildren().remove(reviewButton);
    }

    public void addFFTWidgets() {
        getChildren().add(FFTWidgets);
    }

    public void addReviewButton() {
        getChildren().add(reviewButton);
    }

    public void addHelpHumanBox() {
        getChildren().add(helpHumanBox);
    }

    public Button getEditFFTButton() {
        return editFFTButton;
    }

    public Button getStartAIButton() {
        return startAIButton;
    }

    public Button getStopAIButton() {
        return stopAIButton;
    }

    public Button getRestartButton() {
        return restartButton;
    }

    public CheckBox getHelpHumanBox() {
        return helpHuman;
    }

    public Button getReviewButton() {
        return reviewButton;
    }

    public Button getMenuButton() {
        return menuButton;
    }

    public boolean containsFFTWidgets() {
        return getChildren().contains(FFTWidgets);
    }

    public boolean containsAIWidgets() {
        return getChildren().contains(AIWidgets);
    }

    public boolean containsHelpBox() {
        return getChildren().contains(helpHumanBox);
    }

    public boolean containsReviewButton() {
        return getChildren().contains(reviewButton);
    }
}
