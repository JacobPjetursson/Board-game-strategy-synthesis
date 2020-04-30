package fftlib.logic;

import fftlib.FFTManager;

public class Literal {
    public int id;
    public boolean negated;

    // used for cartesian product to allow the option of not picking any literal
    public static Literal NULL = new Literal(0, false);

    public Literal(int id) {
        this.id = id;
        this.negated = false;
    }

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
        if (negated)
            return -id;
        return id;
    }

    public String getName() {
        String negPfx = negated ? "!" : "";
        return negPfx + FFTManager.getAtomName.apply(id);
    }

    @Override
    public String toString() {
        return getName();
    }
}