package org.ggp.base.util.gdl.grammar;

import java.util.Objects;

@SuppressWarnings("serial")
public final class GdlVariable extends GdlTerm
{

    private final String name;

    GdlVariable(String name)
    {
        this.name = name.intern();
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean isGround()
    {
        return false;
    }

    @Override
    public GdlSentence toSentence()
    {
        throw new RuntimeException("Unable to convert a GdlVariable to a GdlSentence!");
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GdlVariable that = (GdlVariable) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public GdlVariable clone() {
        return new GdlVariable(name);
    }
}
