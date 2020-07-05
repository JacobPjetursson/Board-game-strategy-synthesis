package fftlib.game;

import fftlib.FFTManager;
import fftlib.logic.LiteralSet;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class FFTNode {
    public int turn;
    public boolean reachable = true;
    public HashSet<FFTNode> reachableParents;

    // cached literalSet
    public LiteralSet converted;

    public int getTurn() {
        return turn;
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

    public boolean isReachable() {
        return reachable;
    }

    public LiteralSet convert() {
        if (converted == null)
            converted = FFTManager.nodeToLiterals.apply(this);
        return converted;
    }

    public abstract ArrayList<? extends FFTMove> getLegalMoves();

    public ArrayList<FFTNode> getChildren() {
        ArrayList<FFTNode> children = new ArrayList<>();
        for (FFTMove m : getLegalMoves()) {
            FFTNode child = getNextNode(m);
            children.add(child);
        }
        return children;
    }

    public abstract FFTNode getNextNode(FFTMove move);

    public abstract boolean isTerminal();

    /**
     * @return The winner of the game (Will return draw if isTerminal is false)
     */
    public abstract int getWinner();

    /**
     *
     * @return The move used by the parent to expand into this node
     */
    public abstract FFTMove getMove();

    public static FFTNode getInitialNode() {
        return FFTManager.initialFFTNode;
    }
}
