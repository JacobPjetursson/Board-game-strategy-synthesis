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
import misc.Config;
import tictactoe.game.Controller;
import tictactoe.gui.dialog.RestartGameDialog;
import tictactoe.gui.menu.MenuPane;

import java.util.ArrayList;

public class NavPane extends VBox {
    private int buttonWidth = Config.WIDTH / 6 + 20;
    private ArrayList<Button> buttons;
    private Button startAIButton;
    private Button stopAIButton;
    private Button restartButton;
    private Button menuButton;
    private Button editFFTButton;
    private Button addRuleFFTButton;
    private HBox interactiveFFTBox;
    private CheckBox interactiveFFT;
    private Button showFFTButton;
    private VBox FFTWidgets;
    private VBox AIWidgets;

    NavPane(Controller cont) {
        setMinWidth(Config.WIDTH / 3);
        setAlignment(Pos.CENTER);
        setSpacing(40);
        buttons = new ArrayList<Button>();
        restartButton = new Button("Restart Game");
        buttons.add(restartButton);
        restartButton.setOnMouseClicked(event -> restartGame(cont));

        startAIButton = new Button("Start AI vs. AI");
        buttons.add(startAIButton);

        stopAIButton = new Button("Stop AI vs. AI");
        buttons.add(stopAIButton);

        AIWidgets = new VBox(startAIButton, stopAIButton);
        AIWidgets.setSpacing(10);
        AIWidgets.setAlignment(Pos.CENTER);

        menuButton = new Button("Menu");
        buttons.add(menuButton);
        menuButton.setOnMouseClicked(event -> goToMenu());

        showFFTButton = new Button("Show FFT");
        buttons.add(showFFTButton);

        editFFTButton = new Button("Edit FFT");
        buttons.add(editFFTButton);

        addRuleFFTButton = new Button("Add Rule to FFT");
        buttons.add(addRuleFFTButton);

        interactiveFFT = new CheckBox();
        interactiveFFT.setSelected(true);
        Label interactiveLabel = new Label("Interactive FFT");
        interactiveLabel.setFont(Font.font("Verdana", 14));
        interactiveLabel.setPadding(new Insets(0, 0, 0, 5));
        interactiveFFTBox = new HBox(interactiveFFT, interactiveLabel);
        interactiveFFTBox.setAlignment(Pos.CENTER);

        FFTWidgets = new VBox(showFFTButton, editFFTButton, addRuleFFTButton, interactiveFFTBox);
        FFTWidgets.setSpacing(10);
        FFTWidgets.setAlignment(Pos.CENTER);

        for (Button button : buttons) {
            button.setMinWidth(buttonWidth);
            button.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
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
        stage.setScene(new Scene(new MenuPane(getScene()), Config.WIDTH, Config.HEIGHT));
    }

    public void addAIWidgets() {
        getChildren().add(AIWidgets);
    }


    public void removeWidgets() {
        getChildren().remove(AIWidgets);
        getChildren().remove(FFTWidgets);
    }

    public void addFFTWidgets() {
        getChildren().add(FFTWidgets);
    }

    public void addShowFFTButton() {
        getChildren().add(showFFTButton);
    }

    public Button getEditFFTButton() {
        return editFFTButton;
    }

    public Button getAddRuleFFTButton() {
        return addRuleFFTButton;
    }

    public CheckBox getInteractiveFFTBox() {
        return interactiveFFT;
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

    public Button getMenuButton() {
        return menuButton;
    }

    public Button getShowFFTButton() {
        return showFFTButton;
    }

    public boolean containsFFTWidgets() {
        return getChildren().contains(FFTWidgets);
    }

    public boolean containsAIWidgets() {
        return getChildren().contains(AIWidgets);
    }

    public boolean containsShowFFTButton() {
        return getChildren().contains(showFFTButton);
    }
}
