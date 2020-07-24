package fftlib.logic.rule;

import fftlib.game.FFTMove;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;

import java.util.HashSet;

public class GdlRule extends Rule {
    @Override
    public void setRuleIndex(int index) {

    }

    @Override
    public Rule clone() {
        return null;
    }

    @Override
    public void removePrecondition(Literal l) {

    }

    @Override
    public void addPrecondition(Literal l) {

    }

    @Override
    public void removeAction() {

    }

    @Override
    public HashSet<FFTMove> apply(LiteralSet lSet) {
        return null;
    }

    @Override
    public void setAction(Action action) {

    }

    @Override
    public void setPreconditions(LiteralSet preconditions) {

    }
}
