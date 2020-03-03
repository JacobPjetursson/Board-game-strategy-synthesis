package org.ggp.base.util.gdl.grammar;

import java.util.Objects;

@SuppressWarnings("serial")
public final class GdlNot extends GdlLiteral
{

    private final GdlLiteral body;
    private transient Boolean ground;

    GdlNot(GdlLiteral body)
    {
        this.body = body;
        ground = null;
    }

    public GdlLiteral getBody()
    {
        return body;
    }

    @Override
    public boolean isGround()
    {
        if (ground == null)
        {
            ground = body.isGround();
        }

        return ground;
    }

    @Override
    public String toString()
    {
        return "( not " + body + " )";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GdlNot gdlNot = (GdlNot) o;
        return Objects.equals(body, gdlNot.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body);
    }

    @Override
    public GdlNot clone() {
        return new GdlNot(body);
    }
}
