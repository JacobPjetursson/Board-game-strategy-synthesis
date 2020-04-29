package kulibrat.gui.Dialogs;


import javafx.stage.Stage;
import kulibrat.game.Controller;
import kulibrat.game.Node;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;


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
                cont.getPlayerInstance(PLAYER2), new Node(), cont.getTime(PLAYER1), cont.getTime(PLAYER2), cont.getOverwriteDB());
    }
}
