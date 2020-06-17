package mens_morris;

import fftlib.FFTManager;
import fftlib.game.FFTSolver;

public class Main {

    public static void main(String[] args) {
        Node n = new Node();
        GameSpecifics specs = new GameSpecifics();
        FFTManager.initialize(specs);
        FFTSolver.solveGame();

        FFTManager.autogenFFT();
    }
}
