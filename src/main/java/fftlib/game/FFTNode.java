package fftlib.game;

import fftlib.FFTManager;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.SymmetryRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public abstract class FFTNode {
    protected int turn;
    private boolean reachable;
    private HashSet<FFTNode> reachableParents;

    // cached literalSet
    private LiteralSet converted;

    private Rule appliedRule; // the current rule that applies to this state

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

    public void addReachableParents(Collection<FFTNode> parents) {
        for (FFTNode parent : parents) {
            addReachableParent(parent);
        }
    }

    public void removeReachableParent(FFTNode parent) {
        reachableParents.remove(parent);
        if (reachableParents.isEmpty())
            reachable = false;
    }

    public void removeReachableParents(Collection<FFTNode> parents) {
        for (FFTNode parent : parents) {
            removeReachableParent(parent);
        }
    }

    public void setReachableParents(HashSet<FFTNode> parents) {
        this.reachableParents = parents;
        reachable = !parents.isEmpty();
    }

    public Rule getAppliedRule() {
        return appliedRule;
    }

    public void setAppliedRule(Rule r) {
        if (r instanceof SymmetryRule) {
            SymmetryRule symRule = (SymmetryRule) r;
            r = symRule.getParent();
        }
        appliedRule = r;
    }

    public HashSet<FFTNode> getReachableParents() {
        if (reachableParents == null)
            reachableParents = new HashSet<>();
        return reachableParents;
    }

    public boolean isReachable() {
        return reachable;
    }

    // used for setting the initial state
    public void setReachable(boolean reachable) {
        this.reachable = reachable;
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

    public abstract FFTNode clone();
}
