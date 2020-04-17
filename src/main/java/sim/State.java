package sim;

import fftlib.Literal;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import static misc.Globals.*;

public class State implements FFTState {
    private Move move;
    private int turn;
    private long zobrist_key;
    LinkedList<Line> lines;

    // Reachability (experimental)
    HashSet<State> reachableParents;
    boolean reachable;

    // initial state
    public State () {
        turn = PLAYER1;
        this.lines = new LinkedList<>();
        lines.add(new Line(0, 1));
        lines.add(new Line(0, 2));
        lines.add(new Line(0, 3));
        lines.add(new Line(0, 4));
        lines.add(new Line(0, 5));

        lines.add(new Line(1, 2));
        lines.add(new Line(1, 3));
        lines.add(new Line(1, 4));
        lines.add(new Line(1, 5));

        lines.add(new Line(2, 3));
        lines.add(new Line(2, 4));
        lines.add(new Line(2, 5));

        lines.add(new Line(3, 4));
        lines.add(new Line(3, 5));

        lines.add(new Line(4, 5));
        this.zobrist_key = initZobrist();
        reachable = true; // initial state always reachable;
    }

    // next state
    public State(State parent, Move m) {
        this(parent);
        this.move = m;
        Logic.doTurn(m, this);
        reachableParents = new HashSet<>();
        updateHashCode(parent);
    }

    // Duplicate constructor, for "root" state
    public State(State state) {
        lines = new LinkedList<>();
        for (Line l : state.lines)
            lines.add(new Line(l));

        turn = state.turn;
        move = state.move;
        zobrist_key = state.zobrist_key;
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



    @Override
    public int hashCode() {
        return (int) zobrist_key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State)) return false;

        State state = (State) obj;
        return this == state ||
                this.zobrist_key == state.zobrist_key;
    }

    @Override
    public HashSet<Literal> getLiterals() {
        HashSet<Literal> literals = new HashSet<>();
        for (Line l : lines) {
            if (l.color == PLAYER1)
                literals.add(new Literal(l.n1, l.n2, PLAYER1, false));
            else if (l.color == PLAYER2)
                literals.add(new Literal(l.n1, l.n2, PLAYER2, false));
        }
        return literals;
    }

    @Override
    public HashSet<Literal> getAllLiterals() {
        HashSet<Literal> literals = new HashSet<>();
        for (Line l : lines) {
            if (l.color == PLAYER1)
                literals.add(new Literal(l.n1, l.n2, PLAYER1, false));
            else if (l.color == PLAYER2)
                literals.add(new Literal(l.n1, l.n2, PLAYER2, false));
            else
                literals.add(new Literal(l.n1, l.n2, PLAYER_ANY, true));
        }
        return literals;
    }

    @Override
    public long getZobristKey() {
        return this.zobrist_key;
    }

    private long initZobrist() {
        long hash = 0L;
        for (Line l : lines) {
            long z1 = Zobrist.points[l.n1][l.color];
            long z2 = Zobrist.points[l.n2][l.color];
            hash ^= (z1 + z2);
        }
        hash ^= Zobrist.turn[turn];
        return hash;
    }

    private void updateHashCode(State parent) {
        zobrist_key ^= Zobrist.turn[parent.getTurn()];
        zobrist_key ^= Zobrist.turn[turn];

        Line l = move.line;

        long z1 = Zobrist.points[l.n1][move.team];
        long z2 = Zobrist.points[l.n2][move.team];
        zobrist_key ^= (z1 + z2);
    }

    @Override
    public int getTurn() {
        return turn;
    }

    public void setTurn(int newTurn) {
        zobrist_key ^= Zobrist.turn[turn];
        zobrist_key ^= Zobrist.turn[newTurn];
        this.turn = newTurn;
    }

    public ArrayList<State> getChildren() {
        ArrayList<State> children = new ArrayList<>();
        for (Move m : getLegalMoves()) {
            State child = new State(this, m);
            children.add(child);
        }
        return children;
    }

    @Override
    public FFTState getNextState(FFTMove move) {
        return getNextState((Move) move);
    }

    public State getNextState(Move m) {
        return new State(this, m);
    }

    void setLine(int n1, int n2, int color) {
        for (Line l : lines) {
            if (l.n1 == n1 && l.n2 == n2)
                l.color = color;
        }
    }

    // Creates and/or returns a list of new state objects which correspond to the children of the given state.
    public ArrayList<Move> getLegalMoves() {
        return Logic.legalMoves(turn, this);
    }

    @Override
    public Move getMove() {
        return move;
    }

    @Override
    public void addReachableParent(FFTState parent) {

    }

    @Override
    public void removeReachableParent(FFTState parent) {

    }

    @Override
    public FFTState clone() {
        return new State(this);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("TURN:").append(turn).append("\t");
        for (Line l : lines)
            s.append(l.toString()).append(";\t");
        return s.toString();
    }
}
