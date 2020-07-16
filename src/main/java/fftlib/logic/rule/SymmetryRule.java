package fftlib.logic.rule;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.logic.LiteralSet;

import java.util.HashSet;
import java.util.Objects;

public class SymmetryRule extends Rule { // Simpler rule class for rules in symmetry hashset

    public SymmetryRule(LiteralSet precons, Action action) {
        this.preconditions = precons;
        this.action = action;
        setAllPreconditions();
    }

    public SymmetryRule(SymmetryRule duplicate) {
        this.preconditions = new LiteralSet(duplicate.preconditions);
        this.action = new Action(duplicate.action);
        setAllPreconditions();
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(preconditions, action);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SymmetryRule)) return false;

        SymmetryRule rule = (SymmetryRule) obj;
        if (this == rule)
            return true;

        return this.preconditions.equals(rule.preconditions) && this.action.equals(rule.action);
    }

    public void setRuleIndex(int index) {
        this.ruleIndex = index;
    }

    @Override
    public Rule clone() {
        return new SymmetryRule(this);
    }

    @Override
    public void removePrecondition(Literal l) {

    }

    @Override
    public void addPrecondition(Literal l) {

    }

    @Override
    public HashSet<FFTMove> apply(FFTNode n) {
        return null;
    }

    @Override
    public HashSet<FFTMove> apply(LiteralSet lSet) {
        return null;
    }

    @Override
    public void setAction(Action action) {

    }

}
