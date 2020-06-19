package misc.demos;

import fftlib.FFTManager;
import fftlib.FFTSolution;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.FFTSolver;
import fftlib.logic.Rule;
import misc.Config;
import sim.GameSpecifics;
import sim.Line;
import sim.Move;
import sim.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static misc.Config.AUTOGEN_TEAM;
import static misc.Config.MINIMIZE_PRECONDITIONS;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);

        // build set of testNodes (SIM)
        ArrayList<FFTNode> testNodes = new ArrayList<>();
        Node test1 = new Node();
        testNodes.add(test1);
        Node test2 = test1.getNextNode(new Move(1, new Line(0, 1)));
        testNodes.add(test2);
        Node test3 = test2.getNextNode(new Move(2, new Line(0, 2)));
        testNodes.add(test3);
        Node test4 = test3.getNextNode(new Move(1, new Line(0, 3)));
        testNodes.add(test4);
        Node test5 = test4.getNextNode(new Move(2, new Line(3, 4)));
        testNodes.add(test5);
        Node test6 = test5.getNextNode(new Move(1, new Line(4, 5)));
        testNodes.add(test6);
        Node test7 = test6.getNextNode(new Move(2, new Line(0, 4)));
        testNodes.add(test7);
        Node test8 = test7.getNextNode(new Move(1, new Line(2, 3)));
        testNodes.add(test8);

        System.out.println("Solving game");
        FFTSolver.solveGame();
        //FFTManager.autogenFFT();
        System.out.println("fft size: " + FFTManager.currFFT.size());
        System.out.println("ruleList size: " + FFTManager.currFFT.getRuleList().size());

        long timeStart;
        double timeSpent;
        HashMap<FFTNode, FFTMove> classicStates = new HashMap<>();
        HashMap<FFTNode, FFTMove> newStates = new HashMap<>();

        // BENCHMARKS
        int iterations = 1;
        // benchmark of classic apply method
        Config.USE_APPLY_OPT = false;
        timeStart = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            for (FFTNode n : testNodes) {
                //for (FFTNode n : FFTSolution.getSolution().keySet()) {
                HashSet<FFTMove> moves = FFTManager.currFFT.apply(n);
                if (!moves.isEmpty()) {
                    classicStates.put(n, moves.iterator().next());
                }
            }
        }
        timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on classic apply function: " + timeSpent + " seconds." +
                " AppliedNo: " + classicStates.size());

        // benchmark of new apply method
        Config.USE_APPLY_OPT = true;
        timeStart = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            //for (FFTNode n : FFTSolution.getSolution().keySet()) {
            for (FFTNode n : testNodes) {
                HashSet<FFTMove> moves = FFTManager.currFFT.apply(n);
                if (!moves.isEmpty()) {
                    newStates.put(n, moves.iterator().next());
                }
            }
        }
        timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on new apply function: " + timeSpent + " seconds." +
                " AppliedNo: " + newStates.size());

        // Control check
        System.out.println("Checking correctness: ");
        if (newStates.size() != classicStates.size()) {
            System.out.println("Size discrepancy!");
        }
        for (Map.Entry<FFTNode, FFTMove> entry : newStates.entrySet()) {
            FFTNode newNode = entry.getKey();
            FFTMove newMove = entry.getValue();

            FFTMove classicMove = classicStates.get(newNode);
            if (classicMove == null || !classicMove.equals(newMove)) {
                System.out.println("discrepancy!");
                System.out.println("newnode: " + newNode);
                System.out.println("classic Move: " + classicMove);
                System.out.println("new move: " + newMove);
                Rule r = FFTManager.currFFT.getRuleList().findRule(newNode.convert().getAll());
                System.out.println("found rule: " + r);
                break;
            }
        }

        // MINIMIZE WITH NEW OPTIMIZATION
        Config.USE_APPLY_OPT = true;
        System.out.println("Minimizing");
        FFTManager.currFFT.minimize(AUTOGEN_TEAM, MINIMIZE_PRECONDITIONS);
    }
}
