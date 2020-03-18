package fftlib.game;

import fftlib.Action;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;

import java.util.HashSet;

public interface FFTGameSpecifics {

    FFTMove actionToMove(Action a, int team);

    FFTState preconsToState(HashSet<Literal> precons, int team);

    Rule gdlToRule(String precons, String action);

    String getFFTFilePath();

    int[] getBoardDim(); // row, col

    String[] getPlayerNames(); // VISUAL

    int[] getAllowedTransformations();

    FFTState getInitialState();

    FFTLogic getLogic();

    FFTFailState getFailState(); // VISUAL

    InteractiveFFTState getInteractiveState(); // VISUAL

    int getMaxPrecons();

    int getGameWinner();
}
