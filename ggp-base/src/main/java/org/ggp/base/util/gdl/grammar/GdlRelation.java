package org.ggp.base.util.gdl.grammar;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public final class GdlRelation extends GdlSentence
{

    private final ImmutableList<GdlTerm> body;
    private transient Boolean ground;
    private final GdlConstant name;

    GdlRelation(GdlConstant name, ImmutableList<GdlTerm> body)
    {
        this.name = name;
        this.body = body;
        ground = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GdlRelation that = (GdlRelation) o;
        return Objects.equals(body, that.body) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, name);
    }

    @Override
    public int arity()
    {
        return body.size();
    }

    private boolean computeGround()
    {
        for (GdlTerm term : body)
        {
            if (!term.isGround())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public GdlTerm get(int index)
    {
        return body.get(index);
    }

    @Override
    public GdlConstant getName()
    {
        return name;
    }

    @Override
    public boolean isGround()
    {
        if (ground == null)
        {
            ground = computeGround();
        }

        return ground;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("( " + name + " ");
        for (GdlTerm term : body)
        {
            sb.append(term + " ");
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public GdlTerm toTerm()
    {
        return GdlPool.getFunction(name, body);
    }

    @Override
    public List<GdlTerm> getBody()
    {
        return body;
    }

    @Override
    public GdlRelation clone() {
        return new GdlRelation(name, body);
    }
}
