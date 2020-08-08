package fftlib.game;

import fftlib.FFTManager;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.SymmetryRule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static misc.Config.SINGLE_THREAD;

public abstract class FFTNode {
    protected int turn;
    private boolean reachable;
    private Set<FFTNode> reachableParents;

    // cached literalSet
    private LiteralSet converted;

    private RuleMapping appliedRuleMapping; // the current rule that applies to this state

    public int getTurn() {
        return turn;
    }

    public synchronized void addReachableParent(FFTNode parent) {
        if (reachableParents == null) {
            if (SINGLE_THREAD)
                reachableParents = new HashSet<>();
            else
                reachableParents = ConcurrentHashMap.newKeySet();
        }
        reachableParents.add(parent);
        reachable = true;
    }

    public synchronized void addReachableParents(Collection<FFTNode> parents) {
        for (FFTNode parent : parents) {
            addReachableParent(parent);
        }
    }

    public synchronized void removeReachableParent(FFTNode parent) {
        reachableParents.remove(parent);
        if (reachableParents.isEmpty())
            reachable = false;
    }

    public synchronized void removeReachableParents(Collection<FFTNode> parents) {
        for (FFTNode parent : parents) {
            removeReachableParent(parent);
        }
    }

    public synchronized void clearReachableParents() {
        reachableParents.clear();
        reachable = false;
    }

    public synchronized Set<FFTNode> getReachableParents() {
        if (reachableParents == null)
            if (SINGLE_THREAD)
                reachableParents = new HashSet<>();
            else
                reachableParents = ConcurrentHashMap.newKeySet();
        return reachableParents;
    }

    public synchronized boolean isReachable() {
        return reachable;
    }

    // below functions are used to lock certain blocks
    public synchronized void addReachableParent(FFTNode parent, Map<FFTNode, Set<FFTNode>> addedParentNodes) {
        if (reachableParents == null) {
            if (SINGLE_THREAD)
                reachableParents = new HashSet<>();
            else
                reachableParents = ConcurrentHashMap.newKeySet();
        }

        if (!reachableParents.contains(parent)) {
            reachableParents.add(parent);
            reachable = true;
            Set<FFTNode> addParents;
            if (SINGLE_THREAD)
                addParents = addedParentNodes.getOrDefault(this, new HashSet<>());
            else
                addParents = addedParentNodes.getOrDefault(this, ConcurrentHashMap.newKeySet());
            addParents.add(parent);
            addedParentNodes.put(this, addParents);
        }
    }

    public synchronized void removeReachableParent(FFTNode parent, Map<FFTNode, Set<FFTNode>> deletedParentNodes) {
        if (reachableParents.contains(parent)) {
            reachableParents.remove(parent);
            if (reachableParents.isEmpty())
                reachable = false;
            Set<FFTNode> deletedParents;
            if (SINGLE_THREAD)
                deletedParents = deletedParentNodes.getOrDefault(this, new HashSet<>());
            else
                deletedParents = deletedParentNodes.getOrDefault(this, ConcurrentHashMap.newKeySet());
            deletedParents.add(parent);
            deletedParentNodes.put(this, deletedParents);
        }
    }

    public synchronized void clearReachableParents(Map<FFTNode, Set<FFTNode>> deletedParentNodes) {
        Set<FFTNode> deletedParents;
        if (SINGLE_THREAD)
            deletedParents = deletedParentNodes.getOrDefault(this, new HashSet<>());
        else
            deletedParents = deletedParentNodes.getOrDefault(this, ConcurrentHashMap.newKeySet());
        deletedParents.addAll(reachableParents);
        deletedParentNodes.put(this, deletedParents);
        reachableParents.clear();
        reachable = false;
    }

    public synchronized void addToAppliedMap(Map<FFTNode, RuleMapping> map, Rule r, FFTMove m) {
        RuleMapping existing = map.get(this);
        if (existing != null) {// in case of symmetry
            existing.getMoves().add(m);
        }
        else {
            Set<FFTMove> moves = new HashSet<>();
            moves.add(m);
            map.put(this, new RuleMapping(r, moves));
        }
    }

    public RuleMapping getAppliedRuleMapping() {
        return appliedRuleMapping;
    }

    public void setAppliedRuleMapping(RuleMapping rm) {
        appliedRuleMapping = rm;
    }

    // used for setting the initial state
    public synchronized void setReachable(boolean reachable) {
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
        return FFTManager.getInitialNode();
    }

    public abstract FFTNode clone();
}
