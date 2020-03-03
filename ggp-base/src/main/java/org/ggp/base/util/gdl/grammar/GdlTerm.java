package org.ggp.base.util.gdl.grammar;

@SuppressWarnings("serial")
public abstract class GdlTerm extends Gdl
{

    @Override
    public abstract boolean isGround();

    public abstract GdlSentence toSentence();

    @Override
    public abstract String toString();

    public abstract int hashCode();

    public abstract boolean equals(Object obj);

}
