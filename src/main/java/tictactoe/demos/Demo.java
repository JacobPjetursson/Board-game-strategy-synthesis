package tictactoe.demos;

import fftlib.*;
import fftlib.game.FFTSolver;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.State;

import java.util.ArrayList;

import static misc.Globals.PLAYER1;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics(null);
        new FFTManager(gs);
        FFTSolver.solveGame(new State());

        // Make strategy with meta rules
        ArrayList<FFT> ffts = FFTManager.load("FFTs/tictactoe_meta_fft.txt");
        FFT fft = FFTManager.autogenFFT(ffts.get(0));
    }
}
