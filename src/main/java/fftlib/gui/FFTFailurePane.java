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

import java.util.Set;

import static fftlib.FFTManager.currFFT;


public class FFTFailurePane extends BorderPane {
    FFTEditPane editPane;

    public FFTFailurePane(Scene prevScene, FFTEditPane editPane) {
        Set<FFTMove> chosenMoves = currFFT.apply(currFFT.getFailingPoint()).getMoves();
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.editPane = editPane;
        // set title
        Label title = new Label("First state where \nthe strategy chose a sub-optimal move");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        // set left part (failnode)
        Node playBox = FFTManager.getFailNode();

        // set right part (fft)
        FFTPane fftPane = new FFTPane();
        fftPane.update(currFFT.getFailingPoint());
        // combine
        HBox centerBox = new HBox(playBox, fftPane);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(10);
        setCenter(centerBox);

        // set bottom nodes (info and buttons)
        VBox bottomBox = new VBox(8);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));


        String moveInfo;
        if (chosenMoves.isEmpty())
            moveInfo = "The strategy is not defined on the above node,\nand a random, sub-optimal move exists";
        else
            moveInfo = "The strategy has chosen a sub-optimal move";
        Label moveInfoLabel = new Label(moveInfo);
        moveInfoLabel.setFont(Font.font("Verdana", 24));
        moveInfoLabel.setAlignment(Pos.CENTER);
        moveInfoLabel.setTextAlignment(TextAlignment.CENTER);

        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 18));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });

        Button addRuleInteractiveBtn = new Button("Add rule for this state");
        addRuleInteractiveBtn.setFont(Font.font("Verdana", 18));

        // add rule to FFT button
        addRuleInteractiveBtn.setOnAction(event -> {
            Stage stage = (Stage) getScene().getWindow();
            FFTNode node = currFFT.getFailingPoint();
            FFTMove move = FFTSolution.queryNode(node).move; // random optimal move
            stage.setScene(editPane.getScene());
            Rule r = new PropRule(node.convert(), move.convert());
            if (!chosenMoves.isEmpty()) {// insert new rule before the applied rule
                Rule appliedRule = currFFT.apply(node).getRule();
                int idx = currFFT.getRules().indexOf(appliedRule);
                editPane.addRule(idx, r);
            } else {
                editPane.appendRule(r);
            }
        });

        BorderPane.setAlignment(back, Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(moveInfoLabel, addRuleInteractiveBtn, back);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
    }
}
