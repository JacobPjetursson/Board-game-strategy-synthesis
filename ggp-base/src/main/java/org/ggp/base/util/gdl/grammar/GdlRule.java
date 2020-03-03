package org.ggp.base.util.gdl.grammar;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public final class GdlRule extends Gdl
{

    private final ImmutableList<GdlLiteral> body;
    private transient Boolean ground;
    private final GdlSentence head;

    GdlRule(GdlSentence head, ImmutableList<GdlLiteral> body)
    {
        this.head = head;
        this.body = body;
        ground = null;
    }

    public int arity()
    {
        return body.size();
    }

    private Boolean computeGround()
    {
        for (GdlLiteral literal : body)
        {
            if (!literal.isGround())
            {
                return false;
            }
        }

        return true;
    }

    public GdlLiteral get(int index)
    {
        return body.get(index);
    }

    public GdlSentence getHead()
    {
        return head;
    }

    public List<GdlLiteral> getBody()
    {
        return body;
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

        sb.append("( <= " + head + " ");
        for (GdlLiteral literal : body)
        {
            sb.append(literal + " ");
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GdlRule gdlRule = (GdlRule) o;
        return Objects.equals(body, gdlRule.body) &&
                Objects.equals(head, gdlRule.head);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, head);
    }

    @Override
    public GdlRule clone() {
        return new GdlRule(head, body);
    }
}
