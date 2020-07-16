package fftlib.auxiliary;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.logic.Literal;
import fftlib.logic.LiteralSet;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.PropRule;
import misc.Config;

import java.util.*;

import static misc.Config.SINGLE_THREAD;

public class InvertedList {
    private ArrayList<Set<FFTNode>> nodeList = new ArrayList<>();
    private ArrayList<Set<PropRule>> ruleList = new ArrayList<>();

    public InvertedList(boolean storeNodes) {
        // add extra map to account for atom 1-indexing
        if (storeNodes)
            nodeList.add(new HashSet<>());
        else
            ruleList.add(new HashSet<>());

        for (int ignored : FFTManager.getGameAtoms.get()) {
            if (storeNodes)
                nodeList.add(new HashSet<>());
            else
                ruleList.add(new HashSet<>());
        }
    }

    public void add(FFTNode node) {
        LiteralSet lSet = node.convert();
        for (Literal l : lSet)
            nodeList.get(l.id).add(node);
    }

    // need to add both positive and negated atom if it is not present in the preconditions
    public void add(PropRule rule) {
        for (int atom : FFTManager.getGameAtoms.get()) {
            LiteralSet precons = rule.getAllPreconditions();
            Literal pos = new Literal(atom);
            Literal neg = new Literal(atom);
            neg.setNegated(true);
            // add if contains pos or does not contain neg
            if (precons.contains(pos) || !precons.contains(neg))
                ruleList.get(pos.id).add(rule);
            if (precons.contains(neg) || !precons.contains(pos))
                ruleList.get(neg.id).add(rule);

        }
    }

    public void remove(FFTNode node) {
        for (Set<FFTNode> set : nodeList)
            set.remove(node);
    }

    public void remove(PropRule rule) {
        for (Set<PropRule> set : ruleList) {
            set.remove(rule);
        }
    }

    public HashSet<FFTMove> apply(FFTNode node, boolean safe) {
        return findMoves(node, safe);
    }

    public HashSet<FFTMove> findMoves(FFTNode node, boolean safe) {
        LiteralSet nodeSet = node.convert();
        if (ruleList.isEmpty())
            return new HashSet<>();
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<Set<PropRule>> relevantSets = new ArrayList<>();
        Set<PropRule> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : nodeSet) {
            Set<PropRule> invertedEntry = ruleList.get(l.id);
            relevantSets.add(invertedEntry);
            if (invertedEntry.size() < smallestSize) {
                smallestSize = invertedEntry.size();
                smallest = invertedEntry;
            }
        }
        // iterate over smallest and look entry up in all relevant lists
        // break if element is not in list
        // if element is in all lists, add to new set
        PropRule firstRule = null;
        int firstIndex = Integer.MAX_VALUE;
        ArrayList<PropRule> candidates = new ArrayList<>();

        for (PropRule r : smallest) {
            if (r.getRuleIndex() <= firstIndex && ruleApplies(relevantSets, r)) {
                firstRule = r;
                firstIndex = firstRule.getRuleIndex();
                candidates.add(firstRule);
            }
        }
        HashSet<FFTMove> moves = new HashSet<>();
        if (firstRule == null) // no rule applies
            return moves;
        moves.add(firstRule.getAction().convert()); // can't be null
        if (safe)
            node.setAppliedRule(firstRule);
        if (!Config.SYMMETRY_DETECTION && !Config.USE_LIFTING) {
            return moves;
        }

        // pick up moves from other symmetric rules
        for (PropRule r : candidates)
            if (r.getRuleIndex() == firstIndex)
                moves.add(r.getAction().convert());

        return moves;
    }

    public void findNodes(PropRule rule, Map<FFTNode, Set<FFTMove>> appliedMap) {
        findNodes(rule, appliedMap, false);
    }

    // todo - ONLY WORKS WITH NON-SYMMETRY AND NO LIFTING
    // todo - we can fix it by calling findNodes for each symmetric rule,
    // todo      and concatenate the actions if multiple symmetries applies
    public void findNodes(Rule rule, Map<FFTNode, Set<FFTMove>> appliedMap, boolean safe) {
        if (nodeList.isEmpty())
            return;
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<Set<FFTNode>> relevantSets = new ArrayList<>();
        Set<FFTNode> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : rule.getAllPreconditions()) {
            Set<FFTNode> invertedEntry = nodeList.get(l.id);
            relevantSets.add(invertedEntry);
            if (invertedEntry.size() < smallestSize) {
                smallestSize = invertedEntry.size();
                smallest = invertedEntry;
            }
        }
        // iterate over smallest and look entry up in all relevant lists
        // break if element is not in list
        // if element is in all lists, add to new set
        if (!SINGLE_THREAD) {
            smallest.parallelStream().forEach(node ->
                    insert(relevantSets, node, rule, appliedMap, safe));
        } else {
            for (FFTNode node : smallest) {
                insert(relevantSets, node, rule, appliedMap, safe);
            }
        }
    }

    private static void insert(ArrayList<Set<FFTNode>> sets, FFTNode node,
                               Rule rule, Map<FFTNode, Set<FFTMove>> appliedMap, boolean safe) {
        if (nodeApplies(sets, node)) {
            HashSet<FFTMove> ruleMoves = new HashSet<>();
            ruleMoves.add(rule.getAction().convert());
            appliedMap.put(node, ruleMoves);
            if (safe)
                node.setAppliedRule(rule);

        }
    }

    private static boolean nodeApplies(
            ArrayList<Set<FFTNode>> sets, FFTNode key) {
        for (Set<FFTNode> set : sets) {
            if (!set.contains(key))
                return false;
        }
        return true;
    }

    private static boolean ruleApplies(
            ArrayList<Set<PropRule>> sets, PropRule key) {
        for (Set<PropRule> set : sets)
            if (!set.contains(key))
                return false;
        return true;
    }

    public boolean contains(PropRule rule) {
        for (Set<PropRule> set : ruleList)
            if (set.contains(rule))
                return true;
        return false;
    }

    public boolean contains(FFTNode node) {
        for (Set<FFTNode> set : nodeList)
            if (set.contains(node))
                return true;
        return false;
    }
}
