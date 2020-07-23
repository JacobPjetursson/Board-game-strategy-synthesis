package tictactoe.demos;

import fftlib.FFTManager;
import fftlib.game.FFTSolver;
import fftlib.logic.*;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.Action;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.RuleGroup;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Move;
import tictactoe.game.Node;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        FFTSolver.solveGame();
        // make testrules
        LiteralSet lSet = new LiteralSet();
        PropRule test1 = new PropRule(lSet, new Action("P1(1, 1)"));

        lSet = new LiteralSet();
        lSet.add(new PropLiteral("P1(0, 0)"));
        PropRule dead1 = new PropRule(lSet, new Action("P1(1, 1)"));

        lSet = new LiteralSet();
        lSet.add(new PropLiteral("P1(0, 0)"));
        lSet.add(new PropLiteral("P2(2, 2)"));
        lSet.add(new PropLiteral("!P1(2, 2)"));
        lSet.add(new PropLiteral("!P2(0, 0)"));
        lSet.add(new PropLiteral("!P1(1, 1)"));
        lSet.add(new PropLiteral("P2(0, 2)"));
        lSet.add(new PropLiteral("P1(0, 1)"));
        lSet.add(new PropLiteral("P2(0, 2)"));
        lSet.add(new PropLiteral("!P1(0, 2)"));
        lSet.add(new PropLiteral("!P2(0, 1)"));
        lSet.add(new PropLiteral("!P1(1, 2)"));
        lSet.add(new PropLiteral("P2(1, 2)"));
        PropRule dead2 = new PropRule(lSet, new Action("P1(1, 1)"));
        LiteralSet testSet = new LiteralSet(dead2.getPreconditions());
        System.out.println(testSet);
        LiteralSet testSet1 = new LiteralSet(dead2.getPreconditions());
        System.out.println(testSet1);
        System.out.println(dead2.getPreconditions());


        lSet = new LiteralSet();
        PropRule notdead = new PropRule(lSet, new Action("P1(1, 2)"));

        FFT fft = new FFT("Synthesis");
        fft.append(test1);
        fft.append(dead1);
        fft.append(dead2);
        fft.append(notdead);

        System.out.println(fft);
        fft.removeDeadRules(test1);
        System.out.println(fft);

        // make testNodes
        Node testNode1 = new Node();
        Node testNode2 = testNode1.getNextNode(new Move(0, 0, 1));
        System.out.println("apply testnode1: " + testNode1);
        System.out.println("testNode1 applies with move: " + fft.apply(testNode1));
        System.out.println("apply testnode2: " + testNode2);
        System.out.println("testNode2 applies with move: " + fft.apply(testNode2));


        /*

        FFTManager.autogenFFT();
        FFT fft = FFTManager.currFFT;
        HashMap<FFTNode, FFTNode> newReach = new HashMap<>(FFTAutoGen.getReachableStates());
        System.out.println("New fft applied size: " + newReach.size());

        System.out.println("Comparing applied states");
        for (FFTNode n : loadedReach.keySet()) {
            if (!newReach.containsKey(n)) {
                System.out.println("Node discrepancy!");
                System.out.println("Node: " + n + " , not in new applied states");
            }
        }

         */


/*
 */
/*
        System.out.println("rules size: " + fft.ruleGroups.get(0).rules.size());
        System.out.println("ruleList size: " + fft.getRuleList().size());
        Rule r = fft.ruleGroups.get(0).rules.get(5);
        System.out.println("Removing rule: " + r);
        fft.remove(r);
        System.out.println("rules size: " + fft.ruleGroups.get(0).rules.size());
        System.out.println("ruleList size: " + fft.getRuleList().size());
        System.out.println("Adding back rule");
        fft.ruleGroups.get(0).rules.add(5, r);
        fft.getRuleList().sortedAdd(r);
        for (Rule ru : fft.ruleGroups.get(0).rules) {
            if (ru.getRuleIndex() == 5)
                System.out.println("CMON MAN");
        }
        for (Rule ru : fft.getRuleList())
            if (ru.getRuleIndex() == 5)
                System.out.println("ARGHHH");
                */
        /*
        for (int atom : FFTManager.sortedGameAtoms)
            System.out.println(FFTManager.getAtomName.apply(atom));
        Node test1 = new Node();
        Node test2 = new Node(new int[][] {{0, 2, 0}, {0, 0, 0}, {0, 0, 0}}, 1);
        Node test3 = new Node(new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 2, 1}}, 1);
        Node test4 = new Node(new int[][] {{1, 0, 0}, {0, 0, 0}, {0, 2, 1}}, 2);
        Node test5 = new Node(new int[][] {{1, 0, 2}, {0, 0, 0}, {0, 2, 1}}, 1);
        System.out.println(test1.convert().getBitString());
        System.out.println(test1.convert());
        System.out.println(test2.convert().getBitString());
        System.out.println(test2.convert());
        System.out.println(test3.convert().getBitString());
        System.out.println(test3.convert());
        System.out.println(test4.convert().getBitString());
        System.out.println(test4.convert());
        System.out.println(test5.convert().getBitString());
        System.out.println(test5.convert());

         */
        /*

        Rule r1 = new Rule(test1.convert(), new Action("P1(0, 0)"));
        Rule r2 = new Rule(test2.convert(), new Action("P1(0, 1)"));
        Rule r3 = new Rule(test3.convert(), new Action("P1(0, 2)"));
        Rule r4 = new Rule(test4.convert(), new Action("P1(1, 0)"));
        Rule r5 = new Rule(test5.convert(), new Action("P1(1, 1)"));
        Rule r6 = new Rule("P1(1, 2) AND !P2(2, 0) AND !P2(0, 1)", "+P1(1, 1)");
        Rule r7 = new Rule("!P1(1, 2) AND P2(2, 0) AND P2(0, 1)", "+P1(1, 1)");
        Rule r8 = new Rule("!P2(0, 1)", "+P1(1, 1)");
        Rule r9 = new Rule("P2(0, 1)", "+P1(1, 1)");
        Rule r10 = new Rule("!P2(2, 0) AND !P2(0, 1)", "+P1(1, 1)");
        RuleList ruleList = new RuleList();
        ruleList.sortedAdd(r1);
        ruleList.sortedAdd(r2);
        ruleList.sortedAdd(r3);
        ruleList.sortedAdd(r4);
        ruleList.sortedAdd(r5);
        ruleList.sortedAdd(r6);
        ruleList.sortedAdd(r7);
        ruleList.sortedAdd(r8);
        ruleList.sortedAdd(r9);
        ruleList.sortedAdd(r10);

        ArrayList<Rule> arrayList = new ArrayList<>();
        arrayList.add(r1);
        arrayList.add(r2);
        arrayList.add(r3);
        arrayList.add(r4);
        arrayList.add(r5);
        arrayList.add(r6);
        arrayList.add(r7);
        arrayList.add(r8);
        arrayList.add(r9);
        arrayList.add(r10);
        //arrayList.sort(new RuleList.RuleComparator());

        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(arrayList.get(i));
            System.out.println(ruleList.get(i));
            System.out.println();
        }

        ruleList.sortedRemove(r2);
        ruleList.sortedRemove(r5);
        System.out.println(ruleList.size());
        for (int i = 0; i < ruleList.size(); i++) {
            System.out.println(arrayList.get(i));
            System.out.println(ruleList.get(i));
            System.out.println();
        }

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

