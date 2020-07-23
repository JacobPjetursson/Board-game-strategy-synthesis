package fftlib.gui;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.FFTSolution;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.Rule;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import static fftlib.FFTManager.currFFT;


public class FFTFailurePane extends BorderPane {
    FFTEditPane editPane;

    public FFTFailurePane(Scene prevScene, FFTEditPane editPane) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.editPane = editPane;
        // set title
        Label title = new Label("First encountered node where the strategy chose a sub-optimal move");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        // set left part (failnode)
        Node playBox = FFTManager.getFailNode();

        // set right part (fft)
        FFTPane fftPane = new FFTPane();
        fftPane.update(currFFT.getFailingPoint().getNode());
        // combine
        HBox centerBox = new HBox(playBox, fftPane);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(10);
        setCenter(centerBox);

        // set bottom nodes (info and buttons)
        VBox bottomBox = new VBox(8);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label colorInfoLabel = new Label("Green are the optimal moves, blue is the move chosen by the strategy");
        colorInfoLabel.setFont(Font.font("Verdana", 16));

        String moveInfo;
        if (currFFT.getFailingPoint().random)
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
            FFTNode node = currFFT.getFailingPoint().getNode();
            FFTMove move = FFTSolution.queryNode(node).move; // random optimal move
            stage.setScene(editPane.getScene());
            Rule r = new PropRule(node.convert(), move.convert());
            if (!currFFT.getFailingPoint().random) {// insert new rule before the applied rule
                Rule appliedRule = currFFT.getAppliedRule(node);
                int idx = currFFT.getRules().indexOf(appliedRule);
                editPane.addRule(idx, r);
            } else {
                editPane.appendRule(r);
            }
        });

        BorderPane.setAlignment(back, Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(colorInfoLabel, moveInfoLabel, addRuleInteractiveBtn, back);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
    }
}
