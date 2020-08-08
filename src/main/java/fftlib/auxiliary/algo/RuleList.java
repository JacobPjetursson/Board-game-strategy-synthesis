package fftlib.auxiliary.algo;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.RuleMapping;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.PredRule;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.SymmetryRule;
import misc.Config;

import java.util.*;

public class RuleList extends ArrayList<Rule> {

    private static final RuleComparator rc = new RuleComparator();
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

    public RuleList() {
        super();
    }

    public RuleMapping apply(FFTNode n) {
        return findMoves(n);
    }

    public RuleMapping findMoves(FFTNode n) {
        LiteralSet lSet = n.convert();
        List<Rule> appliedRules = findRules(lSet);

        if (appliedRules.isEmpty())
            return RuleMapping.NOMATCH;

        int minIdx = Integer.MAX_VALUE;
        Rule firstRule = null;
        for (Rule r : appliedRules) {
            if (r.getRuleIndex() < minIdx) {
                minIdx = r.getRuleIndex();
                firstRule = r;
            }
        }
        HashSet<FFTMove> moves = new HashSet<>();
        moves.add(firstRule.getAction().convert()); // can't be null
        if (!Config.SYMMETRY_DETECTION && !Config.USE_LIFTING)
            return new RuleMapping(firstRule, moves);

        for (Rule r : appliedRules)
            if (r.getRuleIndex() == minIdx) {
                moves.add(r.getAction().convert());
            }

        return new RuleMapping(firstRule, moves);
    }

    public List<Rule> findRules(LiteralSet lSet) {
        return filterByAtom(0, lSet, this);
    }

    private List<Rule> filterByAtom(int atomIdx, LiteralSet lSet, List<Rule> rules) {
        // rules is never empty since we only call function if non-empty
        if (atomIdx >= sortedAtoms.size())
            return rules;

        ArrayList<Rule> appliedRules = new ArrayList<>();
        int atom = sortedAtoms.get(atomIdx++);

        Literal pos = new PropLiteral(atom);
        Literal neg = new PropLiteral(atom);
        neg.setNegated(true);

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

    public boolean add(Rule rule) {
        if (Config.USE_LIFTING && rule instanceof PredRule) {
            PredRule pr = (PredRule) rule;
            for (Rule ru : pr.getGroundedPropRules())
                this.add(ru);
        }
        else if (rule instanceof PropRule){
            PropRule r = (PropRule) rule;
            if (Config.SYMMETRY_DETECTION) {
                for (Rule ru : r.getSymmetryRules())
                    super.add(ru);
            } else {
                super.add(r);
            }
        } else {
            super.add(rule);
        }
        return true;
    }

    public boolean remove(Rule rule) {
        if (Config.USE_LIFTING && rule instanceof PredRule) {
            PredRule pr = (PredRule) rule;
            for (PropRule ru : pr.getGroundedPropRules())
                this.remove(ru);
        }
        else {
            PropRule r = (PropRule) rule;
            if (Config.SYMMETRY_DETECTION) {
                for (Rule ru : r.getSymmetryRules())
                    super.remove(ru);
            } else {
                super.remove(r);
            }
        }
        return true;
    }

    // maintains ordering
    public void sortedAdd(Rule rule) {
        if (Config.USE_LIFTING && rule instanceof PredRule) {
            PredRule pr = (PredRule) rule;
            for (PropRule ru : pr.getGroundedPropRules())
                this.sortedAdd(ru);
        }
        else {
            PropRule r = (PropRule) rule;
            if (Config.SYMMETRY_DETECTION) {
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
    }

    // n + log(n) instead of (n + n)
    public boolean sortedRemove(Rule rule) {
        if (Config.USE_LIFTING && rule instanceof PredRule) {
            PredRule pr = (PredRule) rule;
            for (PropRule ru : pr.getGroundedPropRules())
                this.sortedRemove(ru);
        }
        else {
            PropRule r = (PropRule) rule;
            if (Config.SYMMETRY_DETECTION) {
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
            // differentiate between rules with different ruleIndex
            // (since this is possible in the FFT)
            return r1.getRuleIndex() - r2.getRuleIndex();
        }
    }
}
