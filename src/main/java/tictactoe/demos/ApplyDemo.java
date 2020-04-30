package tictactoe.demos;

import fftlib.FFTManager;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

public class ApplyDemo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);

        Node test1 = new Node();
        Node test2 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, 1);
        Node test3 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 2}}, 1);
        /*
        System.out.println(testRule.getNumberOfCoveredStates());
        System.out.println(test1.getState().getBitString());
        System.out.println(test2.getState().getBitString());
        System.out.println(test3.getState().getBitString());
        System.out.println(testRule.getCoveredStateBitCodes());

         */



    }
}
