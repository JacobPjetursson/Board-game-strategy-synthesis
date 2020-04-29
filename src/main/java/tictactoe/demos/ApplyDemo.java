package tictactoe.demos;

import fftlib.Action;
import fftlib.FFTManager;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.LiteralSet;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

public class ApplyDemo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);

        Node test1 = new Node();
        Node test2 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, 1);
        Node test3 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 2}}, 1);
        LiteralSet precons = test1.getState().getAllLiterals();
        Action action = new Action("P1(0, 0)");
        Rule testRule = new Rule(precons, action);
        //testRule.removePrecondition(new Literal("!P1(2, 2)"));
        //testRule.removePrecondition(new Literal("!P2(2, 2)"));
        System.out.println("testRule: " + testRule);
        System.out.println("symmetric rules: " + testRule.getSymmetryRules());
        /*
        System.out.println(testRule.getNumberOfCoveredStates());
        System.out.println(test1.getState().getBitString());
        System.out.println(test2.getState().getBitString());
        System.out.println(test3.getState().getBitString());
        System.out.println(testRule.getCoveredStateBitCodes());

         */



    }
}
