package fftlib.logic.literal;

import fftlib.logic.Action;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

public class LiteralSet extends HashSet<Literal> {
    BigInteger bitString = BigInteger.ZERO; // bitstring for all literals

    public LiteralSet(LiteralSet literals) {
        addAll(literals);
    }

    public LiteralSet(List<Literal> literals) {
        addAll(literals);
    }

    public LiteralSet() {
        super();
    }

    public LiteralSet(Literal l) {
        super();
        add(l);
    }

    public LiteralSet(int literalId) {
        super();
        add(new PropLiteral(literalId));
    }

    @Override
    public boolean add(Literal l) {
        boolean added = super.add(l);
        if (added) {
            bitString = bitString.setBit(l.id);
        }
        return added;
    }

    public boolean add(String literalName) {
        return add(new PropLiteral(literalName));
    }

    public boolean remove(Literal l) {
        boolean removed = super.remove(l);
        if (removed)
            bitString = bitString.clearBit(l.id);
        return removed;
    }

    public boolean removeAll(LiteralSet literals) {
        boolean removeAll = super.removeAll(literals);
        initializeBitString();
        return removeAll;
    }

    public LiteralSet applyAction(Action action) {
        LiteralSet set = new LiteralSet(this);
        set.addAll(action.adds);
        set.removeAll(action.rems);
        return set;
    }

    public BigInteger getBitString() {
        return bitString;
    }

    public void initializeBitString() {
        bitString = BigInteger.ZERO;

        for (Literal l : this)
            bitString = bitString.setBit(l.id);
    }


    public boolean equals(LiteralSet lSet) {
        if (this == lSet) return true;
        return bitString.equals(lSet.bitString);
    }

    public int hashCode() {
        return bitString.intValue();
    }

    public boolean contains(Literal literal) {
        return (bitString.testBit(literal.id));
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Literal l : this) {
            if (str.length() > 0)
                str.append(" ∧ ");
            str.append(l);
        }
        return str.toString();
    }
}
