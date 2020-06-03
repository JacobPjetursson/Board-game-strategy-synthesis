package fftlib.logic;

import fftlib.FFTManager;

import java.util.Objects;

// TODO - make super class for PredLiteral and Literal to extend from
public class PredLiteral extends Literal{
    // PredLiteral is represented by a string instead of an ID
    public String name;
    public boolean negated;

    public PredLiteral(String name) {
        if (name.startsWith("!")) {
            this.negated = true;
        }
        this.name = name;
        // TODO - check that an arbitrary grounding of the name is a legal prop literal
    }

    public PredLiteral(PredLiteral duplicate) {
        this.name = duplicate.name;
        this.negated = duplicate.negated;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PredLiteral)) return false;

        PredLiteral literal = (PredLiteral) obj;
        if (this == literal)
            return true;
        System.out.println(this.name);
        return this.name.equals(literal.name) && this.negated == literal.negated;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
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
        return new Literal(newName);
    }

}
