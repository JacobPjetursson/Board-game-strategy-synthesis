package fftlib.auxiliary.algo;

import fftlib.game.*;
import fftlib.logic.rule.Rule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static misc.Config.*;
import static misc.Globals.*;

/** This class offers a very dynamic setup that supports various structures all at once that shares common functions,
 * such as put(), get(), etc.
 * It helps compress a lot of the code in the FFTAutoGen class
 */
public class NodeMap {

    public static final int NO_TYPE = 0;
    public static final int TYPE_NODE_LIST = 2;
    public static final int TYPE_INVERTED_LIST = 3;

    private Map<FFTNode, FFTNode> map;
    private final int type;

    private InvertedList invertedList;
    private NodeList nodeList;

    public NodeMap(int type) {
        this.type = type;
        // ignore sorts (for now) if multithreading
        if (type == NO_TYPE) {
            if (SINGLE_THREAD)
                map = new HashMap<>();
            else
                map = new ConcurrentHashMap<>();
        }
        else if (type == TYPE_NODE_LIST) {
            nodeList = new NodeList();
        }
        else if (type == TYPE_INVERTED_LIST)
            invertedList = new InvertedList(true);
    }

    public int getType() {
        return type;
    }


    public void add(FFTNode n) {
        if (type == TYPE_INVERTED_LIST)
            invertedList.add(n);
        else if (type == TYPE_NODE_LIST)
            nodeList.sortedAdd(n);
        else
            map.put(n, n);

    }

    public void remove(FFTNode n) {
        if (type == TYPE_INVERTED_LIST)
            invertedList.remove(n);
        else if (type == TYPE_NODE_LIST)
            nodeList.sortedRemove(n);
        else
            map.remove(n);
    }

    // NOTE: only works for NO_TYPE
    public Collection<FFTNode> values() {
        return map.values();
    }

    // NOTE: only works for NO_TYPE
    public FFTNode get(FFTNode key) {
        return map.get(key);
    }

    public int size() {
        if (type == NO_TYPE)
            return map.size();
        if (type == TYPE_INVERTED_LIST)
            return -1; // hard to determine number of unique elements in this structure
        return nodeList.size();
    }

    public boolean contains(FFTNode n) {
        if (type == NO_TYPE)
            return map.containsKey(n);
        if (type == TYPE_INVERTED_LIST)
            return invertedList.contains(n);
        return nodeList.sortedContains(n);
    }

    public Collection<FFTNode> getAll() {
        if (type == NO_TYPE)
            return map.keySet();
        if (type == TYPE_INVERTED_LIST)
            return invertedList.getAll();
        return nodeList;
    }

    public void findNodes(Rule r, Map<FFTNode, RuleMapping> appliedMap) {
        if (USE_INVERTED_LIST_NODES_OPT) {
            invertedList.findNodes(r, appliedMap);
        }
        else if (USE_NODELIST) {
            nodeList.findNodes(r,appliedMap);
        }
        else if (!SINGLE_THREAD) {
            values().parallelStream().forEach(node ->
                    insert(node, r, appliedMap));
        }
        else {
            for (FFTNode n : values())
                insert(n, r, appliedMap);
        }
    }

    private void insert(FFTNode n, Rule r, Map<FFTNode, RuleMapping> nodes) {
        Set<FFTMove> moves = r.apply(n);
        if (!moves.isEmpty())
            nodes.put(n, new RuleMapping(r, moves));
    }

    public void removeAll(Collection<FFTNode> nodes) {
        for (FFTNode n : nodes)
            remove(n);
    }

    public void putAll(Collection<FFTNode> nodes) {
        for (FFTNode n : nodes)
            add(n);
    }

    public Map<FFTNode, FFTNode> getMap() {
        return map;
    }

    public NodeList getNodeList() {
        return nodeList;
    }

    public InvertedList getInvertedList() {
        return invertedList;
    }


    // todo - use this some other place
    // Allows us to sort the nodes based on custom values, such as which node is closed to a terminal node
    public static class NodeComparator implements Comparator<FFTNode> {
        @Override
        public int compare(FFTNode n1, FFTNode n2) {
            if (RULE_ORDERING == RULE_ORDERING_TERMINAL_LAST ||
                    RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST) {
                NodeMapping nm1 = FFTSolution.queryNode(n1);
                NodeMapping nm2 = FFTSolution.queryNode(n2);
                if (nm1 == null) {
                    if (nm2 == null)
                        return compareBitString(n1, n2);
                    return -1;
                } else if (nm2 == null) {
                    return 1;
                }
                int n1_score = Math.abs(nm1.getScore());
                int n2_score = Math.abs(nm2.getScore());

                if (RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST) {
                    if (n1_score > n2_score)
                        return -1;
                    else if (n2_score > n1_score)
                        return 1;
                    else {
                        return compareBitString(n1, n2);
                    }
                }
            }
            int n1_precons_amount = n1.convert().size();
            int n2_precons_amount = n2.convert().size();
            if (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST) {
                if (n1_precons_amount > n2_precons_amount)
                    return n1.hashCode() - n2.hashCode();
                return n2.hashCode() - n1.hashCode();
            }
            return 0;
        }

        private int compareBitString(FFTNode n1, FFTNode n2) {
            int bitStringDiff = n1.convert().getBitString().compareTo(n2.convert().getBitString());
            if (bitStringDiff > 0)
                return 1;
            else if (bitStringDiff < 0)
                return -1;
            return 0;
        }
    }
}
