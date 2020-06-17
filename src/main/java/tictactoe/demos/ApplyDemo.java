package tictactoe.demos;

import fftlib.FFTManager;
import fftlib.FFTSolution;
import fftlib.game.*;
import fftlib.logic.*;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.Node;

import java.util.*;

public class ApplyDemo {

    private static ArrayList<Integer> sortedAtoms;

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);

/*
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
 */
        Rule r1 = new Rule("P1(1, 2) AND !P2(2, 0) AND !P2(0, 1)", "+P1(1, 1)");
        Rule r2 = new Rule("!P1(1, 2) AND P2(2, 0) AND P2(0, 1)", "+P1(1, 1)");
        Rule r3 = new Rule("!P2(0, 1)", "+P1(1, 1)");
        Rule r4 = new Rule("P2(0, 1)", "+P1(1, 1)");
        Rule r5 = new Rule("!P2(2, 0) AND !P2(0, 1)", "+P1(1, 1)");


        FFT fft = new FFT("test");
        fft.addRuleGroup(new RuleGroup("test"));
        sortedAtoms = FFTManager.getGameAtoms.get();
        sortedAtoms.sort(Collections.reverseOrder());

        for (int atm : sortedAtoms) {
            System.out.println(atm);
            Literal l = new Literal(atm);
            System.out.println(l.getName());
            System.out.println();
        }

        List<Rule> rules = new ArrayList<>();

        //TreeMap<LiteralSet, Rule> rules = new TreeMap<>(new LiteralSetComparator());
        rules.add(r1);
        rules.add(r2);
        rules.add(r3);
        rules.add(r4);
        rules.add(r5);
        for (int i = 0; i < rules.size(); i++) {
            Rule r = rules.get(i);
            r.setRuleIndex(i);
        }
        rules.sort(new LiteralSetComparator());

        Node test2 = new Node(new int[][] {{0, 0, 0}, {0, 0, 1}, {0, 0, 0}}, 1); // x1, x2, !x3
        Rule appliedRule = findRule(test2.convert().getAll(), rules);
        System.out.println("applied rule:");
        System.out.println(appliedRule);



        FFTSolver.solveGame();
        FFTManager.autogenFFT();
        System.out.println("fft size: " + FFTManager.currFFT.size());
        List<Rule> fftRules = FFTManager.currFFT.getRules();
        // set index of all rules (todo - supposed to be done when initially adding the rule)
        for (int i = 0; i < fftRules.size(); i++) {
            Rule r = fftRules.get(i);
            r.setRuleIndex(i);
        }
        // sort rules based on custom sorter
        fftRules.sort(new LiteralSetComparator());
        HashMap<? extends FFTNode, NodeMapping> solution = FFTSolution.getSolution();
        long timeStart;
        double timeSpent;
        ArrayList<FFTNode> classicStates = new ArrayList<>();
        // BENCHMARKS
        int iterations = 100;
        // benchmark of classic apply method
        timeStart = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            for (FFTNode n : solution.keySet()) {
                HashSet<FFTMove> moves = FFTManager.currFFT.apply(n);
                if (!moves.isEmpty()) {
                    classicStates.add(n);
                }
            }
        }
        timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on classic apply function: " + timeSpent + " seconds." +
                " AppliedNo: " + classicStates.size());

        // benchmark of new apply method
        timeStart = System.currentTimeMillis();
        ArrayList<FFTNode> newStates = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            for (FFTNode n : solution.keySet()) {
                if (findRule(n.convert().getAll(), fftRules) != null)
                    newStates.add(n);
            }
        }
        timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on new apply function: " + timeSpent + " seconds." +
                " AppliedNo: " + newStates.size());

        for (int i = 0; i < newStates.size(); i++) {
            FFTNode newNode = newStates.get(i);
            if (newNode != classicStates.get(i)) {
                System.out.println("discrepancy!");
                System.out.println("newnode: " + newNode);
                DEBUG = true;
                Rule r = findRule(newNode.convert().getAll(), fftRules);
                System.out.println("found rule: " + r);
                DEBUG = false;
                break;
            }
        }
    }

    private static boolean DEBUG = false;
    private static Rule findRule(LiteralSet lSet, List<Rule> rules) {
        // (todo) Make this a treeset sorted by ruleIndex for constant lookup for correct rule
        List<Rule> appliedRules = filterByAtom(0, lSet, rules);
        if (appliedRules.isEmpty())
            return null;
        int minIdx = rules.size();
        if (DEBUG)
        System.out.println("applied rules:");
        for (Rule r : appliedRules) {
            if (DEBUG)
                System.out.println(r + " , index: " + r.getRuleIndex());
            if (r.getRuleIndex() < minIdx)
                minIdx = r.getRuleIndex();
        }
        return rules.get(minIdx);
    }

    private static List<Rule> filterByAtom(int atomIdx, LiteralSet lSet, List<Rule> rules) {
        if (atomIdx >= sortedAtoms.size()) {
            if (DEBUG) System.out.println("returning rules: " + rules);
            return rules;
        }

        ArrayList<Rule> appliedRules = new ArrayList<>();
        int atom = sortedAtoms.get(atomIdx++);
        if (DEBUG) {
            System.out.println("atom: " + atom);
            System.out.println("LiteralSet: " + lSet);
        }
        Literal pos = new Literal(atom);
        Literal neg = new Literal(atom, true);
        if (DEBUG)
            System.out.println("pos: " + pos);
        int posEnd = rules.size();
        int negStart = rules.size();
        // find pos & neg intervals (todo - make binary search)
        boolean posFound = false;
        for (int i = 0; i < rules.size(); i++) {
            LiteralSet precons = rules.get(i).getAllPreconditions();
            if (!precons.contains(pos) && !posFound) {
                posEnd = i;
                posFound = true;
            }
            if (precons.contains(neg)) {
                negStart = i;
                break;
            }
        }
        List<Rule> middleList = null, sideList = null;
        if (posEnd != rules.size() && negStart != 0) // middle list exists
            middleList = rules.subList(posEnd, negStart);
        // trim one side
        if (posEnd != 0 && lSet.contains(pos))
            sideList = rules.subList(0, posEnd);
        else if (negStart != rules.size() && lSet.contains(neg))
            sideList = rules.subList(negStart, rules.size());

        if (DEBUG) {
            if (middleList != null) {
                System.out.println("middle list of length:" + middleList.size());
                for (Rule r : middleList)
                    System.out.println(r);
            }
            if (sideList != null) {
                System.out.println("side list of size: " + sideList.size());
                for (Rule r : sideList)
                    System.out.println(r);
            }
        }

        if (middleList != null)
            appliedRules.addAll(filterByAtom(atomIdx, lSet, middleList));
        if (sideList != null)
            appliedRules.addAll(filterByAtom(atomIdx, lSet, sideList));
        return appliedRules;
    }

    public static class LiteralSetComparator implements Comparator<Rule> {

        @Override
        public int compare(Rule o1, Rule o2) {
            LiteralSet set1 = o1.getAllPreconditions();
            LiteralSet set2 = o2.getAllPreconditions();
            for (int atm : sortedAtoms) {
                Literal l = new Literal(atm);
                if (set1.contains(l) && !set2.contains(l))
                    return -1;
                if (set2.contains(l) && !set1.contains(l))
                    return 1;
                l.setNegated(true);
                if (set1.contains(l) && !set2.contains(l))
                    return 1;
                if (set2.contains(l) && !set1.contains(l))
                    return -1;
            }
            return 0;
        }
    }
}
