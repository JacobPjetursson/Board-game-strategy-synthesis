package FFT;

import game.Controller;
import game.Logic;
import game.Move;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import misc.Config;

public class ShowFFTPane extends BorderPane {
    private ListView<VBox> lw;
    FFTManager fftManager;
    Controller cont;

    public ShowFFTPane(FFTManager fftManager, Controller cont) {
        this.fftManager = fftManager;
        this.cont = cont;
        Label title = new Label();
        if (fftManager.currFFT == null)
            title.setText("You do not have an FFT yet");
        else
            title.setText(fftManager.currFFT.name);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefWidth(350);
        lw.setSelectionModel(new NoSelectionModel<VBox>());
        BorderPane.setMargin(lw, new Insets(15));
        setCenter(lw);

        Button close = new Button("Close");
        close.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });
        setBottom(close);
        BorderPane.setAlignment(close, Pos.CENTER);
        BorderPane.setMargin(close, new Insets(10));

        if (fftManager.currFFT != null)
            showRuleGroups();
    }

    public void showRuleGroups() {
        ObservableList<VBox> ruleGroups = FXCollections.observableArrayList();
        boolean ruleApplied = false;
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
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
                for(int symmetry : Config.SYMMETRY) {
                    if (!ruleApplied && r.applies(cont.getState(), symmetry)) {
                        // TODO - messy code below
                        Move move = r.action.getMove();
                        move.team = cont.getState().getTurn();
                        if (Logic.isLegalMove(cont.getState(), move)) {
                            rLabel.setTextFill(Color.BLUE);
                            ruleApplied = true;
                            break;
                        }
                    }
                }

                rgVBox.getChildren().add(rLabel);
            }
            ruleGroups.add(rgVBox);
        }
        lw.setItems(ruleGroups);
    }
}
