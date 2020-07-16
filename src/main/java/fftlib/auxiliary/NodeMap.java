package fftlib.auxiliary;

import fftlib.game.FFTMove;
import fftlib.game.FFTSolution;
import fftlib.game.FFTNode;
import fftlib.game.NodeMapping;
import fftlib.logic.rule.Rule;

import java.math.BigInteger;
import java.util.*;

import static misc.Config.*;
import static misc.Globals.*;

/** This class offers a very dynamic setup that supports various structures all at once that shares common functions,
 * such as put(), get(), etc.
 * It helps compress a lot of the code in the FFTAutoGen class
 */
public class NodeMap {
    public static final int NO_SORT = 0;
    public static final int BITSTRING_SORT = 1;
    public static final int RULE_SORT = 2;

    // TODO - super non-important, but would be nice to have this class represented as a single map somehow
    private TreeMap<BigInteger, FFTNode> codeMap;
    private Map<FFTNode, FFTNode> map;
    private final int sort;
    private InvertedList nodeList;

    public NodeMap(int sort) {
        this.sort = sort;
        if (sort == BITSTRING_SORT)
            codeMap = new TreeMap<>(Comparator.reverseOrder());
        else if (sort == RULE_SORT) {
            map = new TreeMap<>(new NodeComparator());
        } else
            map = new HashMap<>();

        if (USE_INVERTED_LIST_NODES_OPT)
            nodeList = new InvertedList(true);
    }


    public void put(FFTNode n) {
        if (USE_INVERTED_LIST_NODES_OPT)
            nodeList.add(n);
        if (sort == BITSTRING_SORT)
            codeMap.put(n.convert().getBitString(), n);
        else
            map.put(n, n);

    }

    public FFTNode remove(FFTNode n) {
        if (USE_INVERTED_LIST_NODES_OPT)
            nodeList.remove(n);
        if (sort == BITSTRING_SORT)
            return codeMap.remove(n.convert().getBitString());
        return map.remove(n);
    }

    public Collection<FFTNode> values() {
        if (sort == BITSTRING_SORT)
            return codeMap.values();
        return map.values();
    }

    public FFTNode get(BigInteger key) {
        return codeMap.get(key);
    }

    public FFTNode get(FFTNode key) {
        return map.get(key);
    }

    public int size() {
        if (sort == BITSTRING_SORT)
            return codeMap.size();
        return map.size();
    }

    public boolean contains(FFTNode n) {
        if (sort == BITSTRING_SORT)
            return codeMap.containsKey(n.convert().getBitString());
        return map.containsKey(n);

    }

    public TreeMap<BigInteger, FFTNode> getCodeMap() {
        return codeMap;
    }

    public Map<FFTNode, FFTNode> getMap() {
        return map;
    }

    public void findNodes(Rule r, Map<FFTNode, Set<FFTMove>> appliedMap) {
        findNodes(r, appliedMap, false);
    }

    public void findNodes(Rule r, Map<FFTNode, Set<FFTMove>> appliedMap, boolean safe) {
        if (sort == BITSTRING_SORT) {
            BigInteger code = r.getAllPreconditions().getBitString();
            // multithreading has bad performance
            for (Map.Entry<BigInteger, FFTNode> entry : codeMap.entrySet()) {
                if (entry.getKey().compareTo(code) < 0)
                    break;
                insert(entry.getValue(), r, appliedMap, safe);

            }
        }
        else if (USE_INVERTED_LIST_NODES_OPT) {
            nodeList.findNodes(r, appliedMap, safe);
        }
        else if (!SINGLE_THREAD) {
            values().parallelStream().forEach(node ->
                    insert(node, r, appliedMap, safe));
        } else {
            for (FFTNode n : values()) {
                insert(n, r, appliedMap, safe);
            }
        }
    }

    private void insert(FFTNode n, Rule r, Map<FFTNode, Set<FFTMove>> nodes, boolean safe) {
        HashSet<FFTMove> moves = r.apply(n);
        if (!moves.isEmpty()) {
            nodes.put(n, moves);
            if (safe)
                n.setAppliedRule(r);
        }
    }

    public void removeAll(Collection<FFTNode> nodes) {
        for (FFTNode n : nodes)
            remove(n);
    }

    public void putAll(Collection<FFTNode> nodes) {
        for (FFTNode n : nodes)
            put(n);
    }


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
