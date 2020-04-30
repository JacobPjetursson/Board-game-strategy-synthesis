package tictactoe.demos;

import fftlib.FFTManager;
import fftlib.game.FFTSolver;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        FFTSolver.solveGame(new Node());




        // Make strategy with meta rules
        //ArrayList<FFT> ffts = FFTManager.load("FFTs/tictactoe_meta_fft.txt");
        //FFT fft = FFTManager.autogenFFT(ffts.get(0));
    }
}
