package misc.demos;

import fftlib.FFTAutoGen;
import fftlib.FFTManager;
import fftlib.FFTSolution;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.FFTSolver;
import fftlib.logic.Rule;
import misc.Config;
import sim.GameSpecifics;
import sim.Line;
import sim.Move;
import sim.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static misc.Config.AUTOGEN_TEAM;
import static misc.Config.MINIMIZE_PRECONDITIONS;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);

        System.out.println("Solving game");
        FFTSolver.solveGame();
        //FFTManager.autogenFFT();
        System.out.println("fft size: " + FFTManager.currFFT.size());

        // MINIMIZE TEST
        System.out.println("Minimizing");
        //FFTManager.currFFT.minimize(AUTOGEN_TEAM, MINIMIZE_PRECONDITIONS);
    }
}
