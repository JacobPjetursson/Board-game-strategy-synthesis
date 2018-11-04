package gui.Dialogs;

import game.Controller;
import game.State;
import javafx.stage.Stage;

import static misc.Config.BLACK;
import static misc.Config.RED;

public class RestartGameDialog extends ConfirmDialog {

    Controller cont;
    Stage primaryStage;
    public RestartGameDialog(String labelText, Stage primaryStage, Controller cont) {
        super(labelText);
        this.primaryStage = primaryStage;
        this.cont = cont;
    }

    @Override
    public void setYesBtnMouseClicked() {
        Stage stage = (Stage) getScene().getWindow();
        stage.close();
        new Controller(primaryStage, cont.getPlayerInstance(RED),
                cont.getPlayerInstance(BLACK), new State(), cont.getTime(RED), cont.getTime(BLACK), cont.getOverwriteDB());
    }
}
