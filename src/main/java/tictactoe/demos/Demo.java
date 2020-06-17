package tictactoe.demos;

import fftlib.FFTManager;
import fftlib.game.FFTNode;
import fftlib.game.FFTSolver;
import fftlib.game.LiteralSet;
import fftlib.logic.*;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Move;
import tictactoe.game.Node;

import java.util.*;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        FFTSolver.solveGame();

/*
        TreeMap<Long, Node> codes = new TreeMap<>();
        Node n = new Node();
        codes.put(n.convert().getBitString(), n);
        n = n.getNextNode(new Move(1, 1, 1));
        codes.put(n.convert().getBitString(), n);
        n = n.getNextNode(new Move(0, 0, 2));
        codes.put(n.convert().getBitString(), n);
        n = n.getNextNode(new Move(2, 2, 1));
        codes.put(n.convert().getBitString(), n);

        n = new Node();
        n = n.getNextNode(new Move(2, 2, 1));
        codes.put(n.convert().getBitString(), n);

        for (Map.Entry<Long, Node> entry : codes.entrySet()) {
            System.out.println("Node: " + entry.getValue() + " , with code: " + entry.getKey());
        }

        FFTNode highest_code_node = codes.pollLastEntry().getValue();
        System.out.println("highest code node: " + highest_code_node.convert().getBitString());
        Rule highest_code_rule = new Rule(highest_code_node.convert(), new Action("P2(2, 0)"));
        System.out.println("highest code rule: " + highest_code_rule.getAllPreconditions().getBitString());

        TreeMap<Integer, Literal> literalSet = new TreeMap<>();
        for (Literal l : highest_code_rule.getPreconditions()) {
            literalSet.put(l.id, l);
        }
        System.out.println("Literals in highest code rule:");
        for (Map.Entry<Integer, Literal> entry : literalSet.entrySet()) {
            System.out.println("Literal: " + entry.getValue() + " , with key: " + (1 << entry.getKey()));
        }

 */



        // Make strategy with meta rules
        //ArrayList<FFT> ffts = FFTManager.load("FFTs/tictactoe_meta_fft.txt");
        //FFT fft = FFTManager.autogenFFT(ffts.get(0));
    }
}

