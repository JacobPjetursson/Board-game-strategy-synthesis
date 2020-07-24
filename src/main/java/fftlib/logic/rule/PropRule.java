package fftlib.logic.rule;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.Move;

import java.math.BigInteger;
import java.util.*;

import static misc.Config.*;


public class PropRule extends Rule {

    private HashSet<SymmetryRule> symmetryRules;

    // parsing constructor
    public PropRule(String preconStr, String actionStr) {
        if (ENABLE_GGP_PARSER) {
            PropRule r = FFTManager.gdlToRule.apply(preconStr, actionStr);
            this.action = r.action;
            this.preconditions = r.preconditions;
        } else {
            this.action = parseAction(actionStr);
            this.preconditions = parsePreconditions(preconStr);
            initializeAllPreconditions();
        }
        removeActionPrecons();
        initializeSymmetryRules();
    }

    public PropRule(LiteralSet precons, Action action) {
        this.action = new Action(action);
        this.preconditions = new LiteralSet(precons);
        initializeAllPreconditions();
        removeActionPrecons();
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    // GDL Constructor
    public PropRule(Set<GdlSentence> sentences, Move move) {
        this.sentences = sentences;
        this.move = move;
    }

    // Empty constructor to allow rule buildup
    public PropRule() {
        this.preconditions = new LiteralSet();
        this.allPreconditions = new LiteralSet();
        this.action = new Action();
        if (SYMMETRY_DETECTION)
            this.symmetryRules = new HashSet<>();
    }

    public PropRule(Set<GdlSentence> sentences) {
        this.sentences = sentences;
    }

    // Duplicate constructor
    public PropRule(PropRule duplicate) {
        this.action = new Action(duplicate.action);
        this.preconditions = new LiteralSet(duplicate.preconditions);
        this.allPreconditions = new LiteralSet(duplicate.allPreconditions);
        this.ruleIndex = duplicate.ruleIndex;
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    public void setRuleIndex(int index) {
        ruleIndex = index;
        if (SYMMETRY_DETECTION)
            for (SymmetryRule ru : symmetryRules)
                ru.setRuleIndex(index);
    }

    @Override
    public Rule clone() {
        return new PropRule(this);
    }

    public BigInteger getBitString() {
        return getAllPreconditions().getBitString();
    }

    public void addPrecondition(Literal l) {
        if (!action.getPreconditions().contains(l)) // do not include precondition from action
            preconditions.add(l);
        allPreconditions.add(l);
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    @Override
    public void removeAction() {
        this.action = new Action();
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    public void removePrecondition(Literal l) {
        this.preconditions.remove(l);
        if (!action.getPreconditions().contains(l))
            this.allPreconditions.remove(l);
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    public void setAction(Action a) {
        if (a == null)
            this.action = new Action();
        else
            this.action = a;
        removeActionPrecons();
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    // Gdl
    public void setMove(Move m) {
        this.move = m;
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    public void setPreconditions(LiteralSet precons) {
        if (precons == null)
            this.preconditions = new LiteralSet();
        else
            this.preconditions = precons;
        if (SYMMETRY_DETECTION)
            initializeSymmetryRules();
    }

    // Gdl
    public void setSentences(Set<GdlSentence> sentences) {
        this.sentences = sentences;
        //this.transformedSentences = getTransformedSentences();
    }

    // Lifts a single propositional symbol to a predicate symbol
    // Takes as arg the index to lift
    // returns null if the lifting is illegal (it creates inconsistencies)
    public PredRule liftAll(int prop) {
        LiteralSet newPrecs = new LiteralSet();
        for (Literal l : preconditions) {
            PropLiteral pLit = (PropLiteral) l;
            newPrecs.add(pLit.liftAll(prop));
        }
        LiteralSet addSet = new LiteralSet();
        LiteralSet remSet = new LiteralSet();
        for (Literal l : action.adds) {
            PropLiteral pLit = (PropLiteral) l;
            addSet.add(pLit.liftAll(prop));
        }
        for (Literal l : action.rems) {
            PropLiteral pLit = (PropLiteral) l;
            remSet.add(pLit.liftAll(prop));
        }
        Action a = new Action(addSet, remSet);
        PredRule pr = new PredRule(newPrecs, a);
        if (pr.isInConsistent())
            return null;
        pr.setRuleIndex(this.ruleIndex);
        return pr;
    }

    // returns sorted list based on most reoccurring index (which we will prioritize when lifting)
    public ArrayList<Integer> getSortedProps() {
        TreeMap<Integer, Integer> occMap = new TreeMap<>();
        // map idx -> occ and then reverse map
        addToOccMap(occMap, preconditions);
        addToOccMap(occMap, action.adds);
        addToOccMap(occMap, action.rems);
        TreeMap<Integer, ArrayList<Integer>> reverseMap = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<Integer, Integer> entry : occMap.entrySet()) {
            int occ = entry.getValue();
            int idx = entry.getKey();
            if (reverseMap.containsKey(occ))
                reverseMap.get(occ).add(idx);
            else {
                ArrayList<Integer> newList = new ArrayList<>();
                newList.add(idx);
                reverseMap.put(occ, newList);
            }
        }
        ArrayList<Integer> props = new ArrayList<>();
        for (ArrayList<Integer> vals : reverseMap.values())
            props.addAll(vals);
        return props;
    }

    private void addToOccMap(TreeMap<Integer, Integer> occMap, LiteralSet litSet) {
        for (Literal l : litSet) {
            for (int i : l.getIndices()) {
                if (occMap.containsKey(i))
                    occMap.put(i, occMap.get(i)+1);
                else
                    occMap.put(i, 1);
            }
        }
    }

    private void initializeSymmetryRules() {
        HashSet<SymmetryRule> symRules = FFTManager.getSymmetryRules.apply(this);
        for (SymmetryRule r : symRules)
            r.setRuleIndex(this.ruleIndex);
        this.symmetryRules = symRules;
    }

    public HashSet<SymmetryRule> getSymmetryRules() {
        return symmetryRules;
    }

    public HashSet<FFTMove> apply(LiteralSet lSet) {
        HashSet<FFTMove> moves = new HashSet<>();
        // make quick test by bitstring comparisons
        if (!SYMMETRY_DETECTION && getBitString().compareTo(lSet.getBitString()) > 0)
            return moves;

        FFTMove m = match(this, lSet);
        if (m != null)
            moves.add(m);
        if (!SYMMETRY_DETECTION) {
            return moves;
        }

        for (SymmetryRule rule : symmetryRules) {
            m = match(rule, lSet);
            if (m != null)
                moves.add(m);
        }
        return moves;
    }

    public HashSet<FFTMove> apply(FFTNode node) {
        return apply(node.convert());
    }

    private FFTMove match(Rule rule, LiteralSet stLiterals) {
        boolean match = true;
        for (Literal l : rule.preconditions) {
            match = matchLiteral(l, stLiterals);
            if (!match)
                break;

        }
        if (match && rule.action.isLegal(stLiterals))
            return rule.action.convert();

        return null;
    }

    private boolean matchLiteral(Literal lit, LiteralSet stLiterals) {
        return stLiterals.contains(lit);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PropRule)) return false;

        PropRule propRule = (PropRule) obj;
        if (this == propRule)
            return true;
        if (SYMMETRY_DETECTION)
            return this.symmetryRules.equals(propRule.symmetryRules) && this.getRuleIndex() == propRule.getRuleIndex();
        return this.preconditions.equals(propRule.preconditions) &&
                this.action.equals(propRule.action) && this.getRuleIndex() == propRule.getRuleIndex();
    }

    @Override
    public int hashCode() {
        if (SYMMETRY_DETECTION)
            return 31 * Objects.hash(symmetryRules);
        int hash = 23;
        hash = hash * 31 + getBitString().intValue();
        hash = hash * 31 + action.hashCode();
        hash = hash * 31 + ruleIndex;
        return hash;
    }

}
