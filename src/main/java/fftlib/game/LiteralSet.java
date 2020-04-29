package fftlib.game;

import fftlib.Literal;

import java.util.HashSet;
import java.util.List;

public class LiteralSet extends HashSet<Literal> {
    long bitString;

    public LiteralSet(LiteralSet literals) {
        super(literals);
    }

    public LiteralSet(List<Literal> literals) {
        super(literals);
    }

    public LiteralSet() {
        super();
    }

    public LiteralSet(Literal l) {
        super();
        add(l);
    }

    public LiteralSet(int literalId, boolean negated) {
        super();
        add(new Literal(literalId, negated));
    }

    @Override
    public boolean add(Literal l) {
        boolean added = super.add(l);
        if (added && !l.negated)
            bitString |= (1 << l.id);
        return added;
    }

    public boolean add(String literalName) {
        return add(new Literal(literalName));
    }

    public boolean remove(Literal l) {
        boolean removed = super.remove(l);
        if (removed && !l.negated)
            bitString &= ~(1 << l.id);
        return removed;
    }

    public boolean removeAll(LiteralSet literals) {
        boolean removeAll = super.removeAll(literals);
        initializeBitString();
        return removeAll;
    }

    public long getBitString() {
        return bitString;
    }

    public void initializeBitString() {
        bitString = 0;
        for (Literal l : this) {
            if (l.negated)
                continue;
            bitString |= (1 << l.id);
        }
    }




}
