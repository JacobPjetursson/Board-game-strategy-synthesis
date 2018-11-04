package FFT;

import game.Controller;
import gui.Dialogs.DeleteFFTDialog;
import gui.Dialogs.DeleteRGDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import misc.Database;
import misc.Config;

import static misc.Config.WIDTH;

public class EditFFTScene extends VBox {
    private int textFieldWidth = 150;
    private ListView<BorderPane> lw;
    private FFTManager fftManager;
    private Label title;
    private Button delBtn, renameBtn, newBtn, addNewRuleGroupBtn, verifyBtn;
    private ComboBox<String> changeBox;

    public EditFFTScene(Stage primaryStage, Scene prevScene, FFTManager fftManager, Controller cont) {
        setSpacing(15);
        setAlignment(Pos.CENTER);
        this.fftManager = fftManager;
        title = new Label();
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        HBox labelBox = new HBox(title);
        labelBox.setAlignment(Pos.CENTER);

        VBox changeRenameBox = new VBox(5);
        changeRenameBox.setAlignment(Pos.CENTER);
        changeBox = new ComboBox<>();
        changeBox.setPromptText("Change FFT");
        changeBox.setMinWidth(100);
        // set items
        changeBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            // In case of clear selection
            if ((Integer)newValue == -1)
                return;
            fftManager.setCurrFFT((Integer)newValue);
            Platform.runLater(() -> changeBox.getSelectionModel().clearSelection());
            update();
        });
        changeRenameBox.getChildren().add(changeBox);

        renameBtn = new Button("Rename FFT");
        renameBtn.setMinWidth(100);
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

        VBox delNewBox = new VBox(5);
        delNewBox.setAlignment(Pos.CENTER);
        delBtn = new Button("Delete FFT");
        delBtn.setMinWidth(100);
        delBtn.setStyle("-fx-border-color: #000000; -fx-background-color: #ff0000;");
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
        newBtn.setMinWidth(100);
        newBtn.setStyle("-fx-border-color: #000000; -fx-background-color: blue;");
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

        // New rule group
        Label newRuleGroupLabel = new Label("New rule group: ");
        newRuleGroupLabel.setFont(Font.font("Verdana", 15));
        TextField newRuleGroupField = new TextField();
        newRuleGroupField.setMinWidth(textFieldWidth);
        newRuleGroupField.setMaxWidth(textFieldWidth);

        Text text = new Text("Add");
        text.setFill(Color.WHITE);
        addNewRuleGroupBtn = new Button("Add");
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
        teamChoice.setValue("Red");
        teamChoice.setItems(FXCollections.observableArrayList("Red", "Black"));

        Label forLabel = new Label(" for: ");
        forLabel.setFont(Font.font("Verdana", 15));
        ChoiceBox<String> verificationChoice = new ChoiceBox<>();
        verificationChoice.setMinWidth(textFieldWidth);
        verificationChoice.setMaxWidth(textFieldWidth);
        verificationChoice.setValue("Whole FFT");
        verificationChoice.setItems(FXCollections.observableArrayList("Whole FFT", "Existing Rules"));

        Label verifiedLabel = new Label("The FFT was successfully verified");
        verifiedLabel.setFont(Font.font("Verdana", 15));

        verifyBtn = new Button("Verify FFT");
        verifyBtn.setTooltip(new Tooltip("Checks if the current FFT is a winning strategy,\n" +
                "or if given rules are part of winning strategy"));
        verifyBtn.setOnMouseClicked(event -> {
            if (!Database.connectAndVerify())
                return;
            int team = teamChoice.getSelectionModel().getSelectedIndex() + 1;
            boolean wholeFFT = verificationChoice.getSelectionModel().getSelectedIndex() == 0;
            boolean verified = fftManager.currFFT.verify(team, wholeFFT);
            if (!verified && fftManager.currFFT.failingPoint != null) {
                Scene scene = primaryStage.getScene();
                primaryStage.setScene(new Scene(new FFTFailurePane(scene, fftManager, cont), Config.WIDTH, Config.HEIGHT));
            } else if (verified && !getChildren().contains(verifiedLabel)) {
                getChildren().add(4, verifiedLabel);

            }
        });
        HBox verifyBox = new HBox();
        verifyBox.setAlignment(Pos.CENTER);
        verifyBox.setSpacing(10);
        verifyBox.getChildren().addAll(verifyBtn, teamChoice, forLabel, verificationChoice);



        Button back = new Button("Back");
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });
        HBox bottomBox = new HBox(10);
        VBox.setMargin(bottomBox, new Insets(10));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.getChildren().add(back);

        setVgrow(lw, Priority.ALWAYS);
        getChildren().addAll(topPane, lw, ruleGroupBox, verifyBox, bottomBox);
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
        }
        else
            title.setText("Edit FFT with name:\n" + fftManager.currFFT.name);
        // Items in changeBox (combobox)
        ObservableList<String> fftStrs = FXCollections.observableArrayList();
        for (FFT fft : FFTManager.ffts) {
            fftStrs.add(fft.name);
        }
        changeBox.setItems(fftStrs);
        // ListView
        ObservableList<BorderPane> ruleGroups = FXCollections.observableArrayList();
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            final int index = i; // FUCKING JAVA CANCER
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(i);
            VBox rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER);
            Label rgLabel = new Label((i + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 16));
            rgVBox.getChildren().add(rgLabel);
            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r.printRule());
                rLabel.setFont(Font.font("Verdana", 10));
                rgVBox.getChildren().add(rLabel);
            }
            // up/down list buttons
            int buttonSize = 150;
            VBox upDownButtons = new VBox();
            upDownButtons.setAlignment(Pos.CENTER);
            Button upButton = new Button("▲");
            Button downButton = new Button("▼");
            upButton.setMinWidth(50);
            downButton.setMinWidth(50);
            upButton.setOnMouseClicked(event -> {
                if (index == 0)
                    return;
                fftManager.currFFT.ruleGroups.remove(index);
                fftManager.currFFT.ruleGroups.add(index - 1, rg);
                FFTManager.save();
                update();
                lw.getSelectionModel().select(index - 1);
            });
            downButton.setOnMouseClicked(event -> {
                if (index == fftManager.currFFT.ruleGroups.size() - 1)
                    return;
                fftManager.currFFT.ruleGroups.remove(index);
                fftManager.currFFT.ruleGroups.add(index + 1, rg);
                FFTManager.save();
                update();
                lw.getSelectionModel().select(index + 1);
            });

            // Edit / Remove buttons
            VBox editRemoveButtons = new VBox(10);
            editRemoveButtons.setAlignment(Pos.CENTER);

            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-border-color: #000000; -fx-background-color: blue;");
            editButton.setMinWidth(buttonSize);
            editButton.setOnMouseClicked(event -> {
                Stage newStage = new Stage();
                newStage.setScene(new Scene(
                        new EditRuleGroupPane(rg, this), 700, 500));
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(getScene().getWindow());
                newStage.setOnCloseRequest(Event::consume);
                newStage.show();
            });

            Button removeButton = new Button("Remove");
            removeButton.setStyle("-fx-border-color: #000000; -fx-background-color: #ff0000;");
            removeButton.setMinWidth(buttonSize);
            removeButton.setOnMouseClicked(event -> {
                // Confirmation (Pretty big delete after all)
                String labelText = "Are you sure you want to delete the rule group:\n" + rg.name;
                Stage newStage = new Stage();
                newStage.setScene(new Scene(
                        new DeleteRGDialog(labelText, this, index), 500, 150));
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(getScene().getWindow());
                newStage.setOnCloseRequest(Event::consume);
                newStage.show();

            });
            editRemoveButtons.getChildren().addAll(editButton, removeButton);
            upDownButtons.getChildren().addAll(upButton, downButton);
            HBox allButtons = new HBox(editRemoveButtons, upDownButtons);
            allButtons.setAlignment(Pos.CENTER);
            allButtons.setSpacing(5);

            BorderPane finalPane = new BorderPane();
            finalPane.setCenter(rgVBox);
            finalPane.setRight(allButtons);
            ruleGroups.add(finalPane);
        }
        lw.setItems(ruleGroups);
        lw.getSelectionModel().selectLast();
    }

    public void removeRuleGroup(int index) {
        lw.getItems().remove(index);
        fftManager.currFFT.ruleGroups.remove(index);
        update();
        FFTManager.save();
    }

    // Can be either rename or new fft
    public class NameFFTPane extends RenamePane {
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
