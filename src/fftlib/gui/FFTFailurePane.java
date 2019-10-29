package fftlib.gui;

import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import misc.Config;

import static misc.Config.PLAYER_ANY;


public class FFTFailurePane extends BorderPane {
    private FFTManager fftManager;
    private ListView<VBox> lw;
    FFTInteractivePane interactivePane;

    public FFTFailurePane(Scene prevScene, FFTManager fftManager, FFTInteractivePane interactivePane) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.fftManager = fftManager;
        this.interactivePane = interactivePane;
        Label title = new Label("This is the first state where the FFT failed");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        Node playBox = fftManager.getFailState();
        setCenter(playBox);

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefWidth(520);
        lw.setSelectionModel(new NoSelectionModel<>());
        showRuleGroups();
        BorderPane.setMargin(lw, new Insets(15));
        setRight(lw);


        VBox bottomBox = new VBox(8);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label colorInfoLabel = new Label("Green stands for the winning/non-losing moves, blue is the chosen move");
        colorInfoLabel.setFont(Font.font("Verdana", 16));

        String moveInfo;
        if (fftManager.currFFT.failingPoint.random)
            moveInfo = "The FFT did not apply to the above state, and a random, losing move existed";
        else
            moveInfo = "The FFT applied to the above state, but the move it chose was a losing move";
        Label moveInfoLabel = new Label(moveInfo);
        moveInfoLabel.setFont(Font.font("Verdana", 16));

        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 16));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });

        Button addRuleInteractiveBtn = new Button("Add state to FFT");
        addRuleInteractiveBtn.setFont(Font.font("Verdana", 16));

        // add rule to FFT button
        addRuleInteractiveBtn.setOnAction(event -> {
            Stage stage = (Stage) getScene().getWindow();
            FFTState state = fftManager.currFFT.failingPoint.getState();
            FFTMove move = fftManager.currFFT.failingPoint.getMove();
            stage.setScene(interactivePane.getScene());
            interactivePane.update(state, move);
            interactivePane.setPrevScene(getScene());
        });

        BorderPane.setAlignment(back, Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(colorInfoLabel, moveInfoLabel, addRuleInteractiveBtn, back);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
    }

    private void showRuleGroups() {
        ObservableList<VBox> ruleGroups = FXCollections.observableArrayList();
        boolean ruleApplied = false;
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(i);
            VBox rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER);
            Label rgLabel = new Label((i + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 18));
            rgVBox.getChildren().add(rgLabel);
            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r);
                rLabel.setFont(Font.font("Verdana", 13));
                // TODO - below is hacky
                FFTMove failMove = fftManager.currFFT.failingPoint.getMove();
                FFTState failState = fftManager.currFFT.failingPoint.getState();
                int tempTeam = failMove.getTeam();
                failMove.setTeam(PLAYER_ANY);
                if (!ruleApplied) {
                    if (r.multiRule) {
                        for (Rule rule : r.rules) {
                            FFTMove ruleMove = rule.apply(failState);
                            if (ruleMove == null)
                                continue;
                            ruleMove.setTeam(PLAYER_ANY);
                            if (ruleMove.equals(failMove)) {
                                rLabel.setTextFill(Color.BLUE);
                                ruleApplied = true;
                                break;
                            }
                        }
                    } else {
                        FFTMove ruleMove = r.apply(failState);
                        if (ruleMove != null) {
                            ruleMove.setTeam(PLAYER_ANY);
                            if (ruleMove.equals(failMove)) {
                                rLabel.setTextFill(Color.BLUE);
                                ruleApplied = true;
                            }

                        }

                    }
                }
                failMove.setTeam(tempTeam);
                rgVBox.getChildren().add(rLabel);
            }
            ruleGroups.add(rgVBox);
        }
        lw.setItems(ruleGroups);
    }
}
