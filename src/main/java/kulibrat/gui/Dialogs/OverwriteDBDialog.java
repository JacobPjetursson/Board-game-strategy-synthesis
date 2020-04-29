package kulibrat.gui.Dialogs;

import javafx.stage.Stage;
import kulibrat.misc.Database;

public class OverwriteDBDialog extends ConfirmDialog {

    public OverwriteDBDialog(String labelText) {
        super(labelText);
    }

    @Override
    public void setYesBtnMouseClicked() {
        Stage stage = (Stage) getScene().getWindow();
        stage.close();
        Database.buildLookupDB();
    }
}
