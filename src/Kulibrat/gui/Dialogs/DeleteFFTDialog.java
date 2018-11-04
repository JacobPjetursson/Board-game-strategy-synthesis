package gui.Dialogs;

import FFT.EditFFTScene;
import FFT.FFTManager;
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
