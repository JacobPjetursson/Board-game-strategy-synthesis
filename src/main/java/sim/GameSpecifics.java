package sim;

import fftlib.Action;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;

import java.util.HashSet;

import static misc.Globals.PLAYER2;

public class GameSpecifics implements FFTGameSpecifics {
    @Override
    public FFTMove actionToMove(Action a, int team) {
        return null;
    }

    @Override
    public FFTState preconsToState(HashSet<Literal> precons, int team) {
        return null;
    }

    @Override
    public Rule gdlToRule(String precons, String action) {
        return null;
    }

    @Override
    public String getFFTFilePath() {
        if (Config.ENABLE_GGP_PARSER)
            return "FFTs/sim_GGP_FFT.txt";
        return "FFTs/simFFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        return new int[] {14, 1};
    }

    @Override
    public String[] getPlayerNames() {
        return new String[0];
    }

    @Override
    public int[] getAllowedTransformations() {
        return new int[0];
    }

    @Override
    public FFTState getInitialState() {
        return new State();
    }

    @Override
    public FFTLogic getLogic() {
        return new Logic();
    }

    @Override
    public FFTDatabase getDatabase() {
        return new Database();
    }

    @Override
    public FFTFailState getFailState() {
        return null;
    }

    @Override
    public InteractiveFFTState getInteractiveState() {
        return null;
    }

    @Override
    public int getMaxPrecons() {
        int[] dim = getBoardDim();
        return dim[0] * dim[1];
    }

    @Override
    public int getGameWinner() {
        return PLAYER2;
    }
}
