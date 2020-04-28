package fftlib.game;

import fftlib.Action;
import fftlib.Literal;

import java.util.HashSet;

// TODO - rename this to FFTState and rename FFTState to FFTNode
public class FFTNode {
    private LiteralSet literals;
    private HashSet<FFTNode> reachableParents;
    private boolean reachable;
    private long bitString; // id

    // standard constructor
    public FFTNode(LiteralSet literals) {
        this.literals = new LiteralSet(literals);
        bitString = initializeBitString();
    }

    // duplicate constructor
    public FFTNode(FFTNode dup) {
        this.literals = new LiteralSet(dup.literals);
        this.reachableParents = new HashSet<>(dup.reachableParents);
        this.bitString = dup.bitString;
        this.reachable = dup.reachable;
    }

    public FFTNode getNextNode(Action action) {
        FFTNode nextNode = new FFTNode(this);
        nextNode.literals.addAll(action.adds);
        nextNode.literals.removeAll(action.rems);
        return nextNode;

    }

    public void addReachableParent(FFTNode parent) {
        if (reachableParents == null) {
            reachableParents = new HashSet<>();
        }
        reachableParents.add(parent);
        reachable = true;
    }

    public void removeReachableParent(FFTNode parent) {
        reachableParents.remove(parent);
        if (reachableParents.isEmpty())
            reachable = false;
    }

    public HashSet<FFTNode> getReachableParents() {
        return reachableParents;
    }

    public long initializeBitString() {
        long bs = 0;
        for (Literal l : literals) {
            if (l.negated)
                continue;
            bs |= (1 << l.id);
        }
        return bs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FFTNode fftNode = (FFTNode) o;
        return bitString == fftNode.bitString;
    }

    @Override
    public int hashCode() {
        return (int) bitString;
    }
}
