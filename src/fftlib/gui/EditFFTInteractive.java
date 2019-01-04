package fftlib.gui;

import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;

public class EditFFTInteractive extends BorderPane {
    FFTManager fftManager;
    ListView<StackPane> lw;
    Node playBox;

    public EditFFTInteractive(Scene prevScene, FFTManager fftManager) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.fftManager = fftManager;
        Label title = new Label("Edit the FFT Interactively");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        playBox = fftManager.getInteractiveState(FFTManager.initialFFTState);
        setCenter(playBox);

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefWidth(350);
        showRuleGroups();
        BorderPane.setMargin(lw, new Insets(15));
        setRight(lw);


        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label label1 = new Label("Press the rules to edit them.");
        label1.setFont(Font.font("Verdana", 15));

        Label label2 = new Label("Press them again to go back to adding a new rule");
        label2.setFont(Font.font("Verdana", 15));

        Button back = new Button("Back");
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });
        BorderPane.setAlignment(back, Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(label1, label2, back);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);

    }

    private void showRuleGroups() {
        ObservableList<StackPane> rules = FXCollections.observableArrayList();
        ArrayList<Integer> rgIndices = new ArrayList<>();
        int rgIdx = 0;
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(i);
            Label rgLabel = new Label((i + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 14));
            StackPane rgPane = new StackPane();
            rgPane.setAlignment(Pos.CENTER);
            rgPane.getChildren().add(rgLabel);
            rgIndices.add(rgIdx);
            rules.add(rgPane);
            rgIdx++;
            for (int j = 0; j < rg.rules.size(); j++) {
                final int index = j;
                StackPane rulePane = new StackPane();
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r.printRule());
                rLabel.setFont(Font.font("Verdana", 10));
                rulePane.getChildren().add(rLabel);
                rules.add(rulePane);
                rulePane.setOnMouseClicked(event -> {
                    if (index == lw.getSelectionModel().getSelectedIndex()) {
                        setCenter(playBox);
                        lw.getSelectionModel().clearSelection();
                    } else {
                        r.
                    }

                });
                rgIdx++;
            }
        }
        lw.setItems(rules);

        // Disable rule group titles (unclickable)
        lw.setCellFactory(new Callback<ListView<StackPane>, ListCell<StackPane>>() {
            @Override
            public ListCell<StackPane> call(ListView<StackPane> param) {
                return new ListCell<StackPane>() {
                    @Override
                    protected void updateItem(StackPane item, boolean empty) {
                        super.updateItem(item, empty);
                        if (rgIndices.contains(getIndex()))
                            setDisable(true);
                        setGraphic(item);
                    }
                };
            }
        });
    }
}

