package FFT;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditRuleGroupPane extends VBox {
    private int textFieldWidth = 150;
    private ListView<BorderPane> lw;
    private RuleGroup rg;
    private RuleGroup rg_changes;
    private Label titleLabel;

    public EditRuleGroupPane(RuleGroup rg, EditFFTScene editFFTScene) {
        this.rg = rg;
        rg_changes = new RuleGroup(this.rg);
        setSpacing(15);
        setAlignment(Pos.CENTER);
        titleLabel = new Label("Edit rule group:\n" + rg.name);
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setMinHeight(65);

        // Existing rule groups
        lw = new ListView<>();
        lw.setPickOnBounds(false);
        showRules();

        // New rule
        Label newRuleLabel = new Label("New Rule");
        newRuleLabel.setFont(Font.font("Verdana", 15));
        HBox newRuleBox = new HBox();
        newRuleBox.setAlignment(Pos.CENTER);
        Label label = new Label("IF ");
        newRuleBox.getChildren().add(label);
        TextField ruleField = new TextField();
        ruleField.setMinWidth(textFieldWidth);
        ruleField.setMaxWidth(textFieldWidth);
        newRuleBox.getChildren().add(ruleField);
        label = new Label("THEN ");
        newRuleBox.getChildren().add(label);
        TextField actionField = new TextField();
        actionField.setMinWidth(textFieldWidth);
        actionField.setMaxWidth(textFieldWidth);
        newRuleBox.getChildren().add(actionField);


        Button addRuleBtn = new Button("Add");
        addRuleBtn.setOnMouseClicked(event -> {
            String clauseStr = ruleField.getText();
            String actionStr = actionField.getText();
            if (!Rule.isValidRuleFormat(clauseStr, actionStr)) {
                System.err.println("Incorrect rule format! Please check how-to for parsing rules");
                return;
            }
            Rule r = new Rule(clauseStr, actionStr);
            rg_changes.rules.add(r);
            ruleField.clear();
            actionField.clear();
            showRules();
        });
        newRuleBox.getChildren().add(addRuleBtn);
        VBox ruleBox = new VBox(newRuleLabel, newRuleBox);
        ruleBox.setAlignment(Pos.CENTER);
        ruleBox.setSpacing(10);

        Button back = new Button("Cancel");
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });
        Button save = new Button("Save");
        save.setOnMouseClicked(event -> {
            rg.rules = rg_changes.rules;
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            editFFTScene.update();
            FFTManager.save();
        });
        HBox bottomBox = new HBox(10);
        VBox.setMargin(bottomBox, new Insets(10));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.getChildren().addAll(save, back);

        setVgrow(lw, Priority.ALWAYS);
        BorderPane titleBox = new BorderPane();
        // add rename button
        Button renameBtn = new Button("Rename");
        renameBtn.setAlignment(Pos.CENTER);
        renameBtn.setOnMouseClicked(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new RenameRGPane("Write a new rule group name", rg), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });
        // FIX ME AND MY SHITTY ALIGNING
        titleBox.setPadding(new Insets(10, 10, 0, 10));
        titleBox.setCenter(titleLabel);
        titleBox.setRight(renameBtn);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setAlignment(renameBtn, Pos.CENTER);
        getChildren().addAll(titleBox, lw, ruleBox, bottomBox);
    }

    private void showRules() {
        ObservableList<BorderPane> rules = FXCollections.observableArrayList();
        for (int i = 0; i < rg_changes.rules.size(); i++) {
            final int index = i; // FUCKING JAVA CANCER
            Rule r = rg_changes.rules.get(i);
            Label rLabel = new Label((i + 1) + ": " + r.printRule());
            rLabel.setFont(Font.font("Verdana", 11));

            // up/down list buttons
            int buttonSize = 100;
            VBox upDownButtons = new VBox();
            upDownButtons.setAlignment(Pos.CENTER);
            Button upButton = new Button("▲");
            Button downButton = new Button("▼");
            upButton.setMinWidth(50);
            downButton.setMinWidth(50);
            upButton.setOnMouseClicked(event -> {
                if (index == 0)
                    return;
                rg_changes.rules.remove(index);
                rg_changes.rules.add(index - 1, r);
                showRules();
                lw.getSelectionModel().select(index - 1);
            });
            downButton.setOnMouseClicked(event -> {
                if (index == rg_changes.rules.size() - 1)
                    return;
                rg_changes.rules.remove(index);
                rg_changes.rules.add(index + 1, r);
                showRules();
                lw.getSelectionModel().select(index + 1);
            });

            // Edit / Remove buttons
            VBox rgButtons = new VBox(10);
            rgButtons.setAlignment(Pos.CENTER);
            Button removeButton = new Button("Remove");
            removeButton.setStyle("-fx-border-color: #000000; -fx-background-color: #ff0000;");
            removeButton.setMinWidth(buttonSize);
            removeButton.setOnMouseClicked(event -> removeRule(index));

            BorderPane finalPane = new BorderPane();
            upDownButtons.getChildren().addAll(upButton, downButton);
            HBox allButtons = new HBox(removeButton, upDownButtons);
            allButtons.setAlignment(Pos.CENTER);
            allButtons.setSpacing(5);

            finalPane.setCenter(rLabel);
            finalPane.setRight(allButtons);
            rules.add(finalPane);
        }
        lw.setItems(rules);
        lw.getSelectionModel().selectLast();
    }

    private void removeRule(int index) {
        lw.getItems().remove(index);
        rg_changes.rules.remove(index);
        showRules();
        FFTManager.save();
    }

    public class RenameRGPane extends RenamePane {

        RuleGroup rg;
        RenameRGPane(String labelText, RuleGroup rg) {
            super(labelText);
            this.rg = rg;
        }
        @Override
        void setSaveBtnMouseClicked() {
            if (!tf.getText().replace(" ", "").isEmpty()) {
                rg.name = tf.getText();
                FFTManager.save();
                Stage stage = (Stage) getScene().getWindow();
                stage.close();
                titleLabel.setText("Edit rule group:\n" + tf.getText());
            }
        }
    }
}
