package fftlib.logic;

import fftlib.game.LiteralSet;
import fftlib.logic.Action;
import fftlib.logic.Rule;

import java.util.Objects;

public class SymmetryRule extends Rule { // Simpler rule class for rules in symmetry hashset

    public SymmetryRule(LiteralSet precons, Action action) {
        this.preconditions = precons;
        this.action = action;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(preconditions, action);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rule)) return false;

        Rule rule = (Rule) obj;
        if (this == rule)
            return true;

        return this.preconditions.equals(rule.preconditions) && this.action.equals(rule.action);
    }

}
