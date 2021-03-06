package fftlib.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public abstract class InputPane extends VBox {
    TextField textField;

    public InputPane(String labelText) {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPadding(new Insets(10));
        Label label = new Label(labelText);
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        label.setAlignment(Pos.CENTER);

        textField = new TextField();
        textField.setMaxWidth(200);

        Button saveBtn = new Button("Save");
        saveBtn.setAlignment(Pos.CENTER);
        saveBtn.setOnMouseClicked(event -> {
            setSaveBtnMouseClicked();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setAlignment(Pos.CENTER);
        cancelBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });

        getChildren().addAll(label, textField, saveBtn, cancelBtn);
    }

    abstract void setSaveBtnMouseClicked();
}
