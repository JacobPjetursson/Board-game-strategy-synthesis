package fftlib.gui;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

import static fftlib.FFTManager.*;
import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class EditFFTInteractive extends BorderPane {
    private FFTManager fftManager;
    private ListView<RulePane> lw;
    private InteractiveFFTState interactiveFFTState;
    private Label ruleLabel;

    private RadioButton p1Btn;
    private RadioButton p2Btn;

    private int[] selectedIndices; // rgIdx, rIdx
    public EditFFTInteractive(Scene prevScene, FFTManager fftManager) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.fftManager = fftManager;

        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);

        Label title = new Label("Edit the FFT Interactively");
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
        lw.setPrefWidth(520);
        lw.setCellFactory(param -> new RuleCell());
        showRuleGroups();
        BorderPane.setMargin(lw, new Insets(15));
        setRight(lw);

        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label label1 = new Label("Click the rules to show them on the board");
        label1.setFont(Font.font("Verdana", 16));

        Label label2 = new Label("Click the board tiles to edit their contents");
        label2.setFont(Font.font("Verdana", 16));

        Label label3 = new Label("Set rule perspective:");
        label3.setFont(Font.font("Verdana", 16));

        p1Btn = new RadioButton("Player 1");
        p1Btn.setFont(Font.font("Verdana", 16));
        p2Btn = new RadioButton("Player 2");
        p2Btn.setFont(Font.font("Verdana", 16));
        ToggleGroup toggleGrp = new ToggleGroup();
        p1Btn.setToggleGroup(toggleGrp);
        p2Btn.setToggleGroup(toggleGrp);
        HBox perspectiveGrp = new HBox(10, label3, p1Btn, p2Btn);
        perspectiveGrp.setAlignment(Pos.CENTER);
        p1Btn.setOnMouseClicked(event -> interactiveFFTState.setPerspective(PLAYER1));
        p2Btn.setOnMouseClicked(event -> interactiveFFTState.setPerspective(PLAYER2));


        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 16));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });

        Button addRuleBtn = new Button("Add rule");
        addRuleBtn.setStyle(greenBtnStyle);
        addRuleBtn.setFont(Font.font("Verdana", 16));
        addRuleBtn.setOnMouseClicked(event -> {
            Rule r = interactiveFFTState.getRule();
            if (!Rule.isValidRuleFormat(r)) {
                return;
            }
            ArrayList<RuleGroup> ruleGroups = fftManager.currFFT.ruleGroups;
            if (ruleGroups.isEmpty())
                ruleGroups.add(new RuleGroup(""));
            Rule copy = new Rule(r);
            fftManager.currFFT.ruleGroups.get(ruleGroups.size() - 1).addRule(copy);
            showRuleGroups();
            FFTManager.save();
        });

        Button deleteRuleBtn = new Button("Delete rule");
        deleteRuleBtn.setStyle(redBtnStyle);
        deleteRuleBtn.setFont(Font.font("Verdana", 16));
        deleteRuleBtn.setOnMouseClicked(event -> {
            if (selectedIndices == null) {
                System.err.println("Please select a rule and try again");
                return;
            }
            int rgIdx = selectedIndices[0];
            int rIdx = selectedIndices[1];
            fftManager.currFFT.ruleGroups.get(rgIdx).rules.remove(rIdx);
            showRuleGroups();
            FFTManager.save();
        });

        Button clearRuleBtn = new Button("Clear rule");
        clearRuleBtn.setFont(Font.font("Verdana", 16));
        clearRuleBtn.setOnMouseClicked(event -> {
            selectedIndices = null;
            FFTManager.interactiveState.clear();
            update(FFTManager.initialFFTState);
        });
        VBox infoBox = new VBox(7);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(label1, label2, perspectiveGrp);

        VBox clearBox = new VBox(10, clearRuleBtn);
        clearBox.setAlignment(Pos.CENTER);


        HBox ruleBtnBox = new HBox(10, addRuleBtn, deleteRuleBtn, clearRuleBtn);
        ruleBtnBox.setAlignment(Pos.CENTER);

        VBox backBox = new VBox();
        backBox.setAlignment(Pos.CENTER_RIGHT);
        backBox.getChildren().add(back);

        BorderPane bp = new BorderPane();
        bp.setCenter(ruleBtnBox);
        BorderPane.setMargin(ruleBtnBox, new Insets(0, 0, 0, 50));
        bp.setRight(backBox);


        bottomBox.getChildren().addAll(infoBox, bp);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);

        setupRuleFetchTimer();
        update(FFTManager.initialFFTState);

    }

    public void update(FFTState state) {
        Node node = FFTManager.interactiveState.getInteractiveNode(state);
        if (state.getTurn() == PLAYER1)
            p1Btn.setSelected(true);
        else
            p2Btn.setSelected(true);
        addInteractiveNode(node);
    }

    private void addInteractiveNode(Node node) {
        setCenter(node);
        BorderPane.setMargin(node, new Insets(0, 0, 0, 0));
        BorderPane.setAlignment(node, Pos.CENTER);
    }

    public void update(FFTState state, FFTMove move) {
        update(state);
        FFTManager.interactiveState.setAction(move.getAction());
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
            rules.add(new RulePane(i));
            for (int j = 0; j < rg.rules.size(); j++) {
                rules.add(new RulePane(i, j));
            }
        }
        lw.setItems(rules);

    }

    private class RulePane extends StackPane {
        boolean isRuleGroup;
        int rgIdx, rIdx;
        Label label;

        RulePane(int rgIdx) { // Rulegroup entry
            super();
            setAlignment(Pos.CENTER);
            this.isRuleGroup = true;
            this.rgIdx = rgIdx;

            RuleGroup rg = fftManager.currFFT.ruleGroups.get(rgIdx);
            this.label = new Label((rgIdx + 1) + ": " + rg.name);
            label.setFont(Font.font("Verdana", 18));
            getChildren().add(label);

            setOnMousePressed(Event::consume);
        }

        RulePane(int rgIdx, int rIdx) { // rule entry
            super();
            this.isRuleGroup = false;
            this.rgIdx = rgIdx;
            this.rIdx = rIdx;

            RuleGroup rg = fftManager.currFFT.ruleGroups.get(rgIdx);
            setOnMouseClicked(event -> {
                addInteractiveNode(interactiveFFTState.getInteractiveNode(rg.rules.get(rIdx)));
                selectedIndices = new int[]{rgIdx, rIdx};
            });
            Rule r = rg.rules.get(rIdx);
            this.label = new Label((rIdx + 1) + ": " + r);
            label.setFont(Font.font("Verdana", 13));
            getChildren().add(label);
        }
    }

    private class RuleCell extends ListCell<RulePane> {

        RuleCell() {
            ListCell thisCell = this;

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);

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
                        RuleGroup rg_dropped = fftManager.currFFT.ruleGroups.get(droppedPane.rgIdx);
                        rg_dropped.rules.add(droppedPane.rIdx, r_dragged);
                        selectionIdx = droppedIdx;

                        rgSelectionIdx = droppedPane.rgIdx;
                        rSelectionIdx = droppedPane.rIdx;
                    }
                    event.setDropCompleted(true);
                    showRuleGroups();
                    FFTManager.save();
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
}
