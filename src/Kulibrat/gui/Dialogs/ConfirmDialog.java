package gui.Dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public abstract class ConfirmDialog extends AnchorPane {
    public ConfirmDialog(String labelText) {

        Label label = new Label(labelText);
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        AnchorPane.setTopAnchor(label, 20.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);

        Button yesBtn = new Button("Yes");
        HBox yes = new HBox(yesBtn);
        yes.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(yes, 40.0);
        AnchorPane.setTopAnchor(yes, 0.0);
        AnchorPane.setBottomAnchor(yes, 0.0);
        yesBtn.setOnMouseClicked(event -> {
            setYesBtnMouseClicked();
        });

        Button noBtn = new Button("No");
        HBox no = new HBox(noBtn);
        no.setAlignment(Pos.CENTER);
        AnchorPane.setRightAnchor(no, 40.0);
        AnchorPane.setTopAnchor(no, 0.0);
        AnchorPane.setBottomAnchor(no, 0.0);
        noBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });

        getChildren().addAll(label, yes, no);
    }


    public abstract void setYesBtnMouseClicked();
}
