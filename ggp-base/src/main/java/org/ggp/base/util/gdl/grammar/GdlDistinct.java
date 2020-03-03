package org.ggp.base.util.gdl.grammar;

import java.util.Objects;

@SuppressWarnings("serial")
public final class GdlDistinct extends GdlLiteral
{

    private final GdlTerm arg1;
    private final GdlTerm arg2;
    private transient Boolean ground;

    GdlDistinct(GdlTerm arg1, GdlTerm arg2)
    {
        this.arg1 = arg1;
        this.arg2 = arg2;
        ground = null;
    }

    public GdlTerm getArg1()
    {
        return arg1;
    }

    public GdlTerm getArg2()
    {
        return arg2;
    }

    @Override
    public boolean isGround()
    {
        if (ground == null)
        {
            ground = arg1.isGround() && arg2.isGround();
        }

        return ground;
    }

    @Override
    public String toString()
    {
        return "( distinct " + arg1 + " " + arg2 + " )";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GdlDistinct that = (GdlDistinct) o;
        return Objects.equals(arg1, that.arg1) &&
                Objects.equals(arg2, that.arg2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arg1, arg2);
    }

    @Override
    public GdlDistinct clone() {
        return new GdlDistinct(arg1, arg2);
    }
}
