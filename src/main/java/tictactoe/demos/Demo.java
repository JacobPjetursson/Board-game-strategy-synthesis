package tictactoe.demos;

import fftlib.FFTManager;
import fftlib.game.FFTSolver;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

import java.util.ArrayList;
import java.util.HashMap;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        FFTSolver.solveGame(new Node());
        TestClass testClass = new TestClass();

        HashMap<Integer, Integer> parTest = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            parTest.put(i, i);
        }

        parTest.values().parallelStream().forEach(i -> {
            if (testClass.printNumbers(i))
                System.out.println("thread: " + i + " found number 8");
        });


        // Make strategy with meta rules
        //ArrayList<FFT> ffts = FFTManager.load("FFTs/tictactoe_meta_fft.txt");
        //FFT fft = FFTManager.autogenFFT(ffts.get(0));
    }
}

