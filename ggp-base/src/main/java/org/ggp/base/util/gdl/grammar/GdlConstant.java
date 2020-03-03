package org.ggp.base.util.gdl.grammar;

import java.util.Objects;

@SuppressWarnings("serial")
public final class GdlConstant extends GdlTerm
{
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GdlConstant that = (GdlConstant) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public GdlConstant clone() {
        return new GdlConstant(value);
    }

    private final String value;

    GdlConstant(String value)
    {
        this.value = value.intern();
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean isGround()
    {
        return true;
    }

    @Override
    public GdlSentence toSentence()
    {
        return GdlPool.getProposition(this);
    }

    @Override
    public String toString()
    {
        return value;
    }

}
