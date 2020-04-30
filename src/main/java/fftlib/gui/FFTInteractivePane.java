package fftlib.gui;

import fftlib.logic.FFT;
import fftlib.FFTManager;
import fftlib.logic.Rule;
import fftlib.logic.RuleGroup;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import misc.Globals;

import java.util.ArrayList;
import java.util.Stack;

import static fftlib.FFTManager.*;
import static misc.Config.SHOW_RULE_GROUPS;
import static misc.Globals.*;

public class FFTInteractivePane extends BorderPane {
    private int textFieldWidth = 150;
    private ListView<RulePane> lw;
    private interactiveFFTNode interactiveFFTNode;
    private Label ruleLabel;

    private Button saveRuleBtn;
    private Button deleteRuleBtn;
    private Button deleteRgBtn;
    private Button renameRgBtn;
    private Button saveToDiskBtn;
    private HBox ruleBox, ruleBtnBox, rgBtnBox;
    private HBox verifyBox;
    private VBox bottomBox;

    private Button verifyBtn;
    private Button undoBtn;
    private Stack<UndoItem> undoStack;
    private boolean changes;

    private Scene prevScene;
    private Runnable updateFunc;


    private int[] selectedIndices; // rgIdx, rIdx

    private static final int ROW_SIZE = 27;

    public FFTInteractivePane() {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setMaxHeight(Globals.HEIGHT);
        setMaxWidth(Globals.WIDTH);
        undoStack = new Stack<>();

        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);

        Label title = new Label("Edit the strategy interactively");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        topBox.getChildren().add(title);

        HBox ruleTitleBox = new HBox(2);
        ruleTitleBox.setAlignment(Pos.CENTER);
        Label ruleTitle = new Label("Rule: ");
        ruleTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        ruleTitle.setAlignment(Pos.CENTER);
        ruleTitle.setTextAlignment(TextAlignment.CENTER);
        ruleTitleBox.getChildren().add(ruleTitle);
        ruleLabel = new Label();
        ruleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        ruleLabel.setAlignment(Pos.CENTER);
        ruleLabel.setTextAlignment(TextAlignment.CENTER);
        ruleTitleBox.getChildren().add(ruleLabel);

        topBox.getChildren().add(ruleTitleBox);

        setTop(topBox);
        topBox.setMinHeight(65);
        BorderPane.setAlignment(topBox, Pos.CENTER);

        interactiveFFTNode = FFTManager.interactiveNode;

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefHeight(520);
        lw.setMinWidth(550);
        lw.setCellFactory(param -> new RuleCell());
        showRuleGroups();
        BorderPane.setMargin(lw, new Insets(15));

        bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label label1 = new Label("Click the rules to show them on the board");
        label1.setFont(Font.font("Verdana", 16));

        Label label2 = new Label("Click the board tiles to edit their contents");
        label2.setFont(Font.font("Verdana", 16));

        Label label3 = new Label("Set game perspective:");
        label3.setFont(Font.font("Verdana", 16));

        // VERIFICATION
        Label teamLabel = new Label(" as team: ");
        teamLabel.setFont(Font.font("Verdana", 15));
        ChoiceBox<String> teamChoice = new ChoiceBox<>();
        teamChoice.setMinWidth(textFieldWidth);
        teamChoice.setMaxWidth(textFieldWidth);
        teamChoice.setStyle("-fx-font: 16px \"Verdana\";");
        String[] playerNames = FFTManager.playerNames;
        teamChoice.setValue(playerNames[0]);
        teamChoice.setItems(FXCollections.observableArrayList(playerNames[0], playerNames[1], "Both"));

        ChoiceBox<String> verificationChoice = new ChoiceBox<>();
        verificationChoice.setMinWidth(textFieldWidth);
        verificationChoice.setMaxWidth(textFieldWidth);
        verificationChoice.setValue("Strongly");
        verificationChoice.setStyle("-fx-font: 16px \"Verdana\";");
        verificationChoice.setItems(FXCollections.observableArrayList("Strongly", "Weakly"));

        verifyBtn = new Button("Verify optimality");
        verifyBtn.setFont(Font.font("Verdana", 16));
        verifyBtn.setTooltip(new Tooltip("Checks if the current FFT is a winning strategy,\n" +
                "or if given rules are part of winning strategy"));
        verifyBtn.setOnMouseClicked(event -> {
            int team = teamChoice.getSelectionModel().getSelectedIndex() + 1;
            boolean wholeFFT = verificationChoice.getSelectionModel().getSelectedIndex() == 0;
            // todo
            //boolean verified =  FFTManager.currFFT.verify(team, wholeFFT);
            boolean verified = false;

            if (!verified) {
                if (FFTManager.currFFT.failingPoint == null) {
                    Label verifiedLabel = new Label("Always loses to perfect player");
                    verifiedLabel.setFont(Font.font("Verdana", 16));
                    getChildren().add(4, verifiedLabel);

                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.millis(2500),
                            ae -> getChildren().remove(verifiedLabel)));
                    timeline.play();
                } else {
                    Stage stage = (Stage) getScene().getWindow();
                    Scene scene = stage.getScene();
                    stage.setScene(new Scene(new FFTFailurePane(scene, this), WIDTH, Globals.HEIGHT));
                }
            } else {
                Label verifiedLabel = new Label("The strategy was successfully verified");
                verifiedLabel.setFont(Font.font("Verdana", 16));
                bottomBox.getChildren().add(verifiedLabel);

                Timeline timeline = new Timeline(new KeyFrame(
                        Duration.millis(10000),
                        ae -> bottomBox.getChildren().remove(verifiedLabel)));
                timeline.play();
            }
        });

        verifyBox = new HBox();
        verifyBox.setAlignment(Pos.CENTER);
        verifyBox.setSpacing(10);
        verifyBox.getChildren().addAll(verifyBtn, teamChoice, verificationChoice);

        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 16));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
            if (updateFunc != null) {
                updateFunc.run();
                updateFunc = null;
            }

            if (changes) {
                // Chance to save to disk
                Stage newStage = new Stage();
                newStage.setScene(new Scene(
                        new SaveFFTPane(), 500, 200));
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(getScene().getWindow());
                newStage.setOnCloseRequest(Event::consume);
                newStage.show();
            }
        });

        Button addRuleBtn = new Button("Add rule");
        addRuleBtn.setStyle(greenBtnStyle);
        addRuleBtn.setFont(Font.font("Verdana", 16));
        addRuleBtn.setOnMouseClicked(event -> {
            Rule r = interactiveFFTNode.getRule();
            pushUndoStack();
            ArrayList<RuleGroup> ruleGroups = FFTManager.currFFT.ruleGroups;
            if (ruleGroups.isEmpty())
                ruleGroups.add(new RuleGroup(""));
            Rule copy = new Rule(r);
            FFTManager.currFFT.ruleGroups.get(ruleGroups.size() - 1).addRule(copy);
            showRuleGroups();
        });

        Button addRgBtn = new Button("Add rule group");
        addRgBtn.setStyle(greenBtnStyle);
        addRgBtn.setFont(Font.font("Verdana", 16));
        addRgBtn.setOnMouseClicked(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameRGPane("Write name of new rule group", false), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        undoBtn = new Button("Undo");
        undoBtn.setDisable(true);
        undoBtn.setStyle(orangeBtnStyle);
        undoBtn.setFont(Font.font("Verdana", 16));
        undoBtn.setOnMouseClicked(event -> {
            if (undoStack.empty())
                return;
            UndoItem item = popUndoStack();
            FFTManager.currFFT = item.getFFT();
            showRuleGroups();
            lw.requestFocus();
            selectedIndices = item.getSelectedIndices();

            if (selectedIndices == null) {
                clearPane();
            } else {
                int rgIdx = selectedIndices[0];
                int rIdx = selectedIndices[1];
                int selectionIdx = rgIdx + rIdx + 1;
                lw.getSelectionModel().select(selectionIdx);
                RuleGroup rg = FFTManager.currFFT.ruleGroups.get(rgIdx);
                addInteractiveNode(interactiveFFTNode.getInteractiveNode(rg.rules.get(rIdx)));

            }
        });

        saveToDiskBtn = new Button("Save to disk");
        saveToDiskBtn.setStyle(blueBtnStyle);
        saveToDiskBtn.setFont(Font.font("Verdana", 16));
        saveToDiskBtn.setOnMouseClicked(event -> {
            FFTManager.save();
            playMsg("FFT successfully saved to disk", 3);
            changes = false;
        });

        Button minimizeBtn = new Button("Minimize");
        minimizeBtn.setFont(Font.font("Verdana", 16));
        minimizeBtn.setOnMouseClicked(event -> {

            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new MinimizeFFTPane(), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.show();
        });

        Button generateBtn = new Button("Synthesize");
        generateBtn.setFont(Font.font("Verdana", 16));
        generateBtn.setOnMouseClicked(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new GenerateFFTPane(), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.show();
        });

        saveRuleBtn = new Button("Save rule");
        saveRuleBtn.setStyle(blueBtnStyle);
        saveRuleBtn.setFont(Font.font("Verdana", 16));
        saveRuleBtn.setDisable(true);
        saveRuleBtn.setOnMouseClicked(event -> {
            Rule r = interactiveFFTNode.getRule();
            if (selectedIndices == null) {
                return;
            }
            pushUndoStack();

            int rgIdx = selectedIndices[0];
            int rIdx = selectedIndices[1];
            // Replace
            FFTManager.currFFT.ruleGroups.get(rgIdx).rules.remove(rIdx);
            FFTManager.currFFT.ruleGroups.get(rgIdx).rules.add(rIdx, r);
            showRuleGroups();

            playMsg("Rule saved", 3);
        });

        deleteRuleBtn = new Button("Delete rule");
        deleteRuleBtn.setStyle(redBtnStyle);
        deleteRuleBtn.setFont(Font.font("Verdana", 16));
        deleteRuleBtn.setDisable(true);
        deleteRuleBtn.setOnMouseClicked(event -> {
            pushUndoStack();
            int rgIdx = selectedIndices[0];
            int rIdx = selectedIndices[1];
            FFTManager.currFFT.ruleGroups.get(rgIdx).rules.remove(rIdx);
            clearPane();
            showRuleGroups();
        });

        Button clearRuleBtn = new Button("Clear");
        clearRuleBtn.setFont(Font.font("Verdana", 16));
        clearRuleBtn.setOnMouseClicked(event -> {
            clearPane();
        });

        renameRgBtn = new Button("Rename rule group");
        renameRgBtn.setFont(Font.font("Verdana", 16));
        renameRgBtn.setStyle(blueBtnStyle);
        renameRgBtn.setOnMouseClicked(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameRGPane("Rename rule group", true), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        deleteRgBtn = new Button("Delete rule group");
        deleteRgBtn.setFont(Font.font("Verdana", 16));
        deleteRgBtn.setStyle(redBtnStyle);
        deleteRgBtn.setOnMouseClicked(event -> {
            // Confirmation on delete
            int rgIdx = selectedIndices[0];
            RuleGroup rg = FFTManager.currFFT.ruleGroups.get(rgIdx);
            String labelText = "Are you sure you want to delete the rule group:\n" + rg.name;
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new DeleteRGDialog(labelText), 500, 150));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        VBox infoBox = new VBox(7);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(label1, label2);

        ruleBtnBox = new HBox(10, saveToDiskBtn, saveRuleBtn, deleteRuleBtn);
        ruleBtnBox.setAlignment(Pos.CENTER);

        rgBtnBox = new HBox(10, renameRgBtn, deleteRgBtn);
        rgBtnBox.setAlignment(Pos.CENTER);

        HBox topBtnBox = new HBox(10, addRuleBtn, addRgBtn, generateBtn, minimizeBtn);
        topBtnBox.setAlignment(Pos.CENTER);

        ruleBox = new HBox(10, ruleBtnBox, clearRuleBtn, undoBtn);
        ruleBox.setAlignment(Pos.CENTER);

        VBox backBox = new VBox();
        backBox.setAlignment(Pos.CENTER_RIGHT);
        backBox.getChildren().add(back);

        BorderPane rulePane = new BorderPane();
        rulePane.setCenter(ruleBox);
        BorderPane.setMargin(ruleBox, new Insets(0, 0, 0, 50));
        rulePane.setRight(backBox);


        bottomBox.getChildren().addAll(infoBox, verifyBox, topBtnBox, rulePane);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);

        setupRuleFetchTimer();
        clearPane();

    }

    private void playMsg(String msg, int seconds) {
        Label msgLabel = new Label(msg);
        msgLabel.setFont(Font.font("Verdana", 16));
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setTextAlignment(TextAlignment.CENTER);

        bottomBox.getChildren().add(msgLabel);
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(seconds * 1000),
                ae -> bottomBox.getChildren().remove(msgLabel)));
        timeline.play();
    }

    private void pushUndoStack() {
        UndoItem uItem = new UndoItem(new FFT(currFFT), selectedIndices);
        undoStack.push(uItem);
        undoBtn.setDisable(false);
        changes = true;
    }

    private UndoItem popUndoStack() {
        UndoItem uItem = undoStack.pop();
        if (undoStack.empty())
            undoBtn.setDisable(true);
        return uItem;
    }

    void setUpdateFunc(Runnable updateFunc) {
        this.updateFunc = updateFunc;
    }

    private void clearPane() {
        update(FFTManager.initialFFTNode);
    }

    void refresh(boolean clearUndo) {
        if (clearUndo)
            undoStack.clear();
        clearPane();
        showRuleGroups();
    }

    public void update(FFTNode node) {
        selectedIndices = null;
        FFTManager.interactiveNode.clear();
        undoStack.clear();
        saveRuleBtn.setDisable(true);
        deleteRuleBtn.setDisable(true);
        lw.getSelectionModel().clearSelection();

        Node n = FFTManager.interactiveNode.getInteractiveNode(node);
        addInteractiveNode(n);
    }

    private void addInteractiveNode(Node node) {
        HBox centerBox = new HBox(node, lw);
        centerBox.setSpacing(10);
        centerBox.setAlignment(Pos.CENTER);
        setCenter(centerBox);
        BorderPane.setMargin(centerBox, new Insets(0, 0, 0, 0));
        BorderPane.setAlignment(centerBox, Pos.CENTER);
    }

    public void update(FFTNode node, FFTMove move) {
        update(node);
        FFTManager.interactiveNode.setAction(move.convert());
    }

    public void setPrevScene(Scene scene) {
        this.prevScene = scene;
    }

    private void setupRuleFetchTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), ev -> {
            Rule r = interactiveFFTNode.getRule();
            if (r != null)
                ruleLabel.setText(r.toString());
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void showRuleGroups() {
        selectedIndices = null;
        ObservableList<RulePane> rules = FXCollections.observableArrayList();
        for (int i = 0; i < FFTManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = FFTManager.currFFT.ruleGroups.get(i);
            if (SHOW_RULE_GROUPS) rules.add(new RulePane(i));
            for (int j = 0; j < rg.rules.size(); j++) {
                rules.add(new RulePane(i, j));
            }
        }
        lw.setItems(rules);
        lw.setMaxHeight(rules.size() * ROW_SIZE);

    }

    private class RulePane extends StackPane {
        boolean isRuleGroup;
        int rgIdx, rIdx;
        Label label;

        RulePane(int rgIdx) { // Rulegroup entry
            super();
            setAlignment(Pos.CENTER_LEFT);
            this.isRuleGroup = true;
            this.rgIdx = rgIdx;

            RuleGroup rg = FFTManager.currFFT.ruleGroups.get(rgIdx);
            this.label = new Label((rgIdx + 1) + ": " + rg.name);
            label.setFont(Font.font("Verdana", 18));
            getChildren().add(label);
        }

        RulePane(int rgIdx, int rIdx) { // rule entry
            super();
            setAlignment(Pos.CENTER_LEFT);
            this.isRuleGroup = false;
            this.rgIdx = rgIdx;
            this.rIdx = rIdx;
            RuleGroup rg = FFTManager.currFFT.ruleGroups.get(rgIdx);

            Rule r = rg.rules.get(rIdx);
            this.label = new Label((rIdx + 1) + ": " + r);
            label.setFont(Font.font("Verdana", 14));
            getChildren().add(label);
        }
    }

    private class RuleCell extends ListCell<RulePane> {

        RuleCell() {
            ListCell thisCell = this;

            setOnMousePressed(event -> {
                RulePane pane = getItem();
                if (pane == null)
                    return;

                selectedIndices = new int[]{pane.rgIdx, pane.rIdx};
                if (pane.isRuleGroup) {
                    ruleBox.getChildren().remove(ruleBtnBox);
                    if (!ruleBox.getChildren().contains(rgBtnBox))
                        ruleBox.getChildren().add(0, rgBtnBox);
                    deleteRgBtn.setDisable(false);
                    renameRgBtn.setDisable(false);
                } else {
                    RuleGroup rg = FFTManager.currFFT.ruleGroups.get(pane.rgIdx);
                    addInteractiveNode(interactiveFFTNode.getInteractiveNode(rg.rules.get(pane.rIdx)));
                    selectedIndices = new int[]{pane.rgIdx, pane.rIdx};
                    saveRuleBtn.setDisable(false);
                    deleteRuleBtn.setDisable(false);
                    ruleBox.getChildren().remove(rgBtnBox);
                    if (!ruleBox.getChildren().contains(ruleBtnBox))
                        ruleBox.getChildren().add(0, ruleBtnBox);
                }
            });

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER_LEFT);

            setOnDragDetected(event -> {
                if (isEmpty() || getItem().isRuleGroup)
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
                    int droppedIdx = getIndex();
                    if (draggedIdx == droppedIdx)
                        return;

                    pushUndoStack();

                    int selectionIdx, rgSelectionIdx, rSelectionIdx;

                    RulePane draggedPane = lw.getItems().get(draggedIdx);
                    RuleGroup rg_dragged = FFTManager.currFFT.ruleGroups.get(draggedPane.rgIdx);
                    Rule r_dragged = rg_dragged.rules.get(draggedPane.rIdx);
                    rg_dragged.rules.remove(r_dragged);

                    if (isEmpty()) { // out-of-bounds
                        RuleGroup rg_last = FFTManager.currFFT.ruleGroups.get(FFTManager.currFFT.ruleGroups.size() - 1);
                        rg_last.rules.add(r_dragged);
                        selectionIdx = lw.getItems().size() - 1;

                        rgSelectionIdx = FFTManager.currFFT.ruleGroups.size() - 1;
                        rSelectionIdx = rg_last.rules.size() - 1;
                    } else {
                        RulePane droppedPane = lw.getItems().get(droppedIdx);
                        int rgIdx = droppedPane.rgIdx;
                        int rIdx = droppedPane.rIdx;

                        // Special-case when dropping on rule groups
                        if (droppedPane.isRuleGroup && droppedIdx != 0 && draggedIdx > droppedIdx) {
                            rIdx = lw.getItems().get(droppedIdx - 1).rIdx + 1;
                            rgIdx--;
                        }

                        RuleGroup rg_dropped = FFTManager.currFFT.ruleGroups.get(rgIdx);
                        rg_dropped.rules.add(rIdx, r_dragged);

                        selectionIdx = droppedIdx;

                        rgSelectionIdx = rgIdx;
                        rSelectionIdx = rIdx;
                    }
                    event.setDropCompleted(true);
                    showRuleGroups();
                    lw.getSelectionModel().select(selectionIdx);
                    selectedIndices = new int[]{rgSelectionIdx, rSelectionIdx};
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
                Label l = item.label;
                if (item.isRuleGroup) {
                    l.setTextFill(Color.GRAY);
                } else {
                    l.setTextFill(Color.BLACK);
                }
                setGraphic(item);
            }
        }
    }

    public class DeleteRGDialog extends ConfirmDialog {

        DeleteRGDialog(String labelText) {
            super(labelText);
        }

        @Override
        public void setYesBtnMouseClicked() {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            pushUndoStack();
            removeRuleGroup();
        }

        private void removeRuleGroup() {
            int rgIdx = selectedIndices[0];
            FFTManager.currFFT.ruleGroups.remove(rgIdx);
            deleteRgBtn.setDisable(true);
            renameRgBtn.setDisable(true);
            showRuleGroups();
            clearPane();
        }
    }

    public class NameRGPane extends InputPane {

        private boolean rename;

        NameRGPane(String labelText, boolean rename) {
            super(labelText);
            this.rename = rename;
        }

        @Override
        void setSaveBtnMouseClicked() {
            if (!tf.getText().replace(" ", "").isEmpty()) {
                pushUndoStack();
                RuleGroup rg;
                if (rename) {
                    int rgIdx = selectedIndices[0];
                    rg = FFTManager.currFFT.ruleGroups.get(rgIdx);
                    rg.name = tf.getText();
                } else {
                    rg = new RuleGroup(tf.getText());
                    FFTManager.currFFT.ruleGroups.add(rg);
                }

                Stage stage = (Stage) getScene().getWindow();
                stage.close();
                showRuleGroups();
                deleteRgBtn.setDisable(true);
                renameRgBtn.setDisable(true);
            }
        }
    }

    public class SaveFFTPane extends ConfirmDialog {

        SaveFFTPane() {
            super("You have unsaved changes to the FFT.\n Do you want to save them to disk now?");
        }

        @Override
        public void setYesBtnMouseClicked() {
            FFTManager.save();
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            changes = false;
        }
    }

    public class MinimizeFFTPane extends VBox {
        CheckBox minimizeBox;

        MinimizeFFTPane() {
            Label label = new Label("Choose which team to minimize the FFT for");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);

            minimizeBox = new CheckBox("Minimize preconditions");
            minimizeBox.setSelected(true);
            minimizeBox.setAlignment(Pos.CENTER);

            String[] playerNames = FFTManager.playerNames;

            Button p1Btn = new Button(playerNames[0]);
            p1Btn.setOnMouseClicked(event -> {
                minimizeAndClose(PLAYER1);
            });

            Button p2Btn = new Button(playerNames[1]);
            p2Btn.setOnMouseClicked(event -> {
                minimizeAndClose(PLAYER2);
            });

            Button bothBtn = new Button("Both");
            bothBtn.setOnMouseClicked(event -> {
                minimizeAndClose(PLAYER_ANY);
            });


            HBox btnBox = new HBox(20, p1Btn, p2Btn, bothBtn);
            btnBox.setAlignment(Pos.CENTER);

            setSpacing(20);
            setPadding(new Insets(20, 0, 20, 0));
            setAlignment(Pos.CENTER);

            getChildren().addAll(label, minimizeBox, btnBox);
        }

        private void minimizeAndClose(int team) {
            boolean tempChanges = changes;
            pushUndoStack();
            int ruleSize = FFTManager.currFFT.getAmountOfRules();
            int precSize = FFTManager.currFFT.getAmountOfPreconditions();
            // todo
            //int iterations = FFTManager.currFFT.minimize(team, minimizeBox.isSelected());
            int iterations = -1;

            int diffRules = ruleSize - FFTManager.currFFT.getAmountOfRules();
            int diffPrecs = precSize - FFTManager.currFFT.getAmountOfPreconditions();
            String msg;
            if (iterations == -1) { // error, not a winning strategy
                msg = "FFT is not a winning strategy, so it can't be minimized";
                popUndoStack();
                changes = tempChanges;
            }
            else if (diffRules == 0 && diffPrecs == 0) {
                msg = "FFT is already fully minimized";
                popUndoStack();
                changes = tempChanges;
            } else {
                msg = diffRules + " rules and " + diffPrecs + " preconditions were removed";
                changes = true;
            }
            System.out.println(msg);
            playMsg(msg, 5);
            refresh(false);
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        }

    }

    public class GenerateFFTPane extends VBox {

        GenerateFFTPane() {
            Label label = new Label("Choose which team to generate the FFT for");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);

            String[] playerNames = FFTManager.playerNames;

            Button p1Btn = new Button(playerNames[0]);
            p1Btn.setOnMouseClicked(event -> {
                generateAndClose(PLAYER1);
            });

            Button p2Btn = new Button(playerNames[1]);
            p2Btn.setOnMouseClicked(event -> {
                generateAndClose(PLAYER2);
            });

            Button bothBtn = new Button("Both");
            bothBtn.setOnMouseClicked(event -> {
                generateAndClose(PLAYER_ANY);
            });

            HBox btnBox = new HBox(20, p1Btn, p2Btn, bothBtn);
            btnBox.setAlignment(Pos.CENTER);

            setSpacing(20);
            setPadding(new Insets(20, 0, 20, 0));
            setAlignment(Pos.CENTER);

            getChildren().addAll(label, btnBox);
        }

        private void generateAndClose(int team) {
            boolean tempChanges = changes;
            pushUndoStack();

            // todo
            String msg;
            boolean verify = false;
            //if (!currFFT.verify(team, false)) { // error, not a winning strategy
            if (!verify) {
                msg = "The exiting strategy was not weakly verified, so can't generate an optimal strategy";
                popUndoStack();
                changes = tempChanges;
            }
            //else if (currFFT.verify(team, true)) {
            else if (!verify) {
                msg = "Strategy is already strongly optimal";
                popUndoStack();
                changes = tempChanges;
            } else {
                FFTManager.autogenFFT(currFFT);
                msg = "Strongly optimal strategy succesfully generated";
                changes = true;
            }
            System.out.println(msg);
            playMsg(msg, 5);
            refresh(false);
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        }

    }


    private static class UndoItem {
        private FFT fft;
        private int[] selectedIndices;

        public UndoItem(FFT fft, int[] selectedIndices) {
            this.fft = fft;
            this.selectedIndices = selectedIndices;
        }

        public FFT getFFT() {
            return fft;
        }

        public int[] getSelectedIndices() {
            return selectedIndices;
        }
    }
}
