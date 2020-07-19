package fftlib.gui;

import fftlib.FFTManager;
import javafx.stage.Stage;

public class DeleteFFTDialog extends ConfirmDialog {
    FFTOverviewPane FFTOverviewPane;

    public DeleteFFTDialog(String labelText, FFTOverviewPane FFTOverviewPane) {
        super(labelText);
        this.FFTOverviewPane = FFTOverviewPane;
    }

    @Override
    public void setYesBtnMouseClicked() {
        Stage stage = (Stage) getScene().getWindow();
        stage.close();
        FFTManager.deleteCurrFFT();
        //FFTOverviewPane.update();
    }


}
