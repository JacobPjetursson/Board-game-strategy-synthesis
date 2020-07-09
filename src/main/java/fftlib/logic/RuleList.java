package fftlib.logic;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import misc.Config;

import java.util.*;

public class RuleList extends ArrayList<Rule> {

    private static final RuleComparator rc = new RuleComparator();
    private static ArrayList<Integer> sortedAtoms;
    static {
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

    RuleList() {
        super();
    }

    RuleList(RuleList duplicate) {
        super();
        for (Rule r : duplicate) {
            add(new Rule(r));
        }
    }

    public HashSet<FFTMove> apply(FFTNode n) {
        return findMoves(n.convert());
    }

    public HashSet<FFTMove> findMoves(LiteralSet lSet) {
        List<Rule> appliedRules = findRules(lSet);

        if (appliedRules.isEmpty())
            return new HashSet<>();

        int minIdx = Integer.MAX_VALUE;
        Rule firstRule = null;
        for (Rule r : appliedRules) {
            if (r.getRuleIndex() < minIdx) {
                minIdx = r.getRuleIndex();
                firstRule = r;
            }
        }
        HashSet<FFTMove> moves = new HashSet<>();
        moves.add(firstRule.action.convert()); // can't be null
        if (!Config.SYMMETRY_DETECTION && !Config.USE_LIFTING)
            return moves;

        for (Rule r : appliedRules)
            if (r.getRuleIndex() == minIdx)
                moves.add(r.action.convert());

        return moves;
    }

    public List<Rule> findRules(LiteralSet lSet) {
        return filterByAtom(0, lSet, this);
    }

    private List<Rule> filterByAtom(int atomIdx, LiteralSet lSet, List<Rule> rules) {
        if (atomIdx >= sortedAtoms.size())
            return rules;

        ArrayList<Rule> appliedRules = new ArrayList<>();
        int atom = sortedAtoms.get(atomIdx++);
        // todo
        Literal pos = new Literal(atom);
        String negName = "!" + pos.getName();
        Literal neg = new Literal(negName);

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

    public boolean add(Rule r) {
        if (Config.USE_LIFTING && r instanceof PredRule) {
            PredRule pr = (PredRule) r;
            for (Rule ru : pr.getGroundedRules())
                this.add(ru);
        }
        else if (Config.SYMMETRY_DETECTION) {
            for (Rule ru : r.getSymmetryRules())
                super.add(ru);
        } else {
            super.add(r);
        }
        return true;
    }

    public boolean remove(Rule r) {
        if (Config.USE_LIFTING && r instanceof PredRule) {
            PredRule pr = (PredRule) r;
            for (Rule ru : pr.getGroundedRules())
                this.remove(ru);
        }
        else if (Config.SYMMETRY_DETECTION) {
            for (Rule ru : r.getSymmetryRules())
                super.remove(ru);
        } else {
            super.remove(r);
        }
        return true;
    }

    // maintains ordering
    public void sortedAdd(Rule r) {
        if (Config.USE_LIFTING && r instanceof PredRule) {
            PredRule pr = (PredRule) r;
            for (Rule ru : pr.getGroundedRules())
                this.sortedAdd(ru);
        }
        else if (Config.SYMMETRY_DETECTION) {
            for (Rule ru : r.getSymmetryRules()) {
                int index = Collections.binarySearch(this, ru, rc);
                if (index < 0)
                    index = -index - 1;
                add(index, ru);
            }
        } else {
            int index = Collections.binarySearch(this, r, rc);
            if (index < 0)
                index = -index - 1;
            add(index, r);
        }
    }

    // n + log(n) instead of (n + n)
    public boolean sortedRemove(Rule r) {
        if (Config.USE_LIFTING && r instanceof PredRule) {
            PredRule pr = (PredRule) r;
            for (Rule ru : pr.getGroundedRules())
                this.sortedRemove(ru);
        }
        else if (Config.SYMMETRY_DETECTION) {
            boolean removed = true;
            for (Rule ru : r.getSymmetryRules()) {
                // return true if all rules removed
                int index = Collections.binarySearch(this, ru, rc);
                if (index < 0)
                    removed = false; // no element
                else
                    remove(index);
            }
            return removed;
        } else {
            int index = Collections.binarySearch(this, r, rc);
            if (index < 0)
                return false; // no element
            remove(index);
        }
        return true;
    }

    public void sort() {
        sort(rc);
    }

    private static class RuleComparator implements Comparator<Rule> {
        @Override
        public int compare(Rule r1, Rule r2) {
            LiteralSet set1 = r1.getAllPreconditions();
            LiteralSet set2 = r2.getAllPreconditions();
            for (int atm : sortedAtoms) {
                Literal l = new Literal(atm);
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
            // differentiate between rules with different ruleIndex
            // (since this is possible in the FFT)
            return r1.getRuleIndex() - r2.getRuleIndex();
        }
    }
}
