package fftlib;

import java.util.HashSet;
import java.util.Objects;

public class Literal {
    public int id;
    public boolean negated;

    public Literal(int id, boolean neg) {
        this.id = id;
        this.negated = neg;
    }

    public Literal(String name) {
        if (name.startsWith("!")) {
            this.negated = true;
            name = name.substring(1);
        }
        try {
            this.id = FFTManager.getAtomId.apply(name);
        } catch (Exception e) {
            System.err.println("Literal name: " + name + " not recognized");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Literal(Literal duplicate) {
        this.id = duplicate.id;
        this.negated = duplicate.negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    @Override
    public Literal clone() {
        return new Literal(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) return false;

        Literal literal = (Literal) obj;
        if (this == literal)
            return true;

        return this.id == literal.id && this.negated == literal.negated;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(id, negated);
    }

    public String getName() {
        return FFTManager.getAtomName.apply(id);
    }

    @Override
    public String toString() {
        return getName();
    }
}