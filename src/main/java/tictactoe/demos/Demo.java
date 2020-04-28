package tictactoe.demos;

import fftlib.*;
import fftlib.game.FFTSolver;
import fftlib.game.LiteralSet;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.State;

import java.util.ArrayList;
import java.util.HashSet;

import static misc.Globals.PLAYER1;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        FFTSolver.solveGame(new State());

        LiteralSet literals = new LiteralSet();
        Literal l1 = new Literal("P1(0, 0)");
        Literal l2 = new Literal("P2(1, 1)");
        literals.add(new Literal(l1));
        literals.add(new Literal(l2));
        literals.add(new Literal(l1));


        // Make strategy with meta rules
        //ArrayList<FFT> ffts = FFTManager.load("FFTs/tictactoe_meta_fft.txt");
        //FFT fft = FFTManager.autogenFFT(ffts.get(0));
    }
}
