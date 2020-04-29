package tictactoe.demos;

import fftlib.Action;
import fftlib.FFTManager;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.FFTSolver;
import fftlib.game.LiteralSet;
import fftlib.game.State;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        FFTSolver.solveGame(new Node());

        State initialState = new State();
        LiteralSet literals = new LiteralSet();
        Literal l1 = new Literal("!P1(0, 0)");
        Literal l2 = new Literal("!P2(0, 0)");
        Literal l3 = new Literal("P1(1, 0)");
        literals.add(l1);
        literals.add(l2);
        literals.add(l3);
        State state = initialState.getNextState(new Action("P1(1, 1)"));
        Rule testRule = new Rule(state.getAllLiterals(), new Action("P2(2, 2)"));

        //Rule testRule = new Rule(literals, new Action("P1(2, 2)"));
        System.out.println(testRule);
        //System.out.println(testRule.getSymmetryRules());
        //System.out.println(initialState);
        //System.out.println(testRule.apply(initialState));



        // Make strategy with meta rules
        //ArrayList<FFT> ffts = FFTManager.load("FFTs/tictactoe_meta_fft.txt");
        //FFT fft = FFTManager.autogenFFT(ffts.get(0));
    }
}
