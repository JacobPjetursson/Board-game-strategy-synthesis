package fftlib.auxiliary;

import fftlib.FFTManager;
import fftlib.game.FFTNode;
import fftlib.logic.Literal;
import fftlib.logic.LiteralSet;
import fftlib.logic.Rule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InvertedList {
    private boolean storeNodes;
    private ArrayList<HashMap<BigInteger, FFTNode>> nodeList = new ArrayList<>();
    private ArrayList<HashMap<BigInteger, Rule>> ruleList = new ArrayList<>();

    public InvertedList(boolean storeNodes) {
        this.storeNodes = storeNodes;
        // add extra map to account for atom 1-indexing
        if (storeNodes)
            nodeList.add(new HashMap<>());
        else
            ruleList.add(new HashMap<>());

        for (int atom : FFTManager.getGameAtoms.get()) {
            if (storeNodes)
                nodeList.add(new HashMap<>());
            else
                ruleList.add(new HashMap<>());
        }
    }

    public void add(FFTNode node) {
        LiteralSet lSet = node.convert();
        for (Literal l : lSet)
            nodeList.get(l.id).put(lSet.getBitString(), node);
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
                ruleList.get(pos.id).put(precons.getBitString(), rule);
            if (precons.contains(neg) || !precons.contains(pos))
                ruleList.get(pos.id).put(precons.getBitString(), rule);

        }
    }

    public void remove(FFTNode node) {
        for (HashMap<BigInteger, FFTNode> map : nodeList)
            map.remove(node.convert().getBitString());
    }

    public void remove(Rule rule) {
        for (HashMap<BigInteger, Rule> map : ruleList) {
            map.remove(rule.getBitString());
        }
    }

    public Rule findRule(FFTNode node) {
        if (ruleList.isEmpty())
            return null;
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<HashMap<BigInteger, Rule>> relevantMaps = new ArrayList<>();
        HashMap<BigInteger, Rule> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : node.convert()) {
            HashMap<BigInteger, Rule> invertedEntry = ruleList.get(l.id);
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
        for (Map.Entry<BigInteger, Rule> entry : smallest.entrySet()) {
            boolean applies = true;
            for (HashMap<BigInteger, Rule> map : relevantMaps) {
                if (!map.containsKey(entry.getKey())) {
                    applies = false;
                    break;
                }
            }
            if (applies && entry.getValue().getRuleIndex() < firstIndex) {
                firstRule = entry.getValue();
                firstIndex = firstRule.getRuleIndex();
            }
        }
        return firstRule;
    }

    public HashSet<FFTNode> findNodes(Rule rule) {
        if (nodeList.isEmpty())
            return new HashSet<>();
        // start by finding the smallest list, as well as all relevant lists
        ArrayList<HashMap<BigInteger, FFTNode>> relevantMaps = new ArrayList<>();
        HashMap<BigInteger, FFTNode> smallest = null;

        int smallestSize = Integer.MAX_VALUE;
        for (Literal l : rule.getAllPreconditions()) {
            HashMap<BigInteger, FFTNode> invertedEntry = nodeList.get(l.id);
            relevantMaps.add(invertedEntry);
            if (invertedEntry.size() < smallestSize) {
                smallestSize = invertedEntry.size();
                smallest = invertedEntry;
            }
        }
        if (smallest == null) {
            System.out.println("all precons: " + rule.getAllPreconditions());
            System.out.println("nodeList: " + nodeList);
        }
        // iterate over smallest and look entry up in all relevant lists
        // break if element is not in list
        // if element is in all lists, add to new set
        HashSet<FFTNode> appliedSet = new HashSet<>();
        for (Map.Entry<BigInteger, FFTNode> entry : smallest.entrySet()) {
            boolean applies = true;
            for (HashMap<BigInteger, FFTNode> map : relevantMaps) {
                if (!map.containsKey(entry.getKey())) {
                    applies = false;
                    break;
                }
            }
            if (applies) {
                appliedSet.add(entry.getValue());
            }
        }
        return appliedSet;
    }

    public ArrayList<HashMap<BigInteger, FFTNode>> getNodeList() {
        return nodeList;
    }

    public ArrayList<HashMap<BigInteger, Rule>> getRuleList() {
        return ruleList;
    }
}
