package fftlib.gui;

import fftlib.FFTAutoGen;
import fftlib.logic.FFT;
import fftlib.FFTManager;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.RuleGroup;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;

import static fftlib.FFTManager.*;
import static misc.Config.*;
import static misc.Globals.*;

public class FFTEditPane extends BorderPane {
    private static final int textFieldWidth = 150;
    private static final double RULEGROUP_LABEL_SIZE = 35;
    private static final double RULE_SIZE = 29.5;

    private Label fftTitle;
    private Label ruleLabel;

    private Button deleteRuleBtn;
    private Button deleteRgBtn;
    private Button renameRgBtn;
    private Button undoBtn;
    private Button clearRuleBtn;

    private VBox bottomBox;

    private ComboBox<String> changeFFTBox;
    private Stack<UndoItem> undoStack;
    private boolean changes;

    private Scene prevScene;

    private int selectedCellIdx = -1; // index in list
    private boolean isShiftDown;

    private FFTRuleEditPane ruleEditPane;
    private ListView<RulePane> lw;

    private FFTEditPane thisPane = this;

    public FFTEditPane() {
        // set basic parameters
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setMaxHeight(HEIGHT);
        setMaxWidth(WIDTH);
        undoStack = new Stack<>();

        // titles and fft buttons
        VBox topBox = new VBox(5);
        topBox.setMinHeight(65);
        topBox.setAlignment(Pos.CENTER);
        setTop(topBox);
        BorderPane.setAlignment(topBox, Pos.CENTER);

        // make fft title
        BorderPane fftPane = new BorderPane();
        fftTitle = new Label(currFFT.getName());
        fftTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        fftTitle.setAlignment(Pos.CENTER);
        fftTitle.setTextAlignment(TextAlignment.CENTER);
        fftPane.setCenter(fftTitle);

        // make hbox with fft buttons
        HBox fftBtnBoxLeft = new HBox(5);
        fftBtnBoxLeft.setAlignment(Pos.CENTER_LEFT);
        fftBtnBoxLeft.setPadding(new Insets(10));
        fftPane.setLeft(fftBtnBoxLeft);
        HBox fftBtnBoxRight = new HBox(5);
        fftBtnBoxRight.setAlignment(Pos.CENTER_RIGHT);
        fftBtnBoxRight.setPadding(new Insets(10));
        fftPane.setRight(fftBtnBoxRight);
        // change fft
        changeFFTBox = new ComboBox<>();
        changeFFTBox.setPromptText("Change FFT");
        changeFFTBox.setStyle("-fx-font: 16px \"Verdana\";");
        changeFFTBox.setMinWidth(180);
        changeFFTBox.setMaxWidth(180);
        // set items
        // Items in changeFFTBox
        ObservableList<String> fftStrs = FXCollections.observableArrayList();
        for (FFT fft : ffts) {
            fftStrs.add(fft.getName());
        }
        changeFFTBox.setItems(fftStrs);
        changeFFTBox.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    if ((Integer) newValue == -1)
                        return;
                    setCurrFFT((Integer) newValue);
                    refresh();
                });
        // delete fft
        Button deleteFFTBtn = new Button("Delete FFT");
        deleteFFTBtn.setMinWidth(150);
        deleteFFTBtn.setFont(Font.font("Verdana", 16));
        deleteFFTBtn.setStyle(redBtnStyle);
        deleteFFTBtn.setOnAction(event -> {
            Stage newStage = new Stage();
            String labelText = "Are you sure you want to delete the FFT:\n" +
                    currFFT.getName() + "?";
            newStage.setScene(new Scene(new DeleteFFTDialog(labelText), 500, 150));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        // add new fft
        Button addFFTBtn = new Button("Add new FFT");
        addFFTBtn.setMinWidth(150);
        addFFTBtn.setStyle(greenBtnStyle);
        addFFTBtn.setFont(Font.font("Verdana", 16));
        addFFTBtn.setOnAction(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameFFTPane("Name your new FFT", false), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        // rename
        Button renameFFTBtn = new Button("Rename FFT");
        renameFFTBtn.setStyle(blueBtnStyle);
        renameFFTBtn.setMinWidth(180);
        renameFFTBtn.setMaxWidth(180);
        renameFFTBtn.setFont(Font.font("Verdana", 16));
        renameFFTBtn.setOnAction(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameFFTPane("Rename your FFT", true), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });
        // add all buttons
        fftBtnBoxLeft.getChildren().addAll(addFFTBtn, deleteFFTBtn);
        fftBtnBoxRight.getChildren().addAll(changeFFTBox, renameFFTBtn);

        // make rule label
        ruleLabel = new Label();
        ruleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        ruleLabel.setAlignment(Pos.CENTER);
        ruleLabel.setTextAlignment(TextAlignment.CENTER);
        topBox.getChildren().addAll(fftPane, ruleLabel);

        // set center
        ruleEditPane = fftRuleEditPane;
        ruleEditPane.setFftEditPane(this);
        ruleEditPane.disable();

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setMinWidth(750);
        lw.setCellFactory(param -> new RuleCell());
        lw.requestFocus();
        showRules();

        HBox centerBox = new HBox(15, ruleEditPane.getNode(), lw);
        setCenter(centerBox);
        centerBox.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(lw, Pos.CENTER);
        BorderPane.setMargin(centerBox, new Insets(15));
        BorderPane.setMargin(lw, new Insets(0, 0, 0, 0));

        // set bottom node
        bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        // SET BUTTONS
        // TEAM BOX
        Label teamLabel = new Label("Team:");
        teamLabel.setFont(Font.font("Verdana", 15));
        ChoiceBox<String> teamChoice = new ChoiceBox<>();
        teamChoice.setMinWidth(textFieldWidth);
        teamChoice.setMaxWidth(textFieldWidth);
        teamChoice.setStyle("-fx-font: 16px \"Verdana\";");
        String[] playerNames = FFTManager.playerNames;
        teamChoice.setItems(FXCollections.observableArrayList(playerNames[0], playerNames[1]));
        teamChoice.setValue(getPlayerName(AUTOGEN_TEAM));
        HBox teamBox = new HBox(10);
        teamBox.setAlignment(Pos.CENTER);
        teamBox.getChildren().addAll(teamLabel, teamChoice);
        teamChoice.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, number, t1) -> {
            AUTOGEN_TEAM = t1.intValue()+1;
        });

        // VERIFICATION
        Button verifyBtn = new Button("Verify");
        verifyBtn.setFont(Font.font("Verdana", 16));
        verifyBtn.setTooltip(new Tooltip("Checks if the current FFT is strongly/weakly optimal"));

        verifyBtn.setOnMouseClicked(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new VerifyFFTPane(), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.show();
        });

        // ADD RULE BUTTON
        Button addRuleBtn = new Button("Add rule");
        addRuleBtn.setStyle(greenBtnStyle);
        addRuleBtn.setFont(Font.font("Verdana", 16));
        addRuleBtn.setOnMouseClicked(event -> {
            appendRule(new PropRule());
        });

        Button addRgBtn = new Button("Add rule group");
        addRgBtn.setStyle(greenBtnStyle);
        addRgBtn.setFont(Font.font("Verdana", 16));
        addRgBtn.setOnMouseClicked(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameRGPane("Write name of rule group", false), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        // UNDO BUTTON
        undoBtn = new Button("Undo");
        undoBtn.setDisable(true);
        undoBtn.setStyle(orangeBtnStyle);
        undoBtn.setFont(Font.font("Verdana", 16));
        undoBtn.setOnMouseClicked(event -> {
            if (undoStack.empty()) // shouldn't be possible
                return;
            UndoItem item = popUndoStack();
            FFTManager.ffts = item.ffts;
            setCurrFFT(item.fftIndex);
            // refresh to reset everything, then set idx and show rules again after
            refresh();
            this.selectedCellIdx = item.selectedIndex;
            showRules();
        });

        // SAVE TO DISK BTN
        Button saveToDiskBtn = new Button("Save to disk");
        saveToDiskBtn.setStyle(blueBtnStyle);
        saveToDiskBtn.setFont(Font.font("Verdana", 16));
        saveToDiskBtn.setOnMouseClicked(event -> {
            save();
            playMsg("FFT successfully saved to disk", 3);
            changes = false;
        });

        // MINIMIZE BTN
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

        // SYNTHESIZE FFT BTN
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

        // DELETE RULE BTN
        deleteRuleBtn = new Button("Delete rule");
        deleteRuleBtn.setStyle(redBtnStyle);
        deleteRuleBtn.setFont(Font.font("Verdana", 16));
        deleteRuleBtn.setDisable(true);
        deleteRuleBtn.setOnMouseClicked(event -> {
            pushUndoStack();
            int rIdx = getSelectedPane().rIdx;
            currFFT.removeRule(rIdx);
            refresh();
        });

        // CLEAR RULE BTN
        clearRuleBtn = new Button("Clear rule");
        clearRuleBtn.setFont(Font.font("Verdana", 16));
        clearRuleBtn.setDisable(true);
        clearRuleBtn.setOnMouseClicked(event -> {
            int rIdx = getSelectedPane().rIdx;
            Rule r = currFFT.getRules().get(rIdx);
            if (r.isEmpty())
                return;

            pushUndoStack();
            r.clear();

            ruleLabel.setText("");
            ruleEditPane.clear();
            showRules();

        });

        // RENAME RG BTN
        renameRgBtn = new Button("Rename rule group");
        renameRgBtn.setFont(Font.font("Verdana", 16));
        renameRgBtn.setStyle(blueBtnStyle);
        renameRgBtn.setDisable(true);
        renameRgBtn.setOnMouseClicked(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameRGPane("Rename rule group", true), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        // DELETE RG BTN
        deleteRgBtn = new Button("Delete rule group");
        deleteRgBtn.setFont(Font.font("Verdana", 16));
        deleteRgBtn.setStyle(redBtnStyle);
        deleteRgBtn.setDisable(true);
        deleteRgBtn.setOnMouseClicked(event -> {
            // Confirmation on delete
            RuleGroup rg = getSelectedPane().rg;
            String labelText = "Are you sure you want to delete the rule group:\n" + rg.name;
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new DeleteRGDialog(labelText), 500, 150));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });

        // BACK BUTTON
        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 16));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);

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

        // Glue everything together
        HBox bottomBox1 = new HBox(10);
        bottomBox1.setAlignment(Pos.CENTER);
        bottomBox1.getChildren().addAll(teamBox, verifyBtn, generateBtn, minimizeBtn);


        HBox bottomBox2 = new HBox(5);
        bottomBox2.setAlignment(Pos.CENTER);
        bottomBox2.getChildren().addAll(addRgBtn, deleteRgBtn, renameRgBtn);

        HBox ruleBox = new HBox(5);
        ruleBox.setAlignment(Pos.CENTER);
        ruleBox.getChildren().addAll(addRuleBtn, deleteRuleBtn, clearRuleBtn);

        HBox miscBox = new HBox(5);
        miscBox.setAlignment(Pos.CENTER);
        miscBox.getChildren().addAll(saveToDiskBtn, undoBtn);

        HBox bottomBox3 = new HBox(25, ruleBox, miscBox);
        bottomBox3.setAlignment(Pos.CENTER);

        VBox backBox = new VBox();
        backBox.setAlignment(Pos.CENTER_RIGHT);
        backBox.getChildren().add(back);

        BorderPane bottomBox3Pane = new BorderPane();
        bottomBox3Pane.setCenter(bottomBox3);
        BorderPane.setMargin(bottomBox3, new Insets(0, 0, 0, 50));
        bottomBox3Pane.setRight(backBox);

        bottomBox.getChildren().addAll(bottomBox1, bottomBox2, bottomBox3Pane);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);

        // key event handlers
        lw.setOnKeyPressed(event -> {

            KeyCode code = event.getCode();
            if (code == KeyCode.SHIFT) {
                isShiftDown = true;
                return;
            }
            RulePane selectedPane = getSelectedPane();
            RulePane newPane;

            if (!isShiftDown || selectedPane.isRuleGroup) {
                // determine if we will be moving position
                int changedIdx = 0;
                if (code == KeyCode.DOWN && (selectedCellIdx < lw.getItems().size()-1))
                    changedIdx = 1;
                else if (code == KeyCode.UP && selectedCellIdx > 0)
                    changedIdx = -1;

                if (changedIdx != 0) {
                    // change selection index
                    selectedCellIdx += changedIdx;
                    // highlight new selected pane
                    getSelectedPane().select();
                }
            } else {

                if (code == KeyCode.DOWN) {
                    // get corner cases out of the way
                    // move down when we are in bottom of current rulegroup (no movement)
                    if (selectedPane.inRuleGroup &&
                            selectedPane.rIdx == (selectedPane.rg.endIdx-1)) {
                        selectedPane.rg.endIdx--;
                    } else if (selectedCellIdx < lw.getItems().size() - 1) {
                        selectedCellIdx++;
                        newPane = getSelectedPane();
                        // move down into rulegroup when we are not in rulegroup
                        if (!selectedPane.inRuleGroup &&
                                newPane.isRuleGroup && (selectedPane.rIdx == newPane.rg.startIdx-1)) {
                            newPane.rg.startIdx--;
                        }
                        // regular move
                        else {
                            currFFT.moveRule(selectedPane.rIdx, newPane.rIdx);
                        }
                    }
                } else if (code == KeyCode.UP && selectedCellIdx > 0) {
                    selectedCellIdx--;
                    newPane = getSelectedPane();
                    // move up into rulegroup when we are not in rulegroup
                    if (!selectedPane.inRuleGroup && newPane.inRuleGroup &&
                            selectedPane.rIdx == (newPane.rg.endIdx)) {
                        newPane.rg.endIdx++;
                        // we don't move the rule anywhere, just change indentation
                        selectedCellIdx++;
                    }
                    // move up when we are in top of current rulegroup
                    else if (selectedPane.inRuleGroup &&
                            selectedPane.rIdx == (selectedPane.rg.startIdx)) {
                        selectedPane.rg.startIdx++;
                    } // regular move
                    else {
                        currFFT.moveRule(selectedPane.rIdx, newPane.rIdx);
                    }

                }
                showRules();
            }
        });

        lw.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SHIFT)
                isShiftDown = false;
        });

    }

    public void setRuleText(Rule r) {
        this.ruleLabel.setText(r.toString());
    }

    private RulePane getSelectedPane() {
        return lw.getItems().get(selectedCellIdx);
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

    public void pushUndoStack() {
        UndoItem uItem = new UndoItem(selectedCellIdx);
        undoStack.push(uItem);
        undoBtn.setDisable(false);
        changes = true;
        // only allow 20 items
        if (undoStack.size() >= 20)
            undoStack.remove(undoStack.size() - 1);
    }

    public UndoItem popUndoStack() {
        UndoItem uItem = undoStack.pop();
        if (undoStack.empty())
            undoBtn.setDisable(true);
        return uItem;
    }

    public void refresh() {
        ruleLabel.setText("");
        selectedCellIdx = -1;
        ruleEditPane.clear();
        deleteRuleBtn.setDisable(true);
        clearRuleBtn.setDisable(true);
        deleteRgBtn.setDisable(true);
        renameRgBtn.setDisable(true);
        // set options in fft box
        ObservableList<String> fftStrs = FXCollections.observableArrayList();
        for (FFT fft : FFTManager.ffts) {
            fftStrs.add(fft.getName());
        }
        changeFFTBox.getSelectionModel().select(-1);
        changeFFTBox.setPromptText("Change FFT");
        changeFFTBox.setItems(fftStrs);

        // set fft title
        fftTitle.setText(currFFT.getName());

        showRules();
    }

    public void setPrevScene(Scene scene) {
        this.prevScene = scene;
    }

    // used when creating a rule from the failurepane, and from the ruleBtn
    public void appendRule(Rule r) {
        pushUndoStack();
        currFFT.append(r.clone());
        selectedCellIdx = lw.getItems().size();
        ruleLabel.setText(r.toString());
        ruleEditPane.update(r);
        showRules();
    }

    public void addRule(int idx, Rule r) {
        pushUndoStack();
        currFFT.addRule(r, idx);
        selectedCellIdx = findRuleIdx(r);
        ruleLabel.setText(r.toString());
        ruleEditPane.update(r);
        showRules();
    }

    private int findRuleIdx(Rule r) {
        for (int i = 0; i < lw.getItems().size(); i++) {
            RulePane rp = lw.getItems().get(i);
            if (!rp.isRuleGroup) {
                Rule paneRule = currFFT.getRules().get(rp.rIdx);
                if (paneRule.equals(r))
                    return i;
            }
        }
        return -1;
    }

    public void showRules() {
        ObservableList<RulePane> rules = FXCollections.observableArrayList();

        int i = 0;
        double size = 0;
        while (i < currFFT.getRules().size()) {
            // check if any rulegroup starts at index i
            for (RuleGroup rg : currFFT.getRuleGroups()) {
                if (i == rg.startIdx && i < currFFT.size()) {
                    rules.add(new RulePane(rg));
                    size += RULEGROUP_LABEL_SIZE;
                    while (i < rg.endIdx && i < currFFT.getRules().size()) {
                        rules.add(new RulePane(i, rg));
                        size += RULE_SIZE;
                        i++;
                    }
                }
            }
            if (i >= currFFT.size())
                break;
            rules.add(new RulePane(i));
            size += RULE_SIZE;
            i++;
        }
        // account for corner case where rulegroup is empty at end of rules
        for (RuleGroup rg : currFFT.getRuleGroups()) {
            if (rg.startIdx >= currFFT.size())
            rules.add(new RulePane(rg));
            size += RULEGROUP_LABEL_SIZE;
        }

        lw.setItems(rules);
        lw.setMinHeight(50);
        lw.setMaxHeight(size);
        if (selectedCellIdx != -1) {
            lw.getSelectionModel().select(selectedCellIdx);
            lw.getItems().get(selectedCellIdx).select();
            lw.requestFocus();
        }
    }

    private Node getRuleGroupLock(RuleGroup rg) {
        URL urlLock = this.getClass().getClassLoader().getResource("lock.png");
        URL urlunlock = this.getClass().getClassLoader().getResource("unlock.png");
        Image lockImg = new Image(urlLock.toExternalForm());
        Image unlockImg = new Image(urlunlock.toExternalForm());
        ImageView imgView = new ImageView();
        if (rg.isLocked())
            imgView.setImage(lockImg);
        else
            imgView.setImage(unlockImg);

        imgView.setPreserveRatio(true);
        imgView.setFitHeight(RULEGROUP_LABEL_SIZE / 1.5);
        imgView.setFitWidth(RULEGROUP_LABEL_SIZE / 1.5);
        imgView.setSmooth(true);
        imgView.setOnMouseClicked(event -> {
            pushUndoStack();
            // change lock
            if (rg.isLocked()) {
                imgView.setImage(unlockImg);
                rg.setLocked(false);
            } else {
                imgView.setImage(lockImg);
                rg.setLocked(true);
            }
        });

        return imgView;
    }

    // HELPER CLASSES
    private class RulePane extends StackPane {
        Label label;
        // used if pane is the rule group label
        RuleGroup rg;
        boolean isRuleGroup;
        // used if pane is a normal rule
        int rIdx;
        boolean inRuleGroup;

        RulePane(RuleGroup rg) {
            super();
            setAlignment(Pos.CENTER_LEFT);
            this.rg = rg;
            isRuleGroup = true;
            inRuleGroup = true;

            this.label = new Label(rg.name);
            label.setFont(Font.font("Verdana", 18));
            HBox rgBox = new HBox(20);
            rgBox.setAlignment(Pos.CENTER_LEFT);
            rgBox.getChildren().addAll(label, getRuleGroupLock(rg));
            getChildren().add(rgBox);
        }

        RulePane(int idx, RuleGroup rg) { // rule entry
            this(idx);
            this.inRuleGroup = true;
            this.rg = rg;
            Rule r = currFFT.getRules().get(rIdx);
            this.label.setText("   " + (idx + 1) + ": " + r);
        }

        RulePane(int idx) { // rule entry
            super();
            this.rIdx = idx;
            this.inRuleGroup = false;
            Rule r = currFFT.getRules().get(rIdx);
            setAlignment(Pos.CENTER_LEFT);

            this.label = new Label((idx + 1) + ": " + r);
            label.setFont(Font.font("Verdana", 14));
            getChildren().add(label);
        }

        public void select() {
            // set buttons
            deleteRgBtn.setDisable(!isRuleGroup);
            renameRgBtn.setDisable(!isRuleGroup);
            deleteRuleBtn.setDisable(isRuleGroup);
            clearRuleBtn.setDisable(isRuleGroup);
            if (isRuleGroup) {
                // clear
                ruleLabel.setText("");
                ruleEditPane.disable();
            } else {
                Rule r = currFFT.getRules().get(rIdx);
                // set title
                ruleLabel.setText(r.toString());
                // set rule pane
                ruleEditPane.update(r);
            }
        }
    }

    private class RuleCell extends ListCell<RulePane> {
        RuleCell() {
            setOnMousePressed(event -> {
                selectedCellIdx = getIndex();
                getItem().select();
            });

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER_LEFT);
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
            deleteRuleGroup();
        }

        private void deleteRuleGroup() {
            RuleGroup rg = getSelectedPane().rg;
            currFFT.removeRuleGroup(rg, true);
            refresh();
        }
    }

    public class DeleteFFTDialog extends ConfirmDialog {
        public DeleteFFTDialog(String labelText) {
            super(labelText);
        }

        @Override
        public void setYesBtnMouseClicked() {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            pushUndoStack();
            FFTManager.deleteCurrFFT();
            refresh();
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
            String text = textField.getText().trim();
            if (!text.isEmpty()) {
                pushUndoStack();
                RuleGroup rg;
                if (rename) {
                    rg = getSelectedPane().rg;
                    rg.name = textField.getText();
                } else {
                    // startidx, endidx is just above last rule (so no rules in rg yet)
                    int idx = currFFT.size();
                    rg = new RuleGroup(currFFT, textField.getText(), idx, idx);
                    currFFT.addRuleGroup(rg);
                    selectedCellIdx = lw.getItems().size();
                }

                Stage stage = (Stage) getScene().getWindow();
                stage.close();

                showRules();
            }
        }
    }

    // Can be either rename or new fft
    private class NameFFTPane extends InputPane {
        private boolean rename;

        NameFFTPane(String labelText, boolean rename) {
            super(labelText);
            this.rename = rename;
        }

        @Override
        void setSaveBtnMouseClicked() {
            String text = textField.getText().trim();
            if (!text.isEmpty()) {
                pushUndoStack();
                if (rename)
                    currFFT.setName(textField.getText());
                else
                    addNewFFT(textField.getText());
                Stage stage = (Stage) getScene().getWindow();
                stage.close();
                refresh();
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
            Label label = new Label("Minimize the FFT");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);

            minimizeBox = new CheckBox("Minimize preconditions");
            minimizeBox.setSelected(MINIMIZE_PRECONDITIONS);
            minimizeBox.setAlignment(Pos.CENTER);

            Button minimizeBtn = new Button("Minimize");
            minimizeBtn.setOnMouseClicked(event -> {
                minimizeAndClose();
            });

            HBox btnBox = new HBox(20, minimizeBtn);
            btnBox.setAlignment(Pos.CENTER);

            setSpacing(20);
            setPadding(new Insets(20, 0, 20, 0));
            setAlignment(Pos.CENTER);

            getChildren().addAll(label, minimizeBox, btnBox);
        }

        private void minimizeAndClose() {
            String msg;
            // check if FFT is strongly optimal first
            if (!currFFT.verify(AUTOGEN_TEAM, true))
                msg = "FFT is not strongly optimal, so it can't be minimized";
            else {
                boolean tempChanges = changes;
                int ruleSize = FFTManager.currFFT.getAmountOfRules();
                int precSize = FFTManager.currFFT.getAmountOfPreconditions();
                // strategy might've been manually changed, so we need to find reachable states first
                if (USE_OPTIMIZED_MINIMIZE)
                    FFTAutoGen.findReachableStates();

                FFTAutoGen.minimize(currFFT);
                int diffRules = ruleSize - FFTManager.currFFT.getAmountOfRules();
                int diffPrecs = precSize - FFTManager.currFFT.getAmountOfPreconditions();
                if (diffRules == 0 && diffPrecs == 0) {
                    msg = "FFT is already fully minimized";
                    changes = tempChanges;
                } else {
                    pushUndoStack();
                    msg = diffRules + " rules and " + diffPrecs + " preconditions were removed";
                }
            }

            System.out.println(msg);
            playMsg(msg, 5);
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        }

    }

    public class VerifyFFTPane extends VBox {
        VerifyFFTPane() {

            Label label = new Label("Verify the FFT: " + currFFT.getName());
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);

            Button verifyWeak = new Button("Verify weakly optimal");
            verifyWeak.setOnMouseClicked(event -> {
                verifyAndClose(false);
            });

            Button verifyStrong = new Button("Verify strongly optimal");
            verifyStrong.setOnMouseClicked(event -> {
                verifyAndClose(true);
            });

            HBox btnBox = new HBox(20, verifyWeak, verifyStrong);
            btnBox.setAlignment(Pos.CENTER);

            setSpacing(20);
            setPadding(new Insets(20, 0, 20, 0));
            setAlignment(Pos.CENTER);

            getChildren().addAll(label, btnBox);
        }

        private void verifyAndClose(boolean complete) {
            boolean verified = currFFT.verify(AUTOGEN_TEAM, complete);
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            if (!verified) {
                if (currFFT.getFailingPoint() == null) {
                    playMsg("Strategy is not optimal", 5);
                } else {
                    stage = (Stage) thisPane.getScene().getWindow();
                    Scene thisScene = stage.getScene();
                    stage.setScene(new Scene(new FFTFailurePane(thisScene, thisPane), WIDTH, HEIGHT));
                }
            } else {
                playMsg("Strategy was successfully verified", 5);
            }
        }
    }

    public class GenerateFFTPane extends VBox {

        GenerateFFTPane() {

            Label label = new Label("Synthesize an optimal strategy for " + FFTManager.getPlayerName(AUTOGEN_TEAM));
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);

            Button genBtn = new Button("Synthesize");
            genBtn.setOnMouseClicked(event -> {
                generateAndClose();
            });

            HBox btnBox = new HBox(10, genBtn);
            btnBox.setAlignment(Pos.CENTER);

            setSpacing(20);
            setPadding(new Insets(20, 0, 20, 0));
            setAlignment(Pos.CENTER);

            getChildren().addAll(label, btnBox);
        }

        private void generateAndClose() {
            boolean tempChanges = changes;

            String msg;
            if (!currFFT.verify(AUTOGEN_TEAM, false)) { // error, not a winning strategy
                msg = "The strategy is not weakly optimal, so can't generate a strongly optimal strategy";
                changes = tempChanges;
            }
            else if (currFFT.verify(AUTOGEN_TEAM, true)) {
                msg = "Strategy is already strongly optimal";
                changes = tempChanges;
            } else {
                FFTAutoGen.synthesize(currFFT);
                msg = "Strongly optimal strategy succesfully generated";
                pushUndoStack();
            }
            System.out.println(msg);
            playMsg(msg, 5);
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        }

    }

    private static class UndoItem {
        public ArrayList<FFT> ffts;
        public int fftIndex;
        public int selectedIndex;

        public UndoItem(int selectedIndex) {
            this.ffts = new ArrayList<>();
            for (FFT f : FFTManager.ffts)
                this.ffts.add(new FFT(f));
            this.fftIndex = FFTManager.fftIndex;
            this.selectedIndex = selectedIndex;
        }
    }

}
