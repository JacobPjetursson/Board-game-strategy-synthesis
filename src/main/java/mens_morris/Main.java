package mens_morris;

import fftlib.Clause;
import fftlib.FFTManager;
import fftlib.Literal;
import fftlib.game.FFTSolver;
import fftlib.game.Transform;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.ArrayList;
import java.util.HashSet;

import static fftlib.FFTManager.gameBoardHeight;
import static misc.Globals.CURRENT_GAME;
import static misc.Globals.SIM;

public class Main {

    public static void main(String[] args) {
        State s = new State();
        GameSpecifics specs = new GameSpecifics();
        FFTManager fftManager = new FFTManager(specs);
        FFTSolver.solveGame(s);

        try {
            fftManager.autogenFFT();
        } catch (TransitionDefinitionException | MoveDefinitionException | GoalDefinitionException e) {
            e.printStackTrace();
        }
    }
}
