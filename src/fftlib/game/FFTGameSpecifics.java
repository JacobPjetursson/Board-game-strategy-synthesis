package fftlib.game;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Rule;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;

public interface FFTGameSpecifics {

    FFTMove actionToMove(Action a);

    FFTState clauseToState(Clause c);

    String getFFTFilePath();

    int[] getBoardDim(); // row, col

    int[] getSymmetries();

    FFTState getInitialState();

    FFTLogic getLogic();

    FFTDatabase getDatabase();

    FFTFailState getFailState();

    InteractiveFFTState getInteractiveState();
}
