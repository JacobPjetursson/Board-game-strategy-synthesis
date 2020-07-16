package fftlib.logic.literal;

import fftlib.FFTManager;

import java.util.Objects;

public class PredLiteral extends Literal {
    // PredLiteral is represented by a string instead of an ID
    public String name;

    public PredLiteral(String name) {
        this.name = name;
        // TODO - check that an arbitrary grounding of the name is a legal prop literal
    }

    public PredLiteral(PredLiteral duplicate) {
        this.name = duplicate.name;
    }

    @Override
    public void setNegated(boolean negated) {
        if (negated && !name.startsWith("!"))
            name = "!" + name;
        else if (!negated && name.startsWith("!"))
            name = name.substring(1);
    }

    @Override
    public Literal clone() {
        return new PredLiteral(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PredLiteral)) return false;

        PredLiteral literal = (PredLiteral) obj;
        if (this == literal)
            return true;
        return this.name.equals(literal.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public Literal groundAll(int prop) {
        String[] comps = name.split("\\(");
        String newBody = "(" + comps[1].replace("?x", String.valueOf(prop));
        String newName = comps[0] + newBody;
        if (name.equals(newName))
            return this;
        // test if legal grounding
        String trimmedName = newName.startsWith("!") ? newName.substring(1) : newName;
        if (FFTManager.getAtomId.apply(trimmedName) == 0) // illegal grounding
            return null;
        return new PropLiteral(newName);
    }

}
