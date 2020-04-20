package tictactoe.demos;

import fftlib.Action;
import fftlib.FFTManager;
import fftlib.Literal;
import fftlib.Rule;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.State;

import java.util.HashSet;

import static tictactoe.FFT.Atoms.P1_1_1;
import static tictactoe.FFT.Atoms.P2_2_2;

public class ApplyDemo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        State s0 = new State();
        State s1 = new State();
        s1.setBoardEntry(0, 0, 1);

        State s2 = new State(s1);
        s2.setBoardEntry(1, 1, 2);

        State s3 = new State(s2);
        s3.setBoardEntry(2, 2, 1);

        State s4 = new State(s3);
        s4.setBoardEntry(0, 2, 2);

        State ss = new State();
        ss.setBoardEntry(2, 0, 1);

        HashSet<Literal> precons = new HashSet<>();
        precons.add(new Literal("B(0, 0)"));
        precons.add(new Literal("P1(0, 1)"));
        precons.add(new Literal("B(0, 2)"));
        precons.add(new Literal("P2(1, 0)"));
        precons.add(new Literal("B(1, 1)"));
        precons.add(new Literal("P1(2, 0)"));
        precons.add(new Literal("P2(2, 1)"));
        precons.add(new Literal("B(2, 2)"));
        Action action = new Action("P1(0, 2)");
        Rule testRule = new Rule(precons, action);

        State testState = new State();
        testState.setBoardEntry(0, 1, 1);
        testState.setBoardEntry(1, 1, 2);
        testState.setBoardEntry(1, 2, 2);
        testState.setBoardEntry(2, 0, 1);
        testState.setBoardEntry(2, 1, 2);
        testState.setBoardEntry(2, 2, 1);

        System.out.println(testRule);
        System.out.println(testState);
        System.out.println("Symmetry rules:");
        for (Rule r : FFTManager.getSymmetryRules.apply(testRule)) {
            System.out.println(r);
        }
        System.out.println();
        System.out.println(testRule.apply(testState));


    }
}
