package tictactoe.demos;

import fftlib.Action;
import fftlib.FFTManager;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.LiteralSet;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.State;

import java.util.HashSet;

public class ApplyDemo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        State s0 = new State();
        State s1 = new State();
        s1.setBoardEntry(0, 0, 1);
        System.out.println(s1.getLiterals().getBitString());

        State s2 = new State(s1);
        s2.setBoardEntry(1, 1, 2);
        System.out.println(s2.getLiterals().getBitString());

        State s3 = new State(s2);
        s3.setBoardEntry(2, 2, 1);
        System.out.println(s3.getLiterals().getBitString());

        State s4 = new State(s3);
        s4.setBoardEntry(0, 2, 2);
        System.out.println(s4.getLiterals().getBitString());

        State ss = new State();
        ss.setBoardEntry(2, 0, 1);
        System.out.println(ss.getLiterals().getBitString());

        System.out.println();
        State test = new State();
        State test1 = new State(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}}, 1);
        State test2 = new State(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, 1);
        State test3 = new State(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 2}}, 1);
        LiteralSet precons = test.getAllLiterals();
        Action action = new Action("P1(0, 0)");
        Rule testRule = new Rule(precons, action);
        testRule.removePrecondition(new Literal("!P1(2, 2)"));
        testRule.removePrecondition(new Literal("!P2(2, 2)"));
        System.out.println("testRule: " + testRule);
        System.out.println(testRule.getNumberOfCoveredStates());
        System.out.println(test.getLiterals().getBitString());
        System.out.println(test1.getLiterals().getBitString());
        System.out.println(test2.getLiterals().getBitString());
        System.out.println(test3.getLiterals().getBitString());
        System.out.println(testRule.getCoveredStateBitCodes());



    }
}
