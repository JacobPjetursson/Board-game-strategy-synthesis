package mens_morris;

import fftlib.FFTManager;
import fftlib.game.FFTSolver;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class Main {

    public static void main(String[] args) {
        State s = new State();
        GameSpecifics specs = new GameSpecifics();
        FFTManager.initialize(specs);
        FFTSolver.solveGame(s);

        FFTManager.autogenFFT();
    }
}
