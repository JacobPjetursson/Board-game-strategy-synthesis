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
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import misc.Config;

import java.util.Timer;
import java.util.TimerTask;

import static fftlib.FFTManager.SERIALIZED_MIME_TYPE;
import static fftlib.FFTManager.blueBtnStyle;
import static fftlib.FFTManager.redBtnStyle;
import static misc.Config.WIDTH;

public class EditFFTScene extends VBox {
    private int textFieldWidth = 150;
    private ListView<RulePane> lw;
    private FFTManager fftManager;
    private Label title;
    private Button delBtn, renameBtn, newBtn, addNewRuleGroupBtn, verifyBtn;
    private ComboBox<String> changeBox;
    private EditFFTScene thisScene = this;

    public EditFFTScene(Stage primaryStage, Scene prevScene, FFTManager fftManager) {
        setSpacing(15);
        setAlignment(Pos.CENTER);

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
        // Make box here with rename, change, delete (not now)

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
        });

        HBox ruleGroupBox = new HBox(newRuleGroupLabel, newRuleGroupField, addNewRuleGroupBtn);
        ruleGroupBox.setAlignment(Pos.CENTER);

        Label teamLabel = new Label(" as team: ");
        teamLabel.setFont(Font.font("Verdana", 15));
        ChoiceBox<String> teamChoice = new ChoiceBox<>();
        teamChoice.setMinWidth(textFieldWidth);
        teamChoice.setMaxWidth(textFieldWidth);
        teamChoice.setValue("Player 1");
        teamChoice.setStyle("-fx-font: 16px \"Verdana\";");
        teamChoice.setItems(FXCollections.observableArrayList("Player 1", "Player 2"));

        Label forLabel = new Label(" for: ");
        forLabel.setFont(Font.font("Verdana", 16));
        ChoiceBox<String> verificationChoice = new ChoiceBox<>();
        verificationChoice.setMinWidth(textFieldWidth);
        verificationChoice.setMaxWidth(textFieldWidth);
        verificationChoice.setValue("Whole FFT");
        verificationChoice.setStyle("-fx-font: 16px \"Verdana\";");
        verificationChoice.setItems(FXCollections.observableArrayList("Whole FFT", "Existing Rules"));

        verifyBtn = new Button("Verify FFT");
        verifyBtn.setFont(Font.font("Verdana", 16));
        verifyBtn.setTooltip(new Tooltip("Checks if the current FFT is a winning strategy,\n" +
                "or if given rules are part of winning strategy"));
        verifyBtn.setOnMouseClicked(event -> {
            if (!FFTManager.db.connectAndVerify())
                return;
            int team = teamChoice.getSelectionModel().getSelectedIndex() + 1;
            boolean wholeFFT = verificationChoice.getSelectionModel().getSelectedIndex() == 0;
            boolean verified = fftManager.currFFT.verify(team, wholeFFT);
            if (!verified) {
                if (fftManager.currFFT.failingPoint == null) {
                    Label verifiedLabel = new Label("Unable to win vs. perfect player");
                    verifiedLabel.setFont(Font.font("Verdana", 16));
                    getChildren().add(4, verifiedLabel);

                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.millis(2500),
                            ae -> getChildren().remove(verifiedLabel)));
                    timeline.play();
                } else {
                    Scene scene = primaryStage.getScene();
                    primaryStage.setScene(new Scene(new FFTFailurePane(scene, fftManager), WIDTH, Config.HEIGHT));
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
        verifyBox.getChildren().addAll(verifyBtn, teamChoice, forLabel, verificationChoice);

        Button intEditBtn = new Button("Interactive Editing Mode");
        intEditBtn.setFont(Font.font("Verdana", 16));
        intEditBtn.setAlignment(Pos.CENTER);
        intEditBtn.setOnMouseClicked((event -> {
            Scene scene = primaryStage.getScene();

            primaryStage.setScene(new Scene(
                    new EditFFTInteractive(scene, fftManager), WIDTH, Config.HEIGHT));
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
            title.setText("Edit FFT with name:\n" + fftManager.currFFT.name);
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

    public void removeRuleGroup(int index) {
        lw.getItems().remove(index);
        fftManager.currFFT.ruleGroups.remove(index);
        update();
        FFTManager.save();
    }

    private class RulePane extends BorderPane {

        int idx;
        VBox rgVBox;
        VBox buttons;

        RulePane(int idx) { // Rulegroup entry
            super();
            this.idx = idx;
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(idx);
            rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER);

            Label rgLabel = new Label((idx + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 18));
            rgVBox.getChildren().add(rgLabel);

            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r.print());
                rLabel.setFont(Font.font("Verdana", 13));
                rgVBox.getChildren().add(rLabel);
            }

            // Edit / Remove buttons
            int buttonSize = 150;
            buttons = new VBox();
            buttons.setAlignment(Pos.CENTER);

            Button editButton = new Button("Edit");
            editButton.setStyle(blueBtnStyle);
            editButton.setMinWidth(buttonSize);
            editButton.setFont(Font.font("Verdana", 16));
            editButton.setOnMouseClicked(event -> {
                Stage newStage = new Stage();
                newStage.setScene(new Scene(
                        new EditRuleGroupPane(rg, thisScene), 700, 500));
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(getScene().getWindow());
                newStage.setOnCloseRequest(Event::consume);
                newStage.show();
            });

            Button removeButton = new Button("Remove");
            removeButton.setStyle(redBtnStyle);
            removeButton.setFont(Font.font("Verdana", 16));
            removeButton.setMinWidth(buttonSize);
            removeButton.setOnMouseClicked(event -> {
                // Confirmation (Pretty big delete after all)
                String labelText = "Are you sure you want to delete the rule group:\n" + rg.name;
                Stage newStage = new Stage();
                newStage.setScene(new Scene(
                        new DeleteRGDialog(labelText, thisScene, idx), 500, 150));
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(getScene().getWindow());
                newStage.setOnCloseRequest(Event::consume);
                newStage.show();

            });
            buttons.getChildren().addAll(editButton, removeButton);
            setCenter(rgVBox);
            setRight(buttons);
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
                    FFTManager.save();
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
    private class NameFFTPane extends RenamePane {
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
                FFTManager.save();
                Stage stage = (Stage) getScene().getWindow();
                stage.close();
                update();
            }
        }
    }
}
