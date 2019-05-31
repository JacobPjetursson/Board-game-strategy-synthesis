package fftlib.gui;

import fftlib.FFTManager;
import javafx.stage.Stage;

public class DeleteFFTDialog extends ConfirmDialog {
    FFTManager fftManager;
    FFTOverviewPane FFTOverviewPane;

    public DeleteFFTDialog(String labelText, FFTOverviewPane FFTOverviewPane, FFTManager fftManager) {
        super(labelText);
        this.fftManager = fftManager;
        this.FFTOverviewPane = FFTOverviewPane;
    }

    @Override
    public void setYesBtnMouseClicked() {
        Stage stage = (Stage) getScene().getWindow();
        stage.close();
        fftManager.deleteCurrFFT();
        FFTOverviewPane.update();
    }


}
