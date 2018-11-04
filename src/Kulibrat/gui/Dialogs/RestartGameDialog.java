package kulibrat.gui.Dialogs;


import javafx.stage.Stage;
import kulibrat.game.Controller;
import kulibrat.game.State;

import static kulibrat.misc.Config.BLACK;
import static kulibrat.misc.Config.RED;


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
