package fftlib.auxiliary.algo;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.RuleMapping;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.PropRule;
import misc.Config;

import java.util.*;

import static misc.Config.SINGLE_THREAD;

public class InvertedList {
    public ArrayList<Set<FFTNode>> nodes = new ArrayList<>();
    public ArrayList<Set<PropRule>> rules = new ArrayList<>();

    public InvertedList(boolean storeNodes) {
        // add extra map to account for atom 1-indexing
        if (storeNodes)
            nodes.add(new HashSet<>());
        else
            rules.add(new HashSet<>());

        for (int ignored : FFTManager.getGameAtoms.get()) {
            if (storeNodes)
                nodes.add(new HashSet<>());
            else
                rules.add(new HashSet<>());
        }
    }

    public Set<FFTNode> getAll() {
        Set<FFTNode> all = new HashSet<>();
        for (int i : FFTManager.getGameAtoms.get()) {
            all.addAll(nodes.get(i));
        }
        return all;
    }

    public void add(FFTNode node) {
        LiteralSet lSet = node.convert();
        for (Literal l : lSet)
            nodes.get(l.id).add(node);
    }

    // need to add both positive and negated atom if it is not present in the preconditions
    public void add(PropRule rule) {
        for (int atom : FFTManager.getGameAtoms.get()) {
            LiteralSet precons = rule.getAllPreconditions();
            Literal pos = new PropLiteral(atom);
            Literal neg = new PropLiteral(atom);
            neg.setNegated(true);
            // add if contains pos or does not contain neg
            if (precons.contains(pos) || !precons.contains(neg))
                rules.get(pos.id).add(rule);
            if (precons.contains(neg) || !precons.contains(pos))
                rules.get(neg.id).add(rule);

        }
    }

    public void remove(FFTNode node) {
        for (Set<FFTNode> set : nodes)
            set.remove(node);
    }

    public void remove(PropRule rule) {
        for (Set<PropRule> set : rules) {
            set.remove(rule);
        }
    }

    public RuleMapping apply(FFTNode node) {
        return findMoves(node);
    }

    public RuleMapping findMoves(FFTNode node) {
        LiteralSet nodeSet = node.convert();
        if (rules.isEmpty())
            return RuleMapping.NOMATCH;
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<Set<PropRule>> relevantSets = new ArrayList<>();
        Set<PropRule> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : nodeSet) {
            Set<PropRule> invertedEntry = rules.get(l.id);
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
            return RuleMapping.NOMATCH;
        moves.add(firstRule.getAction().convert()); // can't be null
        if (!Config.SYMMETRY_DETECTION && !Config.USE_LIFTING) {
            return new RuleMapping(firstRule, moves);
        }

        // pick up moves from other symmetric rules
        for (PropRule r : candidates)
            if (r.getRuleIndex() == firstIndex)
                moves.add(r.getAction().convert());

        return new RuleMapping(firstRule, moves);
    }

    // todo - ONLY WORKS WITH NON-SYMMETRY AND NO LIFTING
    // todo - we can fix it by calling findNodes for each symmetric rule,
    // todo      and concatenate the actions if multiple symmetries applies
    public void findNodes(Rule rule, Map<FFTNode, RuleMapping> appliedMap) {
        if (nodes.isEmpty())
            return;
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<Set<FFTNode>> relevantSets = new ArrayList<>();
        Set<FFTNode> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : rule.getAllPreconditions()) {
            Set<FFTNode> invertedEntry = nodes.get(l.id);
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
                    insert(relevantSets, node, rule, appliedMap));
        } else {
            for (FFTNode node : smallest) {
                insert(relevantSets, node, rule, appliedMap);
            }
        }
    }

    private static void insert(ArrayList<Set<FFTNode>> sets, FFTNode node,
                               Rule rule, Map<FFTNode, RuleMapping> appliedMap) {
        if (nodeApplies(sets, node)) {
            Set<FFTMove> ruleMoves = new HashSet<>();
            ruleMoves.add(rule.getAction().convert());
            appliedMap.put(node, new RuleMapping(rule, ruleMoves));

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
        for (Set<PropRule> set : rules)
            if (set.contains(rule))
                return true;
        return false;
    }

    public boolean contains(FFTNode node) {
        for (Set<FFTNode> set : nodes)
            if (set.contains(node))
                return true;
        return false;
    }
}
