package kulibrat.gui.Dialogs;


import javafx.stage.Stage;
import kulibrat.game.Controller;
import kulibrat.game.State;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


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
        new Controller(primaryStage, cont.getPlayerInstance(PLAYER1),
                cont.getPlayerInstance(PLAYER2), new State(), cont.getTime(PLAYER1), cont.getTime(PLAYER2), cont.getOverwriteDB());
    }
}