package fftlib.gui;

import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import fftlib.game.FFTMove;
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


public class FFTFailurePane extends BorderPane {
    private FFTManager fftManager;
    private ListView<VBox> lw;

    public FFTFailurePane(Scene prevScene, FFTManager fftManager) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.fftManager = fftManager;
        Label title = new Label("This is the first encountered state where the FFT failed");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        Node playBox = fftManager.getFailState();
        setCenter(playBox);

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefWidth(350);
        lw.setSelectionModel(new NoSelectionModel<>());
        showRuleGroups();
        BorderPane.setMargin(lw, new Insets(15));
        setRight(lw);


        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label arrowInfoLabel = new Label("Green is for non-losing moves, blue is the chosen move");
        arrowInfoLabel.setFont(Font.font("Verdana", 15));

        String moveInfo;
        if (fftManager.currFFT.failingPoint.random)
            moveInfo = "The FFT did not apply to the above state, and a random, losing move existed";
        else
            moveInfo = "The FFT applied to the above state, but the move it chose was a losing move";
        Label moveInfoLabel = new Label(moveInfo);
        moveInfoLabel.setFont(Font.font("Verdana", 15));

        Button back = new Button("Back");
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });
        BorderPane.setAlignment(back, Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(arrowInfoLabel, moveInfoLabel, back);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
    }

    private void showRuleGroups() {
        ObservableList<VBox> ruleGroups = FXCollections.observableArrayList();
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(i);
            VBox rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER);
            Label rgLabel = new Label((i + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 16));
            rgVBox.getChildren().add(rgLabel);
            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r.printRule());
                rLabel.setFont(Font.font("Verdana", 10));
                // TODO - below is hacky
                FFTMove failMove = fftManager.currFFT.failingPoint.getMove();
                int tempTeam = failMove.getTeam();
                failMove.setTeam(-1);
                if (r.multiRule) {
                    for (Rule rule : r.rules) {
                        if (rule.action.getMove().equals(failMove)) {
                            rLabel.setTextFill(Color.BLUE);
                            break;
                        }
                    }
                }
                else if (r.action.getMove().equals(failMove))
                    rLabel.setTextFill(Color.BLUE);
                failMove.setTeam(tempTeam);
                rgVBox.getChildren().add(rLabel);
            }
            ruleGroups.add(rgVBox);
        }
        lw.setItems(ruleGroups);
    }
}
