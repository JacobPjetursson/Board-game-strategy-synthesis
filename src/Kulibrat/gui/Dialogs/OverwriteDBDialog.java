package gui.Dialogs;

import javafx.stage.Stage;
import misc.Database;

public class OverwriteDBDialog extends ConfirmDialog {
    boolean fft;

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
