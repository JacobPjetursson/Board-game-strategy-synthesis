package kulibrat.gui;

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
import kulibrat.game.Controller;
import kulibrat.gui.Dialogs.RestartGameDialog;
import kulibrat.gui.menu.MenuPane;
import misc.Config;

import java.util.ArrayList;

public class NavPane extends VBox {
    private int buttonWidth = Config.WIDTH / 6 + 40;
    private ArrayList<Button> buttons;
    private Button startAIButton;
    private Button stopAIButton;
    private Button restartButton;
    private Button menuButton;
    private Button reviewButton;
    private Button editFFTButton;
    private Button addRuleFFTButton;
    private HBox automaticFFTBox;
    private CheckBox automaticFFT;
    private VBox FFTWidgets;
    private CheckBox helpHuman;
    private HBox helpHumanBox;
    private VBox AIWidgets;

    NavPane(Controller cont) {
        setMinWidth(Config.WIDTH / 3);
        setMaxWidth(Config.WIDTH / 3);
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

        reviewButton = new Button("Review Game");
        buttons.add(reviewButton);

        editFFTButton = new Button("Edit FFT");
        buttons.add(editFFTButton);

        addRuleFFTButton = new Button("Add state to FFT");
        buttons.add(addRuleFFTButton);

        automaticFFT = new CheckBox();
        automaticFFT.setPrefSize(20, 20);
        automaticFFT.setSelected(true);
        Label automaticLabel = new Label("Automatic FFT");
        automaticLabel.setFont(Font.font("Verdana", 20));
        automaticLabel.setPadding(new Insets(0, 0, 0, 5));
        automaticFFTBox = new HBox(automaticFFT, automaticLabel);
        automaticFFTBox.setAlignment(Pos.CENTER);

        FFTWidgets = new VBox(editFFTButton, addRuleFFTButton, automaticFFTBox);
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
        stage.setScene(new Scene(new MenuPane(getScene()), Config.WIDTH, Config.HEIGHT));
    }

    public void addAIWidgets() {
        getChildren().add(AIWidgets);
    }

    public void addHelpHumanBox() {
        getChildren().add(helpHumanBox);
    }

    public void removeWidgets() {
        getChildren().remove(helpHumanBox);
        getChildren().remove(AIWidgets);
        getChildren().remove(reviewButton);
        getChildren().remove(FFTWidgets);
    }

    public void addReviewButton() {
        getChildren().add(reviewButton);
    }

    public void addFFTWidgets() {
        getChildren().add(FFTWidgets);
    }

    public Button getEditFFTButton() {
        return editFFTButton;
    }

    public Button getAddRuleFFTButton() {
        return addRuleFFTButton;
    }

    public CheckBox getAutomaticFFTBox() {
        return automaticFFT;
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

    public CheckBox getHelpHumanBox() {
        return helpHuman;
    }

    public Button getReviewButton() {
        return reviewButton;
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
