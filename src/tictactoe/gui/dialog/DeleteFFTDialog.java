package tictactoe.gui.dialog;

import fftlib.FFTManager;
import fftlib.gui.EditFFTScene;
import javafx.stage.Stage;

public class DeleteFFTDialog extends ConfirmDialog {
    FFTManager fftManager;
    EditFFTScene editFFTScene;

    public DeleteFFTDialog(String labelText, EditFFTScene editFFTScene, FFTManager fftManager) {
        super(labelText);
        this.fftManager = fftManager;
        this.editFFTScene = editFFTScene;
    }

    @Override
    public void setYesBtnMouseClicked() {
        Stage stage = (Stage) getScene().getWindow();
        stage.close();
        fftManager.deleteCurrFFT();
        editFFTScene.update();
        FFTManager.save();
    }


}
