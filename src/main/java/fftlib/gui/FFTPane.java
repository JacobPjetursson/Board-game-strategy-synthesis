package fftlib.gui;


import fftlib.logic.FFT;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.RuleEntity;
import fftlib.logic.rule.RuleGroup;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;

public class FFTPane extends VBox {
    Label title;
    FFT fft;
    FFTNode node;
    private ListView<VBox> lw;
    private static final double ROW_SIZE = 25.5;
    private boolean ruleApplied;

    public FFTPane() {
        setAlignment(Pos.CENTER);
        title = new Label();
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefHeight(500);
        lw.setPrefWidth(650);


        lw.setSelectionModel(new NoSelectionModel<>());
        setMargin(lw, new Insets(10.0));

        getChildren().addAll(title, lw);
    }

    public void update(FFT fft, FFTNode node) {
        this.fft = fft;
        this.node = node;
        title.setText(fft.name);
        show();
    }

    public void show() {
        ObservableList<VBox> ruleEntities = FXCollections.observableArrayList();
        int index = 0;
        boolean ruleApplied = false;
        for (RuleEntity re : fft.ruleEntities) {
            if (re instanceof RuleGroup) {
                // Rule group
                RuleGroup rg = (RuleGroup) re;
                VBox rgVBox = new VBox(10);
                rgVBox.setAlignment(Pos.CENTER_LEFT);
                Label rgLabel = new Label(rg.name);
                rgLabel.setFont(Font.font("Verdana", 18));
                rgVBox.getChildren().add(rgLabel);
                for (Rule r : rg.rules) {
                    Label rLabel = getRuleLabel(r, index++);
                    rgVBox.getChildren().add(rLabel);
                }
                ruleEntities.add(rgVBox);
            } else {
                Rule r = (Rule) re;
                VBox rVBox = new VBox(10);
                Label rLabel = getRuleLabel(r, index++);
                rVBox.getChildren().add(rLabel);
                ruleEntities.add(rVBox);
            }
        }
        lw.setItems(ruleEntities);
        int rows = 0;
        for (int i = 0; i < ruleEntities.size(); i++)
            rows += ruleEntities.get(0).getChildren().size() + 1;
        lw.setPrefHeight(rows * ROW_SIZE);
    }

    public Label getRuleLabel(Rule r, int index) {
        Label rLabel = new Label((index) + ": " + r);
        rLabel.setFont(Font.font("Verdana", 14));
        HashSet<FFTMove> moves = r.apply(node);
        if (!ruleApplied && !moves.isEmpty()) {
            rLabel.setTextFill(Color.BLUE);
            ruleApplied = true;
        }
        return rLabel;
    }
}
