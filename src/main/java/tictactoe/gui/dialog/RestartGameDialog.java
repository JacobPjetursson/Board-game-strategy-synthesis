package tictactoe.gui.dialog;


import javafx.stage.Stage;
import tictactoe.game.Controller;
import tictactoe.game.State;

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
                cont.getPlayerInstance(PLAYER2), new State());
    }
}
