package fftlib.auxiliary;

import fftlib.FFTSolution;
import fftlib.game.FFTNode;
import fftlib.game.NodeMapping;
import misc.Config;

import java.util.*;

import static misc.Config.RULE_ORDERING;
import static misc.Config.USE_BITSTRING_SORT_OPT;
import static misc.Globals.*;

/** This class offers a very dynamic setup that supports various structures all at once that shares common functions,
 * such as put(), get(), etc.
 * It helps compress a lot of the code in the FFTAutoGen class
 */
public class NodeMap {
    // TODO - super non-important, but would be nice to have this class represented as a single map somehow
    private TreeMap<Long, FFTNode> codeMap;
    private Map<FFTNode, FFTNode> map;

    public NodeMap() {
        if (Config.USE_BITSTRING_SORT_OPT)
            codeMap = new TreeMap<>(Comparator.reverseOrder());
        else if (Config.USE_RULE_ORDERING) {
            map = new TreeMap<>(new NodeComparator());
        } else
            map = new HashMap<>();
    }


    public void put(FFTNode n) {
        if (USE_BITSTRING_SORT_OPT) {
            codeMap.put(n.convert().getBitString(), n);
        }
        else
            map.put(n, n);
    }

    public FFTNode remove(FFTNode n) {
        if (USE_BITSTRING_SORT_OPT)
            return codeMap.remove(n.convert().getBitString());
        return map.remove(n);
    }

    public Collection<FFTNode> values() {
        if (USE_BITSTRING_SORT_OPT)
            return codeMap.values();
        return map.values();
    }

    public FFTNode get(long key) {
        return codeMap.get(key);
    }

    public FFTNode get(FFTNode key) {
        return map.get(key);
    }

    public int size() {
        if (USE_BITSTRING_SORT_OPT)
            return codeMap.size();
        return map.size();
    }

    public TreeMap<Long, FFTNode> getCodeMap() {
        return codeMap;
    }

    public Map<FFTNode, FFTNode> getMap() {
        return map;
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
            long bitStringDiff = n1.convert().getBitString() - n2.convert().getBitString();
            if (bitStringDiff > 0)
                return 1;
            else if (bitStringDiff < 0)
                return -1;
            return 0;
        }
    }


}
