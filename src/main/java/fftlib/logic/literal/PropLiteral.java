package fftlib.logic.literal;

import fftlib.FFTManager;

public class PropLiteral extends Literal {

    public static final int NUMBER_OF_ATOMS = FFTManager.numberOfAtoms;

    public PropLiteral(int id) {
        this.id = id;
    }

    public PropLiteral(String name) {
        try {
            this.id = FFTManager.getAtomId.apply(name);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public PropLiteral(PropLiteral duplicate) {
        this.id = duplicate.id;
    }

    @Override
    public Literal clone() {
        return new PropLiteral(this);
    }

    @Override
    public void setNegated(boolean negated) {
        if (negated && this.id <= NUMBER_OF_ATOMS) {
            this.id += NUMBER_OF_ATOMS;
        } else if (!negated && this.id > NUMBER_OF_ATOMS)
            this.id -= NUMBER_OF_ATOMS;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PropLiteral)) return false;

        PropLiteral literal = (PropLiteral) obj;
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
            return new PropLiteral(id);
        } catch (Exception ignored) {
            // return this literal if id does not exist
        }
        return new PropLiteral(this);
    }
}
