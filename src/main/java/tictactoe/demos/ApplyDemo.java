package tictactoe.demos;

import fftlib.FFTManager;
import fftlib.logic.Action;
import fftlib.logic.PredRule;
import fftlib.logic.Rule;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

public class ApplyDemo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);

        Node test1 = new Node();
        Node test2 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, 1);
        Node test3 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 2}}, 1);
        Rule r = new Rule(test2.convert().getAll(), new Action("P1(0, 1)"));
        System.out.println(r);
        PredRule predRule = r.liftAll(0);
        System.out.println(r.getSortedProps());



    }
}
