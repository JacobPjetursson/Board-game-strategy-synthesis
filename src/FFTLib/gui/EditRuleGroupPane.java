package fftlib.gui;

import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static fftlib.FFTManager.SERIALIZED_MIME_TYPE;

public class EditRuleGroupPane extends VBox {
    private int textFieldWidth = 150;
    private ListView<RulePane> lw;
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
        lw.setCellFactory(param -> new RuleCell());
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
            Rule r = new Rule(clauseStr, actionStr);
            if (r.errors) {
                System.err.println("Incorrect rule format! Please check how-to for parsing rules");
                return;
            }
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
        // FIXME AND MY SHITTY ALIGNING
        titleBox.setPadding(new Insets(10, 10, 0, 10));
        titleBox.setCenter(titleLabel);
        titleBox.setRight(renameBtn);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setAlignment(renameBtn, Pos.CENTER);
        getChildren().addAll(titleBox, lw, ruleBox, bottomBox);
    }

    private void showRules() {
        ObservableList<RulePane> rules = FXCollections.observableArrayList();
        for (int i = 0; i < rg_changes.rules.size(); i++) {
            rules.add(new RulePane(i));
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

    private class RulePane extends BorderPane {

        int idx;

        RulePane(int idx) { // Rulegroup entry
            super();
            this.idx = idx;
            Rule r = rg_changes.rules.get(idx);
            Label rLabel = new Label((idx + 1) + ": " + r.print());
            rLabel.setFont(Font.font("Verdana", 11));

            // Remove button
            VBox rgButtons = new VBox(10);
            rgButtons.setAlignment(Pos.CENTER);
            Button removeButton = new Button("Remove");
            removeButton.setStyle("-fx-border-color: #000000; -fx-background-color: #ff0000;");
            removeButton.setMinWidth(100);
            removeButton.setOnMouseClicked(event -> removeRule(idx));

            setCenter(rLabel);
            setRight(removeButton);
        }
    }

    private class RuleCell extends ListCell<RulePane> {

        RuleCell() {
            ListCell thisCell = this;

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);

            setOnDragDetected(event -> {
                if (isEmpty())
                    return;
                Integer index = getIndex();
                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(SERIALIZED_MIME_TYPE, index);
                dragboard.setDragView(snapshot(null, null));
                dragboard.setContent(content);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasContent(SERIALIZED_MIME_TYPE)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasContent(SERIALIZED_MIME_TYPE)) {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasContent(SERIALIZED_MIME_TYPE)) {
                    setOpacity(1);
                }
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();

                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIdx = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    RulePane draggedPane = lw.getItems().remove(draggedIdx);
                    int droppedIdx = isEmpty() ? lw.getItems().size() : getIndex();

                    Rule r_dragged = rg_changes.rules.get(draggedPane.idx);
                    rg_changes.rules.remove(r_dragged);
                    rg_changes.rules.add(droppedIdx, r_dragged);
                    event.setDropCompleted(true);
                    showRules();
                    lw.getSelectionModel().select(droppedIdx);
                }
                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(RulePane item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                setGraphic(item);
            }
        }
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
