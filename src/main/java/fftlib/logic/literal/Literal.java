package fftlib.logic.literal;

import fftlib.FFTManager;

import java.util.ArrayList;

public abstract class Literal {
    public int id;

    public Literal() {

    }

    public boolean isNegated() {
        return getName().startsWith("!");
    }

    public abstract void setNegated(boolean negated);

    @Override
    public abstract Literal clone();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    public abstract String getName();

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

}