package fftlib.auxiliary;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.logic.Literal;
import fftlib.logic.LiteralSet;
import fftlib.logic.Rule;
import misc.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static misc.Config.SINGLE_THREAD;

public class InvertedList {
    private ArrayList<HashMap<FFTNode, FFTNode>> nodeList = new ArrayList<>();
    private ArrayList<HashMap<Rule, Rule>> ruleList = new ArrayList<>();

    public InvertedList(boolean storeNodes) {
        // add extra map to account for atom 1-indexing
        if (storeNodes)
            nodeList.add(new HashMap<>());
        else
            ruleList.add(new HashMap<>());

        for (int ignored : FFTManager.getGameAtoms.get()) {
            if (storeNodes)
                nodeList.add(new HashMap<>());
            else
                ruleList.add(new HashMap<>());
        }
    }

    public void add(FFTNode node) {
        LiteralSet lSet = node.convert();
        for (Literal l : lSet)
            nodeList.get(l.id).put(node, node);
    }

    // need to add both positive and negated atom if it is not present in the preconditions
    public void add(Rule rule) {
        for (int atom : FFTManager.getGameAtoms.get()) {
            LiteralSet precons = rule.getAllPreconditions();
            Literal pos = new Literal(atom);
            Literal neg = new Literal(atom);
            neg.setNegated(true);
            // add if contains pos or does not contain neg
            if (precons.contains(pos) || !precons.contains(neg))
                ruleList.get(pos.id).put(rule, rule);
            if (precons.contains(neg) || !precons.contains(pos))
                ruleList.get(neg.id).put(rule, rule);

        }
    }

    public void remove(FFTNode node) {
        for (HashMap<FFTNode, FFTNode> map : nodeList)
            map.remove(node);
    }

    public void remove(Rule rule) {
        for (HashMap<Rule, Rule> map : ruleList) {
            map.remove(rule);
        }
    }

    public HashSet<FFTMove> apply(FFTNode node) {
        return findMoves(node.convert());
    }

    public HashSet<FFTMove> findMoves(LiteralSet nodeSet) {
        if (ruleList.isEmpty())
            return new HashSet<>();
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<HashMap<Rule, Rule>> relevantMaps = new ArrayList<>();
        HashMap<Rule, Rule> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : nodeSet) {
            HashMap<Rule, Rule> invertedEntry = ruleList.get(l.id);
            relevantMaps.add(invertedEntry);
            if (invertedEntry.size() < smallestSize) {
                smallestSize = invertedEntry.size();
                smallest = invertedEntry;
            }
        }
        // iterate over smallest and look entry up in all relevant lists
        // break if element is not in list
        // if element is in all lists, add to new set
        Rule firstRule = null;
        int firstIndex = Integer.MAX_VALUE;
        ArrayList<Rule> candidates = new ArrayList<>();

        for (Map.Entry<Rule, Rule> entry : smallest.entrySet()) {
            if (entry.getValue().getRuleIndex() <= firstIndex && ruleApplies(relevantMaps, entry.getKey())) {
                firstRule = entry.getValue();
                firstIndex = firstRule.getRuleIndex();
                candidates.add(firstRule);
            }
        }
        HashSet<FFTMove> moves = new HashSet<>();
        if (firstRule == null) // no rule applies
            return moves;
        moves.add(firstRule.getAction().convert()); // can't be null
        if (!Config.SYMMETRY_DETECTION && !Config.USE_LIFTING)
            return moves;

        for (Rule r : candidates)
            if (r.getRuleIndex() == firstIndex)
                moves.add(r.getAction().convert());

        return moves;
    }

    // todo - ONLY WORKS WITH NON-SYMMETRY AND NO LIFTING
    // todo - we can fix it by calling findNodes for each symmetric rule,
    // todo      and concatenate the actions if multiple symmetries applies
    public void findNodes(Rule rule, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        if (nodeList.isEmpty())
            return;
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<HashMap<FFTNode, FFTNode>> relevantMaps = new ArrayList<>();
        HashMap<FFTNode, FFTNode> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : rule.getAllPreconditions()) {
            HashMap<FFTNode, FFTNode> invertedEntry = nodeList.get(l.id);
            relevantMaps.add(invertedEntry);
            if (invertedEntry.size() < smallestSize) {
                smallestSize = invertedEntry.size();
                smallest = invertedEntry;
            }
        }
        // iterate over smallest and look entry up in all relevant lists
        // break if element is not in list
        // if element is in all lists, add to new set
        if (!SINGLE_THREAD) {
            smallest.entrySet().parallelStream().forEach(entry -> {
                if (nodeApplies(relevantMaps, entry.getKey())) {
                    HashSet<FFTMove> ruleMoves = new HashSet<>();
                    ruleMoves.add(rule.getAction().convert());
                    appliedMap.put(entry.getValue(), ruleMoves);
                }
            });
        } else {
            for (Map.Entry<FFTNode, FFTNode> entry : smallest.entrySet()) {
                if (nodeApplies(relevantMaps, entry.getKey())) {
                    HashSet<FFTMove> ruleMoves = new HashSet<>();
                    ruleMoves.add(rule.getAction().convert());
                    appliedMap.put(entry.getValue(), ruleMoves);
                }
            }
        }
    }

    public static boolean nodeApplies(
            ArrayList<HashMap<FFTNode, FFTNode>> maps, FFTNode key) {
        for (HashMap<FFTNode, FFTNode> map : maps) {
            if (!map.containsKey(key))
                return false;
        }
        return true;
    }

    public static boolean ruleApplies(
            ArrayList<HashMap<Rule, Rule>> maps, Rule key) {
        for (HashMap<Rule, Rule> map : maps)
            if (!map.containsKey(key))
                return false;
        return true;
    }

    public boolean contains(Rule rule) {
        for (HashMap<Rule, Rule> map : ruleList)
            if (map.containsKey(rule))
                return true;
        return false;
    }

    public boolean contains(FFTNode node) {
        for (HashMap<FFTNode, FFTNode> map : nodeList)
            if (map.containsKey(node))
                return true;
        return false;
    }


    public ArrayList<HashMap<FFTNode, FFTNode>> getNodeList() {
        return nodeList;
    }

    public ArrayList<HashMap<Rule, Rule>> getRuleList() {
        return ruleList;
    }
}
