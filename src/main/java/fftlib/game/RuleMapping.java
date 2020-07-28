package fftlib.game;


import fftlib.logic.rule.Rule;
import fftlib.logic.rule.SymmetryRule;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class RuleMapping {
    private Set<FFTMove> moves;
    private Rule rule;

    public static RuleMapping NOMATCH = new RuleMapping();

    public RuleMapping() {
        this.moves = new HashSet<>();
    }

    public RuleMapping(Rule rule, Set<FFTMove> moves) {
        if (rule instanceof SymmetryRule) {
            SymmetryRule r = (SymmetryRule) rule;
            this.rule = r.getParent();
        } else {
            this.rule = rule;
        }
        this.moves = moves;
    }

    public RuleMapping(Rule rule, FFTNode node) {
        this(rule, rule.apply(node));
    }

    public RuleMapping(RuleMapping rm) {
        this.moves = new HashSet<>();
        this.moves.addAll(rm.moves);
        this.rule = rm.rule.clone();
    }

    public RuleMapping clone() {
        return new RuleMapping(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleMapping that = (RuleMapping) o;
        return this.moves.equals(that.moves) &&
                this.rule.equals(that.rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moves, rule);
    }

    public Rule getRule() {
        return rule;
    }

    public Set<FFTMove> getMoves() {
        return moves;
    }

    public String toString() {
        return moves + " , RULE: " + rule;
    }
}
