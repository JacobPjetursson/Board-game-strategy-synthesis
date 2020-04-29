package fftlib.game;

import fftlib.Action;
import fftlib.FFTManager;
import fftlib.Literal;

import java.util.HashSet;

public class State {
    private LiteralSet literals;
    private HashSet<State> reachableParents;
    private boolean reachable = true;

    // standard constructor
    public State(LiteralSet literals) {
        this.literals = new LiteralSet(literals);
    }

    // duplicate constructor
    public State(State dup) {
        this.literals = new LiteralSet(dup.literals);
    }

    // empty constructor
    public State() {
        this.literals = new LiteralSet();
    }

    public State getNextState(Action action) {
        State nextState = new State(this);
        nextState.literals.addAll(action.adds);
        nextState.literals.removeAll(action.rems);
        return nextState;
    }

    public void addReachableParent(State parent) {
        if (reachableParents == null) {
            reachableParents = new HashSet<>();
        }
        reachableParents.add(parent);
        reachable = true;
    }

    public void removeReachableParent(State parent) {
        reachableParents.remove(parent);
        if (reachableParents.isEmpty())
            reachable = false;
    }

    public HashSet<State> getReachableParents() {
        return reachableParents;
    }

    public boolean isReachable() {
        return reachable;
    }

    public long getBitString() {
        return literals.getBitString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return getBitString() == state.getBitString();
    }

    @Override
    public int hashCode() {
        return (int) getBitString();
    }

    public LiteralSet getLiterals() {
        return literals;
    }

    // Used for making rules
    public LiteralSet getAllLiterals() {
        LiteralSet allLiterals = new LiteralSet(literals);
        for (int id : FFTManager.getGameAtoms.get()) {
            Literal l = new Literal(id, false);
            if (!allLiterals.contains(l)) {
                l.setNegated(true);
                allLiterals.add(l);
            }
        }
        return allLiterals;
    }

    public String toString() {
        return literals.toString();
    }
}
