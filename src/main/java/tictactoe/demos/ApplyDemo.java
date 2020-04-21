package tictactoe.demos;

import fftlib.Action;
import fftlib.FFTManager;
import fftlib.Literal;
import fftlib.Rule;
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
        System.out.println(Literal.getBitString(s1.getLiterals()));

        State s2 = new State(s1);
        s2.setBoardEntry(1, 1, 2);
        System.out.println(Literal.getBitString(s2.getLiterals()));

        State s3 = new State(s2);
        s3.setBoardEntry(2, 2, 1);
        System.out.println(Literal.getBitString(s3.getLiterals()));

        State s4 = new State(s3);
        s4.setBoardEntry(0, 2, 2);
        System.out.println(Literal.getBitString(s4.getLiterals()));

        State ss = new State();
        ss.setBoardEntry(2, 0, 1);
        System.out.println(Literal.getBitString(ss.getLiterals()));

        System.out.println();
        State test1 = new State(new int[][] {{1, 2, 1}, {1, 2, 2}, {2, 1, 0}}, 1);
        State test2 = new State(new int[][] {{1, 2, 1}, {1, 2, 2}, {2, 1, 1}}, 1);
        State test3 = new State(new int[][] {{1, 2, 1}, {1, 2, 2}, {2, 1, 2}}, 1);
        HashSet<Literal> precons = new HashSet<>();
        precons.add(new Literal("P1(0, 0)"));
        precons.add(new Literal("P2(0, 1)"));
        precons.add(new Literal("P1(0, 2)"));
        precons.add(new Literal("P1(1, 0)"));
        precons.add(new Literal("P2(1, 1)"));
        precons.add(new Literal("P2(1, 2)"));
        precons.add(new Literal("P2(2, 0)"));
        precons.add(new Literal("P1(2, 1)"));
        precons.add(new Literal("!P1(2, 2)"));
        Action action = new Action("P1(2, 2)");
        Rule test = new Rule(precons, action);
        System.out.println(test.getNumberOfCoveredStates());
        System.out.println(Literal.getBitString(test1.getLiterals()));
        System.out.println(Literal.getBitString(test2.getLiterals()));
        System.out.println(Literal.getBitString(test3.getLiterals()));
        System.out.println(test.getCoveredStateBitCodes());



    }
}
