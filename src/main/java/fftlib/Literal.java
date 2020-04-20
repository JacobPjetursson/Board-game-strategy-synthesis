package fftlib;

public class Literal {
    public int id;
    public boolean negation;

    public Literal(int id, boolean neg) {
        this.id = id;
        this.negation = neg;
    }

    public Literal(String name) {
        if (name.startsWith("!")) {
            this.negation = true;
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
        this.negation = duplicate.negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
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

        return this.id == literal.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    public String getName() {
        return FFTManager.getAtomName.apply(id);
    }

    @Override
    public String toString() {
        return getName();
    }
}