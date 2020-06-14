package tictactoe.demos;

import fftlib.FFTManager;
import fftlib.game.FFTNode;
import fftlib.logic.*;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ApplyDemo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);


        Node test1 = new Node();
        Node test2 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, 2);
        Node test3 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 2, 1}}, 1);
        Node test4 = new Node(new int[][] {{1, 0, 0}, {0, 0, 0}, {0, 2, 1}}, 2);
        Node test5 = new Node(new int[][] {{1, 0, 2}, {0, 0, 0}, {0, 2, 1}}, 1);

        Rule r1 = new Rule(test1.convert().getAll(), new Action("P1(0, 0)"));
        Rule r2 = new Rule(test2.convert().getAll(), new Action("P1(0, 1)"));
        Rule r3 = new Rule(test3.convert().getAll(), new Action("P1(0, 2)"));
        Rule r4 = new Rule(test4.convert().getAll(), new Action("P1(1, 0)"));
        Rule r5 = new Rule(test5.convert().getAll(), new Action("P1(1, 1)"));

        FFT fft = new FFT("test");
        fft.addRuleGroup(new RuleGroup("test"));

        Node testNode = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 2, 1}}, 1);
        long pos_code = testNode.convert().getAll().getBitString();
        long neg_code = testNode.convert().getAll().getNegativeBitString();
        System.out.println("pos code: " + pos_code);
        System.out.println("neg code: " + neg_code);



    }
}
