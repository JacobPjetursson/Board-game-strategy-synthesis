package fftlib.auxiliary.algo;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.RuleMapping;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.PredRule;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.SymmetryRule;
import misc.Config;

import java.util.*;
import java.util.function.Consumer;

import static misc.Config.SINGLE_THREAD;
import static misc.Config.SYMMETRY_DETECTION;

public class NodeList extends ArrayList<FFTNode> {

    private static final NodeComparator nc = new NodeComparator();
    private static ArrayList<Integer> sortedAtoms;
    static {
        initialize();
    }

    public static void initialize() {
        sortedAtoms = new ArrayList<>(FFTManager.sortedGameAtoms);
        // remove all negative atoms (since we dont use those in the ruleList)
        ArrayList<Integer> removeList = new ArrayList<>();
        for (int atom : sortedAtoms) {
            String name = FFTManager.getAtomName.apply(atom);
            if (name.startsWith("!"))
                removeList.add(atom);
        }
        sortedAtoms.removeAll(removeList);
    }

    public NodeList() {
        super();
    }

    public void findNodes(Rule r, Map<FFTNode, RuleMapping> appliedMap) {
        if (SYMMETRY_DETECTION && !(r instanceof SymmetryRule)) {
            PropRule propRule = (PropRule) r;
            Consumer<SymmetryRule> findFromSymmetry = (sRule -> {
                findNodes(sRule, appliedMap);
            });
            if (SINGLE_THREAD)
                propRule.getSymmetryRules().forEach(findFromSymmetry);
            else
                propRule.getSymmetryRules().parallelStream().forEach(findFromSymmetry);
        }
        else {
            LiteralSet lSet = r.getAllPreconditions();
            FFTMove m = r.getAction().convert();
            List<FFTNode> nodes = findNodes(lSet);
            Consumer<FFTNode> addToSet = (node -> {
                node.addToAppliedMap(appliedMap, r, m);
            });

            if (SINGLE_THREAD)
                nodes.forEach(addToSet);
            else
                nodes.parallelStream().forEach(addToSet);
        }
    }

    public List<FFTNode> findNodes(LiteralSet lSet) {
        return filterByAtom(0, lSet, this);
    }

    private List<FFTNode> filterByAtom(int atomIdx, LiteralSet lSet, List<FFTNode> nodes) {
        if (atomIdx >= sortedAtoms.size())
            return nodes;

        List<FFTNode> matchingNodes = new ArrayList<>();
        int atom = sortedAtoms.get(atomIdx++);

        Literal pos = new PropLiteral(atom);
        Literal neg = new PropLiteral(atom);
        neg.setNegated(true);

        // find interval
        int posEnd = findInterval(pos, nodes);
        List<FFTNode> posList = nodes.subList(0, posEnd);
        List<FFTNode> negList = nodes.subList(posEnd, nodes.size());
        if (lSet.contains(pos)) {
            // rule contains pos, add posList
            matchingNodes.addAll(filterByAtom(atomIdx, lSet, posList));
        } else if (lSet.contains(neg)) {
            // rule contains neg, add negList
            matchingNodes.addAll(filterByAtom(atomIdx, lSet, negList));
        } else {
            // rule contains neither, add both
            matchingNodes.addAll(filterByAtom(atomIdx, lSet, posList));
            matchingNodes.addAll(filterByAtom(atomIdx, lSet, negList));
        }
        return matchingNodes;
    }

    // look for first index where value (rule) does not contain 'l'
    // It is always a worst-case search, as we need to verify that the prev rule in fact has the lit
    // if 'start' is true, then we search for the first idx where 'l' is not in rules
    // if 'start' is false, then we search for the first idx where 'l' is in rules
    private int findInterval(Literal l, List<FFTNode> nodes) {
        int low = 0;
        int high = nodes.size() - 1;
        int foundIdx = nodes.size();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            LiteralSet precons = nodes.get(mid).convert();
            if (precons.contains(l))
                low = mid + 1;
            else {
                foundIdx = mid;
                high = mid - 1;
            }
        }
        return foundIdx;
    }

    public synchronized boolean add(FFTNode node) {
        return super.add(node);
    }

    public synchronized boolean remove(FFTNode node) {
        return super.remove(node);
    }

    // maintains ordering
    public synchronized void sortedAdd(FFTNode node) {
        int index = Collections.binarySearch(this, node, nc);
        if (index < 0)
            index = -index - 1;
        add(index, node);
    }

    // n + log(n) instead of (n + n)
    public synchronized boolean sortedRemove(FFTNode node) {
        int index = Collections.binarySearch(this, node, nc);
        if (index < 0)
            return false; // no element
        remove(index);
        return true;
    }

    public synchronized boolean sortedContains(FFTNode n) {
        int index = Collections.binarySearch(this, n, nc);
        return index >= 0;
    }

    private static class NodeComparator implements Comparator<FFTNode> {
        @Override
        public int compare(FFTNode n1, FFTNode n2) {
            LiteralSet set1 = n1.convert();
            LiteralSet set2 = n2.convert();
            for (int atm : sortedAtoms) {
                Literal l = new PropLiteral(atm);
                boolean s1Contains = set1.contains(l);
                boolean s2Contains = set2.contains(l);
                if (s1Contains && !s2Contains)
                    return -1;
                if (s2Contains && !s1Contains)
                    return 1;
                l.setNegated(true);
                s1Contains = set1.contains(l);
                s2Contains = set2.contains(l);
                if (s1Contains && !s2Contains)
                    return 1;
                if (s2Contains && !s1Contains)
                    return -1;

            }
            return 0;
        }
    }
}
