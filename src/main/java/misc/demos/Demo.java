package misc.demos;

import fftlib.FFTManager;
import fftlib.auxiliary.InvertedList;
import fftlib.game.FFTNode;
import fftlib.game.FFTSolver;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.logic.LiteralSet;
import fftlib.logic.Rule;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static misc.Globals.PLAYER2;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);

        System.out.println("Solving game");
        FFTSolver.solveGame();
        //FFTManager.autogenFFT();
        //System.out.println("fft size: " + FFTManager.currFFT.size());

        // MINIMIZE TEST
        //System.out.println("Minimizing");
        //FFTManager.currFFT.minimize(AUTOGEN_TEAM, MINIMIZE_PRECONDITIONS);

        // INVERTED LIST
        InvertedList invertedList = new InvertedList(true);

        // testnodes
        ArrayList<FFTNode> testNodes = new ArrayList<>();
        Node test1 = new Node();
        Node test2 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, 2);
        Node test3 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 2, 1}}, 1);
        Node test4 = new Node(new int[][] {{0, 1, 0}, {0, 0, 0}, {0, 2, 1}}, 2);
        Node test5 = new Node(new int[][] {{1, 0, 2}, {0, 0, 0}, {0, 2, 1}}, 1); //**
        Node test6 = new Node(new int[][] {{0, 1, 2}, {1, 0, 0}, {0, 2, 1}}, 2);
        Node test7 = new Node(new int[][] {{1, 2, 2}, {1, 0, 0}, {0, 2, 1}}, 1); //**
        testNodes.add(test1);
        testNodes.add(test2);
        testNodes.add(test3);
        testNodes.add(test4);
        testNodes.add(test5);
        testNodes.add(test6);
        testNodes.add(test7);
        HashMap<FFTNode, FFTNode> testMap = new HashMap<>();
        testMap.put(test1, test1);
        testMap.put(test2, test2);
        System.out.println("testMap:");
        for (FFTNode node : testMap.keySet()) {
            System.out.println(node);
            System.out.println(node.hashCode());
            Node n = (Node) node;
            System.out.println(n.getZobristKey());
        }
        test1.setBoardEntry(2, 2, 1);
        test1.setTurn(PLAYER2);
        test1.initHashCode();
        System.out.println("testMap:");
        for (FFTNode node : testMap.keySet()) {
            System.out.println(node);
            System.out.println(node.hashCode());
            Node n = (Node) node;
            System.out.println(n.getZobristKey());
        }
/*
        // testRule
        LiteralSet lSet = new LiteralSet();
        lSet.add(new Literal("P1(2, 2)"));
        lSet.add(new Literal("P1(0, 0)"));
        Rule testRule = new Rule(lSet, new Action("P2(1, 1)"));

        // add nodes to inverted lists
        for (FFTNode node : testNodes) {
            invertedList.add(node);
        }
        // PRINT TEST NODES:
        System.out.println("Printing test nodes");
        for (FFTNode testNode : testNodes) {
            System.out.println(testNode.convert().getBitString());
            System.out.println(testNode);
        }
        System.out.println();

        // PRINT INVERTED LISTS
        System.out.println("Printing the inverted list");
        int count = 0;
        for (HashMap<BigInteger, FFTNode> map : invertedList.getNodeList()) {
            System.out.println("atom: " + FFTManager.getAtomName.apply(count++));
            for (Map.Entry<BigInteger, FFTNode> entry : map.entrySet()) {
                System.out.println("bitstring: " + entry.getKey());
                System.out.println("node: " + entry.getValue());
            }
            System.out.println();
        }

        // PRINT APPLIED NODES
        System.out.println("Printing applied nodes");
        for (FFTNode appliedNode : invertedList.findNodes(testRule)) {
            System.out.println(appliedNode.convert().getBitString());
            System.out.println(appliedNode);
        }
        System.out.println();

 */
    }


}
