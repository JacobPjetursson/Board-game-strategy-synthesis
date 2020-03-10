package sim;

import fftlib.Literal;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import sim.ai.Zobrist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import static misc.Globals.PLAYER1;

public class State implements FFTState {
    private Move move;
    private int turn;
    private ArrayList<Move> legalMoves;
    private long zobrist_key;

    LinkedList<Line> lines;

    // initial state
    public State () {
        turn = PLAYER1;
        this.lines = new LinkedList<>();
        this.zobrist_key = initZobrist();
    }

    // next state
    public State(State parent, Move m) {
        this(parent);
        this.move = m;
        Logic.doTurn(m, this);
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
        return null;
    }

    @Override
    public HashSet<Literal> getAllLiterals() {
        return null;
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

    // Creates and/or returns a list of new state objects which correspond to the children of the given state.
    public ArrayList<Move> getLegalMoves() {
        if (legalMoves != null) return legalMoves;
        legalMoves = Logic.legalMoves(turn, this);
        return legalMoves;
    }

    @Override
    public Move getMove() {
        return move;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("TURN:").append(turn).append("\t");
        for (Line l : lines)
            s.append(l.toString()).append(";\t");
        return s.toString();
    }
}
