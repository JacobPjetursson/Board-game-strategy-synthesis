package fftlib.gui;

import fftlib.FFT;
import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import misc.Globals;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.ArrayList;
import java.util.Stack;

import static fftlib.FFTManager.*;
import static misc.Config.SHOW_RULE_GROUPS;
import static misc.Globals.*;

public class FFTInteractivePane extends BorderPane {
    private int textFieldWidth = 150;
    private FFTManager fftManager;
    private int perspective = PLAYER1;
    private ListView<RulePane> lw;
    private InteractiveFFTState interactiveFFTState;
    private Label ruleLabel;

    private RadioButton p1Btn;
    private RadioButton p2Btn;

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

    private static final int ROW_SIZE = 26;

    public FFTInteractivePane(FFTManager fftManager) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setMaxHeight(Globals.HEIGHT);
        setMaxWidth(Globals.WIDTH);
        this.fftManager = fftManager;
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

        interactiveFFTState = FFTManager.interactiveState;

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
        verificationChoice.setValue("Total");
        verificationChoice.setStyle("-fx-font: 16px \"Verdana\";");
        verificationChoice.setItems(FXCollections.observableArrayList("Total", "Partial"));

        verifyBtn = new Button("Verify");
        verifyBtn.setFont(Font.font("Verdana", 16));
        verifyBtn.setTooltip(new Tooltip("Checks if the current FFT is a winning strategy,\n" +
                "or if given rules are part of winning strategy"));
        verifyBtn.setOnMouseClicked(event -> {
            int team = teamChoice.getSelectionModel().getSelectedIndex() + 1;
            boolean wholeFFT = verificationChoice.getSelectionModel().getSelectedIndex() == 0;
            boolean verified =  fftManager.currFFT.verify(team, wholeFFT);

            if (!verified) {
                if (fftManager.currFFT.failingPoint == null) {
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
                    stage.setScene(new Scene(new FFTFailurePane(scene, fftManager, this), WIDTH, Globals.HEIGHT));
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

        p1Btn = new RadioButton(playerNames[0]);
        p1Btn.setFont(Font.font("Verdana", 16));
        p2Btn = new RadioButton(playerNames[1]);
        p2Btn.setFont(Font.font("Verdana", 16));
        ToggleGroup toggleGrp = new ToggleGroup();
        p1Btn.setToggleGroup(toggleGrp);
        p2Btn.setToggleGroup(toggleGrp);
        HBox perspectiveGrp = new HBox(10, label3, p1Btn, p2Btn);
        perspectiveGrp.setAlignment(Pos.CENTER);
        p1Btn.setOnMouseClicked(event -> {
            perspective = PLAYER1;
            interactiveFFTState.setPerspective(perspective);
        });
        p2Btn.setOnMouseClicked(event -> {
            perspective = PLAYER2;
            interactiveFFTState.setPerspective(perspective);
        });


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
            Rule r = interactiveFFTState.getRule();
            if (!Rule.isValidRuleFormat(r)) {
                System.err.println("Invalid rule format!");
                return;
            }
            pushUndoStack();
            ArrayList<RuleGroup> ruleGroups = fftManager.currFFT.ruleGroups;
            if (ruleGroups.isEmpty())
                ruleGroups.add(new RuleGroup(""));
            Rule copy = new Rule(r);
            fftManager.currFFT.ruleGroups.get(ruleGroups.size() - 1).addRule(copy);
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
            fftManager.currFFT = item.getFFT();
            showRuleGroups();
            lw.requestFocus();
            selectedIndices = item.getSelectedIndices();
            int perspective = item.getPerspective();
            interactiveFFTState.setPerspective(perspective);
            if (perspective == PLAYER1)
                p1Btn.fire();
            else
                p2Btn.fire();


            if (selectedIndices == null) {
                clearPane();
            } else {
                int rgIdx = selectedIndices[0];
                int rIdx = selectedIndices[1];
                int selectionIdx = rgIdx + rIdx + 1;
                lw.getSelectionModel().select(selectionIdx);
                RuleGroup rg = fftManager.currFFT.ruleGroups.get(rgIdx);
                addInteractiveNode(interactiveFFTState.getInteractiveNode(rg.rules.get(rIdx)));

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
            Rule r = interactiveFFTState.getRule();
            if (!Rule.isValidRuleFormat(r) || selectedIndices == null) {
                return;
            }
            pushUndoStack();

            int rgIdx = selectedIndices[0];
            int rIdx = selectedIndices[1];
            // Replace
            fftManager.currFFT.ruleGroups.get(rgIdx).rules.remove(rIdx);
            fftManager.currFFT.ruleGroups.get(rgIdx).rules.add(rIdx, r);
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
            fftManager.currFFT.ruleGroups.get(rgIdx).rules.remove(rIdx);
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
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(rgIdx);
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
        infoBox.getChildren().addAll(label1, label2, perspectiveGrp);

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
        UndoItem uItem = new UndoItem(new FFT(fftManager.currFFT), selectedIndices, perspective);
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
        update(FFTManager.initialFFTState);
    }

    void refresh(boolean clearUndo) {
        if (clearUndo)
            undoStack.clear();
        clearPane();
        showRuleGroups();
    }

    public void update(FFTState state) {
        selectedIndices = null;
        FFTManager.interactiveState.clear();
        undoStack.clear();
        saveRuleBtn.setDisable(true);
        deleteRuleBtn.setDisable(true);
        lw.getSelectionModel().clearSelection();

        Node node = FFTManager.interactiveState.getInteractiveNode(state);
        if (state.getTurn() == PLAYER1)
            p1Btn.setSelected(true);
        else
            p2Btn.setSelected(true);
        addInteractiveNode(node);
    }

    private void addInteractiveNode(Node node) {
        HBox centerBox = new HBox(node, lw);
        centerBox.setSpacing(10);
        centerBox.setAlignment(Pos.CENTER);
        setCenter(centerBox);
        BorderPane.setMargin(centerBox, new Insets(0, 0, 0, 0));
        BorderPane.setAlignment(centerBox, Pos.CENTER);
    }

    public void update(FFTState state, FFTMove move) {
        update(state);
        FFTManager.interactiveState.setAction(move.getAction());
    }

    public void setPrevScene(Scene scene) {
        this.prevScene = scene;
    }

    private void setupRuleFetchTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), ev -> {
            Rule r = interactiveFFTState.getRule();
            if (r != null)
                ruleLabel.setText(r.toString());
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void showRuleGroups() {
        selectedIndices = null;
        ObservableList<RulePane> rules = FXCollections.observableArrayList();
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(i);
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

            RuleGroup rg = fftManager.currFFT.ruleGroups.get(rgIdx);
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
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(rgIdx);

            Rule r = rg.rules.get(rIdx);
            this.label = new Label((rIdx + 1) + ": " + r);
            label.setFont(Font.font("Verdana", 13));
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
                    RuleGroup rg = fftManager.currFFT.ruleGroups.get(pane.rgIdx);
                    addInteractiveNode(interactiveFFTState.getInteractiveNode(rg.rules.get(pane.rIdx)));
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
                    RuleGroup rg_dragged = fftManager.currFFT.ruleGroups.get(draggedPane.rgIdx);
                    Rule r_dragged = rg_dragged.rules.get(draggedPane.rIdx);
                    rg_dragged.rules.remove(r_dragged);

                    if (isEmpty()) { // out-of-bounds
                        RuleGroup rg_last = fftManager.currFFT.ruleGroups.get(fftManager.currFFT.ruleGroups.size() - 1);
                        rg_last.rules.add(r_dragged);
                        selectionIdx = lw.getItems().size() - 1;

                        rgSelectionIdx = fftManager.currFFT.ruleGroups.size() - 1;
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

                        RuleGroup rg_dropped = fftManager.currFFT.ruleGroups.get(rgIdx);
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
            fftManager.currFFT.ruleGroups.remove(rgIdx);
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
                    rg = fftManager.currFFT.ruleGroups.get(rgIdx);
                    rg.name = tf.getText();
                } else {
                    rg = new RuleGroup(tf.getText());
                    fftManager.currFFT.ruleGroups.add(rg);
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

        private void minimizeAndClose(int perspective) {
            boolean tempChanges = changes;
            pushUndoStack();
            int ruleSize = fftManager.currFFT.getAmountOfRules();
            int precSize = fftManager.currFFT.getAmountOfPreconditions();
            int iterations = fftManager.currFFT.minimize(perspective, minimizeBox.isSelected());

            int diffRules = ruleSize - fftManager.currFFT.getAmountOfRules();
            int diffPrecs = precSize - fftManager.currFFT.getAmountOfPreconditions();
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

        private void generateAndClose(int perspective) {
            boolean tempChanges = changes;
            pushUndoStack();

            String msg;
            if (!currFFT.verify(perspective, false)) { // error, not a winning strategy
                msg = "The exiting strategy was not partially verified, so can't generate an optimal strategy";
                popUndoStack();
                changes = tempChanges;
            }
            else if (currFFT.verify(perspective, true)) {
                msg = "Strategy is already weakly optimal";
                popUndoStack();
                changes = tempChanges;
            } else {
                FFTManager.autogenFFT(currFFT);
                msg = "Weakly optimal strategy succesfully generated";
                changes = true;
            }
            System.out.println(msg);
            playMsg(msg, 5);
            refresh(false);
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        }

    }


    private class UndoItem {
        private FFT fft;
        private int[] selectedIndices;
        private int perspective;

        public UndoItem(FFT fft, int[] selectedIndices, int perspective) {
            this.fft = fft;
            this.selectedIndices = selectedIndices;
            this.perspective = perspective;
        }

        public FFT getFFT() {
            return fft;
        }

        public int[] getSelectedIndices() {
            return selectedIndices;
        }

        public int getPerspective() {
            return perspective;
        }
    }
}
