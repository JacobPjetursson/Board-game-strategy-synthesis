package fftlib.logic;

import fftlib.FFTManager;

import java.util.ArrayList;

public class Literal {
    public int id;

    // used for cartesian product to allow the option of not picking any literal
    public static Literal NULL = new Literal(0);

    public Literal() {

    }

    public Literal(int id) {
        this.id = id;
    }

    public boolean isNegated() {
        return getName().startsWith("!");
    }

    public void setNegated(boolean negated) {
        String name = getName();
        if (negated && !name.startsWith("!")) {
            this.id = FFTManager.getAtomId.apply("!" + name);
        } else if (name.startsWith("!")){
            this. id = FFTManager.getAtomId.apply(name.substring(1));
        }
    }

    public Literal(String name) {
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
        return id;
    }

    public String getName() {
        return FFTManager.getAtomName.apply(id);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getBody() {
        String name = getName();
        return name.substring(name.lastIndexOf("("));
    }

    // TODO - should scale to values > 9
    public ArrayList<Integer> getIndices() {
        ArrayList<Integer> indices = new ArrayList<>();
        String body = getBody();
        for (char c : body.toCharArray()) {
            if (Character.isDigit(c))
                indices.add(Character.getNumericValue(c));
        }
        return indices;
    }

    public Literal liftAll(int prop) {
        String name = getName();
        String[] comps = name.split("\\(");
        String newBody = "(" + comps[1].replace(String.valueOf(prop), "?x");
        String newName = comps[0] + newBody;
        if (name.equals(newName))
            return this;
        return new PredLiteral(newName);
    }

    public Literal replaceAll(int oldIdx, int newIdx) {
        String oldIdxStr = String.valueOf(oldIdx);
        String newIdxStr = String.valueOf(newIdx);
        String[] comps = getName().split("\\(");
        String newBody = "(" + comps[1].replace(oldIdxStr, newIdxStr);
        String newName = comps[0] + newBody;
        try {
            int id = FFTManager.getAtomId.apply(newName);
            return new Literal(id);
        } catch (Exception ignored) {
            // return this literal if id does not exist
        }
        return new Literal(this);
    }

}