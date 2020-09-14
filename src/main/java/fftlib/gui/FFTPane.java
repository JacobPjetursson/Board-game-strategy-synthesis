package fftlib.gui;


import fftlib.logic.rule.Rule;
import fftlib.logic.rule.MetaRule;
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

import static fftlib.FFTManager.currFFT;
import static misc.Config.SYMMETRY_DETECTION;

public class FFTPane extends VBox {
    Label title;
    FFTNode node;
    private ListView<VBox> lw;
    private static final int FONTSIZE_TITLE = 28; // 18 is default
    private static final int FONTSIZE_RULE = 24; // 13 is default
    private static final int FONTSIZE_METARULE = 28; // 16 is default
    private static final double RULE_SIZE = 40; // 29 is default
    private static final double METARULE_LABEL_SIZE = 46; // 32 is default
    private boolean ruleApplied;
    private double size;

    public FFTPane() {
        setAlignment(Pos.CENTER);

        title = new Label(currFFT.getName());
        title.setFont(Font.font("Verdana", FontWeight.BOLD, FONTSIZE_TITLE));
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

    public void update(FFTNode node) {
        this.node = node;
        refresh();
    }

    public void refresh() {
        title.setText(currFFT.getName());
        ObservableList<VBox> rules = FXCollections.observableArrayList();
        ruleApplied = false;

        size = 0;
        int i = 0;
        while (i < currFFT.getRules().size()) {
            // check if any metarule starts at index i
            for (MetaRule mr : currFFT.getMetaRules()) {
                if (i == mr.startIdx && i < currFFT.size()) {
                    rules.add(getMetaRuleBox(mr));
                    while (i < mr.endIdx && i < currFFT.size()) {
                        Rule r = currFFT.getRules().get(i);
                        rules.add(getRuleBox(r, i, true));
                        i++;
                    }
                }
            }
            if (i >= currFFT.size())
                break;
            Rule r = currFFT.getRules().get(i);
            rules.add(getRuleBox(r, i, false));
            i++;
        }
        // account for corner case where metarule is empty at end of rules
        for (MetaRule mr : currFFT.getMetaRules()) {
            if (mr.startIdx >= currFFT.size()) {
                rules.add(getMetaRuleBox(mr));
            }
        }

        lw.setItems(rules);
        lw.setPrefHeight(size);
    }

    public VBox getRuleBox(Rule r, int index, boolean inline) {
        VBox ruleBox = new VBox(10);
        String pfx = (inline) ? "   " : "";
        Label rLabel = new Label(pfx + (index+1) + ": " + r);
        rLabel.setFont(Font.font("Verdana", FONTSIZE_RULE));
        HashSet<FFTMove> moves = r.apply(node);
        if (!ruleApplied && !moves.isEmpty()) {
            rLabel.setTextFill(Color.BLUE);
            ruleApplied = true;
        }
        ruleBox.getChildren().add(rLabel);
        size += RULE_SIZE;
        return ruleBox;
    }

    public VBox getMetaRuleBox(MetaRule mr) {
        Label rgLabel = new Label(mr.name);
        rgLabel.setFont(Font.font("Verdana", FONTSIZE_METARULE));
        rgLabel.setTextFill(Color.GRAY);

        VBox rgLabelBox = new VBox(10);
        rgLabelBox.getChildren().add(rgLabel);
        size += METARULE_LABEL_SIZE;
        return rgLabelBox;
    }
}
