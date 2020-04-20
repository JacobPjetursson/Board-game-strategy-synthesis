package sim;

import fftlib.*;
import fftlib.game.FFTSolver;
import fftlib.game.Transform;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.ArrayList;
import java.util.HashSet;


import static misc.Globals.*;

public class Main {

    public static void main(String[] args) {
        CURRENT_GAME = SIM;
        State s = new State();
        GameSpecifics specs = new GameSpecifics();
        FFTManager.initialize(specs);
        FFTSolver.solveGame(s);

        FFTManager.autogenFFT();

    }
}
