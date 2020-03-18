package fftlib.gui;

import fftlib.FFT;
import fftlib.FFTManager;
import fftlib.Rule;
import fftlib.RuleGroup;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
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

import static fftlib.FFTManager.*;
import static misc.Globals.WIDTH;

public class FFTOverviewPane extends VBox {
    private int textFieldWidth = 150;
    private ListView<RulePane> lw;
    private FFTManager fftManager;
    private Label title;
    private Button delBtn, renameBtn, newBtn, addNewRuleGroupBtn, verifyBtn;
    private ComboBox<String> changeBox;

    private FFTInteractivePane interactivePane;

    public FFTOverviewPane(Stage primaryStage, FFTManager fftManager, FFTInteractivePane interactivePane) {
        setSpacing(15);
        setAlignment(Pos.CENTER);
        this.interactivePane = interactivePane;

        Scene prevScene = primaryStage.getScene();

        this.fftManager = fftManager;
        title = new Label();
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        HBox labelBox = new HBox(title);
        labelBox.setAlignment(Pos.CENTER);

        VBox changeRenameBox = new VBox();
        changeRenameBox.setAlignment(Pos.CENTER);
        changeRenameBox.setPadding(new Insets(0, 0, 0, 5));
        changeBox = new ComboBox<>();
        changeBox.setPromptText("Change FFT");
        changeBox.setStyle("-fx-font: 16px \"Verdana\";");
        changeBox.setMinWidth(180);
        changeBox.setMaxWidth(180);
        // set items
        changeBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            // In case of clear selection
            if ((Integer) newValue == -1)
                return;
            fftManager.setCurrFFT((Integer) newValue);
            Platform.runLater(() -> changeBox.getSelectionModel().clearSelection());
            update();
            interactivePane.refresh(true);
        });
        changeRenameBox.getChildren().add(changeBox);

        renameBtn = new Button("Rename FFT");
        renameBtn.setMinWidth(180);
        renameBtn.setMaxWidth(180);
        renameBtn.setFont(Font.font("Verdana", 16));
        renameBtn.setOnAction(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameFFTPane("Rename your FFT", true), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });
        changeRenameBox.getChildren().add(renameBtn);

        VBox delNewBox = new VBox();
        delNewBox.setAlignment(Pos.CENTER);
        delBtn = new Button("Delete FFT");
        delBtn.setMinWidth(150);
        delBtn.setFont(Font.font("Verdana", 16));
        delBtn.setStyle(redBtnStyle);
        delBtn.setOnAction(event -> {
            Stage newStage = new Stage();
            String labelText = "Are you sure you want to delete the FFT:\n" +
                    fftManager.currFFT.name + "?";
            newStage.setScene(new Scene(new DeleteFFTDialog(labelText, this, fftManager), 500, 150));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });
        delNewBox.getChildren().add(delBtn);

        newBtn = new Button("New FFT");
        newBtn.setMinWidth(150);
        newBtn.setStyle(blueBtnStyle);
        newBtn.setFont(Font.font("Verdana", 16));
        newBtn.setOnAction(event -> {
            Stage newStage = new Stage();
            newStage.setScene(new Scene(
                    new NameFFTPane("Name your new FFT", false), 500, 200));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(getScene().getWindow());
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        });
        delNewBox.getChildren().add(newBtn);

        GridPane topPane = new GridPane();
        topPane.setAlignment(Pos.CENTER);
        ColumnConstraints columnSides = new ColumnConstraints(WIDTH / 5);
        ColumnConstraints columnMid = new ColumnConstraints((WIDTH / 5) * 3);
        topPane.getColumnConstraints().add(columnSides);
        topPane.getColumnConstraints().add(columnMid);
        topPane.getColumnConstraints().add(columnSides);
        topPane.add(labelBox, 1, 0);
        topPane.add(changeRenameBox, 0, 0);
        topPane.add(delNewBox, 2, 0);

        // Existing rule groups
        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setCellFactory(param -> new RuleCell());

        // New rule group
        Label newRuleGroupLabel = new Label("New rule group: ");
        newRuleGroupLabel.setFont(Font.font("Verdana", 16));
        TextField newRuleGroupField = new TextField();
        newRuleGroupField.setMinWidth(textFieldWidth);
        newRuleGroupField.setMaxWidth(textFieldWidth);

        addNewRuleGroupBtn = new Button("Add");
        addNewRuleGroupBtn.setFont(Font.font("Verdana", 16));
        addNewRuleGroupBtn.setOnMouseClicked(event -> {
            RuleGroup rg = new RuleGroup(newRuleGroupField.getText());
            fftManager.currFFT.addRuleGroup(rg);
            newRuleGroupField.clear();
            update();
            interactivePane.refresh(true);
        });

        HBox ruleGroupBox = new HBox(newRuleGroupLabel, newRuleGroupField, addNewRuleGroupBtn);
        ruleGroupBox.setAlignment(Pos.CENTER);

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
        verificationChoice.setValue("Completely");
        verificationChoice.setStyle("-fx-font: 16px \"Verdana\";");
        verificationChoice.setItems(FXCollections.observableArrayList("Completely", "Partially"));

        verifyBtn = new Button("Verify FFT");
        verifyBtn.setFont(Font.font("Verdana", 16));
        verifyBtn.setTooltip(new Tooltip("Checks if the current FFT is a winning strategy,\n" +
                "or if given rules are part of winning strategy"));
        verifyBtn.setOnMouseClicked(event -> {
            int team = teamChoice.getSelectionModel().getSelectedIndex() + 1;
            boolean wholeFFT = verificationChoice.getSelectionModel().getSelectedIndex() == 0;
            boolean verified = fftManager.currFFT.verify(team, wholeFFT);
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
                    Scene scene = primaryStage.getScene();
                    primaryStage.setScene(new Scene(new FFTFailurePane(scene, fftManager, interactivePane), WIDTH, Globals.HEIGHT));
                }
            } else {
                Label verifiedLabel = new Label("The FFT was successfully verified");
                verifiedLabel.setFont(Font.font("Verdana", 16));
                getChildren().add(4, verifiedLabel);

                Timeline timeline = new Timeline(new KeyFrame(
                        Duration.millis(2500),
                        ae -> getChildren().remove(verifiedLabel)));
                timeline.play();


            }
        });

        HBox verifyBox = new HBox();
        verifyBox.setAlignment(Pos.CENTER);
        verifyBox.setSpacing(10);
        verifyBox.getChildren().addAll(verifyBtn, teamChoice, verificationChoice);

        Button intEditBtn = new Button("Interactive Editing Mode");
        intEditBtn.setFont(Font.font("Verdana", 16));
        intEditBtn.setAlignment(Pos.CENTER);
        intEditBtn.setOnMouseClicked((event -> {
            Scene scene = primaryStage.getScene();
            primaryStage.setScene(interactivePane.getScene());
            interactivePane.setPrevScene(scene);
            interactivePane.setUpdateFunc(this::update);
        }));


        Button back = new Button("Back");
        back.setFont(Font.font("Verdana", 16));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });
        HBox bottomBox = new HBox(10);
        VBox.setMargin(bottomBox, new Insets(10));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.getChildren().add(back);

        setVgrow(lw, Priority.ALWAYS);
        getChildren().addAll(topPane, lw, ruleGroupBox, verifyBox, intEditBtn, bottomBox);
        update();
    }

    public void update() {
        boolean isNull = fftManager.currFFT == null;
        changeBox.setDisable(isNull);
        delBtn.setDisable(isNull);
        renameBtn.setDisable(isNull);
        addNewRuleGroupBtn.setDisable(isNull);
        verifyBtn.setDisable(isNull);
        if (isNull) {
            title.setText("Please make a new FFT");
            return;
        } else
            title.setText(fftManager.currFFT.name);
        // Items in changeBox (combobox)
        ObservableList<String> fftStrs = FXCollections.observableArrayList();
        for (FFT fft : FFTManager.ffts) {
            fftStrs.add(fft.name);
        }
        changeBox.setItems(fftStrs);
        showRuleGroups();
        lw.getSelectionModel().selectLast();
    }

    private void showRuleGroups() {
        // ListView
        ObservableList<RulePane> ruleGroups = FXCollections.observableArrayList();
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            ruleGroups.add(new RulePane(i));
        }
        lw.setItems(ruleGroups);
    }

    private class RulePane extends BorderPane {

        int idx;
        VBox rgVBox;

        RulePane(int idx) { // Rulegroup entry
            super();
            this.idx = idx;
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(idx);
            rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER_LEFT);

            /* Uncomment for RG name
            Label rgLabel = new Label((idx + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 18));
            rgVBox.getChildren().add(rgLabel);
            */

            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r);
                rLabel.setFont(Font.font("Verdana", 13));
                rgVBox.getChildren().add(rLabel);
            }
            setCenter(rgVBox);
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

                    RuleGroup rg_dragged = fftManager.currFFT.ruleGroups.get(draggedPane.idx);
                    fftManager.currFFT.ruleGroups.remove(rg_dragged);
                    fftManager.currFFT.ruleGroups.add(droppedIdx, rg_dragged);

                    event.setDropCompleted(true);
                    showRuleGroups();
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

    // Can be either rename or new fft
    private class NameFFTPane extends InputPane {
        private boolean rename;

        NameFFTPane(String labelText, boolean rename) {
            super(labelText);
            this.rename = rename;
        }

        @Override
        void setSaveBtnMouseClicked() {
            if (!tf.getText().replace(" ", "").isEmpty()) {
                if (rename)
                    fftManager.currFFT.name = tf.getText();
                else
                    fftManager.addNewFFT(tf.getText());
                Stage stage = (Stage) getScene().getWindow();
                stage.close();
                update();
                interactivePane.refresh(true);

                save();
            }
        }
    }
}
