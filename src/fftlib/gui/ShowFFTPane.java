package fftlib.gui;


import fftlib.FFT;
import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class ShowFFTPane extends VBox {
    Label title;
    FFT fft;
    FFTState state;
    private ListView<VBox> lw;

    public ShowFFTPane() {
        setAlignment(Pos.CENTER);
        title = new Label();
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefHeight(500);
        lw.setPrefWidth(500);


        lw.setSelectionModel(new NoSelectionModel<>());
        setMargin(lw, new Insets(10.0));

        getChildren().addAll(title, lw);
    }

    public void update(FFT fft, FFTState state) {
        this.fft = fft;
        this.state = state;
        title.setText(fft.name);
        showRuleGroups();
    }

    public void showRuleGroups() {
        ObservableList<VBox> ruleGroups = FXCollections.observableArrayList();
        boolean ruleApplied = false;
        for (int i = 0; i < fft.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = fft.ruleGroups.get(i);
            VBox rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER);
            Label rgLabel = new Label((i + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 18));
            rgVBox.getChildren().add(rgLabel);
            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r);
                rLabel.setFont(Font.font("Verdana", 13));
                FFTMove move = r.apply(state);
                if (!ruleApplied && move != null) {
                    rLabel.setTextFill(Color.BLUE);
                    ruleApplied = true;
                }

                rgVBox.getChildren().add(rLabel);
            }
            ruleGroups.add(rgVBox);
        }
        lw.setItems(ruleGroups);
    }
}
