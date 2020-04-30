package fftlib.gui;

import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import static misc.Config.SHOW_RULE_GROUPS;
import static misc.Globals.PLAYER_ANY;


public class FFTFailurePane extends BorderPane {
    private ListView<VBox> lw;
    FFTInteractivePane interactivePane;
    int ROW_SIZE = 29;

    public FFTFailurePane(Scene prevScene, FFTInteractivePane interactivePane) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.interactivePane = interactivePane;
        Label title = new Label("First encountered node where the strategy chose a sub-optimal move");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        Node playBox = FFTManager.getFailNode();

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefWidth(520);
        lw.setSelectionModel(new NoSelectionModel<>());
        showRuleGroups();
        BorderPane.setMargin(lw, new Insets(15));
        HBox centerBox = new HBox(playBox, lw);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(10);
        setCenter(centerBox);


        VBox bottomBox = new VBox(8);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label colorInfoLabel = new Label("Green are the optimal moves, blue is the move chosen by the strategy");
        colorInfoLabel.setFont(Font.font("Verdana", 16));

        String moveInfo;
        if (FFTManager.currFFT.failingPoint.random)
            moveInfo = "The strategy did not apply to the above node, and a random, losing move existed";
        else
            moveInfo = "The strategy applied to the above node, but the move it chose was sub-optimal";
        Label moveInfoLabel = new Label(moveInfo);
        moveInfoLabel.setFont(Font.font("Verdana", 16));

        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 16));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });

        Button addRuleInteractiveBtn = new Button("Add rule for this node");
        addRuleInteractiveBtn.setFont(Font.font("Verdana", 16));

        // add rule to FFT button
        addRuleInteractiveBtn.setOnAction(event -> {
            Stage stage = (Stage) getScene().getWindow();
            FFTNode node = FFTManager.currFFT.failingPoint.getNode();
            FFTMove move = FFTManager.currFFT.failingPoint.getMove();
            stage.setScene(interactivePane.getScene());
            interactivePane.update(node, move);
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
        int counter = 0;
        for (int i = 0; i < FFTManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = FFTManager.currFFT.ruleGroups.get(i);
            VBox rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER);
            Label rgLabel = new Label((i + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 18));
            if (SHOW_RULE_GROUPS) rgVBox.getChildren().add(rgLabel);
            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r);
                rLabel.setFont(Font.font("Verdana", 15));
                // TODO - below is hacky
                FFTMove failMove = FFTManager.currFFT.failingPoint.getMove();
                FFTNode failNode = FFTManager.currFFT.failingPoint.getNode();
                int tempTeam = failMove.getTeam();
                failMove.setTeam(PLAYER_ANY);
                if (!ruleApplied) {
                    FFTMove ruleMove = r.apply(failNode.convert()).convert();
                    if (ruleMove != null) {
                        ruleMove.setTeam(PLAYER_ANY);
                        if (ruleMove.equals(failMove)) {
                            rLabel.setTextFill(Color.BLUE);
                            ruleApplied = true;
                        }
                    }
                }
                failMove.setTeam(tempTeam);
                rgVBox.getChildren().add(rLabel);
                counter++;
            }
            ruleGroups.add(rgVBox);
        }
        lw.setItems(ruleGroups);
        lw.setMaxHeight(counter * ROW_SIZE);
    }
}
