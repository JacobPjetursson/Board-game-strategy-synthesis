package fftlib.logic;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RuleList extends ArrayList<Rule> {

    private static final RuleComparator rc = new RuleComparator();
    private static final ArrayList<Integer> sortedAtoms = FFTManager.sortedGameAtoms;

    public FFTMove apply(FFTNode n) {
        Rule r = findRule(n.convert().getAll());
        if (r == null)
            return null;
        return r.action.convert();
    }

    public Rule findRule(LiteralSet lSet) {
        List<Rule> appliedRules = filterByAtom(0, lSet, this);
        if (appliedRules.isEmpty())
            return null;
        int minIdx = size();
        Rule firstRule = null;
        for (Rule r : appliedRules) {
            if (r.getRuleIndex() < minIdx) {
                minIdx = r.getRuleIndex();
                firstRule = r;
            }
        }
        return firstRule;
    }

    private List<Rule> filterByAtom(int atomIdx, LiteralSet lSet, List<Rule> rules) {
        if (atomIdx >= sortedAtoms.size())
            return rules;

        ArrayList<Rule> appliedRules = new ArrayList<>();
        int atom = sortedAtoms.get(atomIdx++);

        Literal pos = new Literal(atom);
        Literal neg = new Literal(atom, true);

        int posEnd = findInterval(pos, true, rules);
        int negStart = findInterval(neg, false, rules);

        List<Rule> middleList = null, sideList = null;
        if (posEnd != rules.size() && negStart != 0) // middle list exists
            middleList = rules.subList(posEnd, negStart);
        // trim one side
        if (posEnd != 0 && lSet.contains(pos))
            sideList = rules.subList(0, posEnd);
        else if (negStart != rules.size() && lSet.contains(neg))
            sideList = rules.subList(negStart, rules.size());

        if (middleList != null)
            appliedRules.addAll(filterByAtom(atomIdx, lSet, middleList));
        if (sideList != null)
            appliedRules.addAll(filterByAtom(atomIdx, lSet, sideList));
        return appliedRules;
    }


    // look for first index where value (rule) does not contain 'l'
    // It is always a worst-case search, as we need to verify that the prev rule in fact has the lit
    // if 'start' is true, then we search for the first idx where 'l' is not in rules
    // if 'start' is false, then we search for the first idx where 'l' is in rules
    private int findInterval(Literal l, boolean start, List<Rule> rules) {
        int low = 0;
        int high = rules.size() - 1;
        int foundIdx = rules.size();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            LiteralSet precons = rules.get(mid).getAllPreconditions();
            if ((start && precons.contains(l)) ||
                    (!start && !precons.contains(l)))
                low = mid + 1;
            else {
                foundIdx = mid;
                high = mid - 1;
            }
        }
        return foundIdx;
    }

    // maintains ordering
    public void sortedAdd(Rule r) {
        int index = Collections.binarySearch(this, r, rc);
        if (index < 0) {
            index = -index - 1;
        }
        add(index, r);
    }

    // n + log(n) instead of (n + n)
    public boolean sortedRemove(Rule r) {
        int index = Collections.binarySearch(this, r, rc);
        if (index < 0) {
            return false; // no element
        }
        remove(index);
        return true;
    }

    private static class RuleComparator implements Comparator<Rule> {
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
