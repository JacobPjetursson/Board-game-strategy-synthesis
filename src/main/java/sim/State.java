package sim;

import fftlib.Literal;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import sim.ai.Zobrist;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static misc.Globals.PLAYER1;

public class State implements FFTState {
    private Move move;
    private int turn;
    private ArrayList<Move> legalMoves;
    private long zobrist_key;

    ArrayList<Line> lines;

    // initial state
    public State () {
        turn = PLAYER1;


        Point p1 = new Point(0, 0);
        Point p2 = new Point(1,0);
        Point p3 = new Point(0, 1);
        Point p4 = new Point(1, 1);
        Point p5 = new Point(0, 2);
        Point p6 = new Point(1, 2);

        lines = new ArrayList<>();

        lines.add(new Line(p1, p2, 1));
        lines.add(new Line(p1, p3, 2));
        lines.add(new Line(p1, p4, 3));
        lines.add(new Line(p1, p5, 4));
        lines.add(new Line(p1, p6, 5));

        lines.add(new Line(p2, p3, 6));
        lines.add(new Line(p2, p4, 7));
        lines.add(new Line(p2, p5, 8));
        lines.add(new Line(p2, p6, 9));

        lines.add(new Line(p3, p4, 9));
        lines.add(new Line(p3, p5, 10));
        lines.add(new Line(p3, p6, 11));

        lines.add(new Line(p4, p5, 12));
        lines.add(new Line(p4, p6, 13));

        lines.add(new Line(p5, p6, 14));
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
        lines = new ArrayList<>();
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
            long z1 = Zobrist.points[l.p1.y][l.p1.x][l.color];
            long z2 = Zobrist.points[l.p2.y][l.p2.x][l.color];
            hash ^= (z1 + z2);
        }
        hash = hash ^ Zobrist.turn[turn];
        return hash;
    }

    private void updateHashCode(State parent) {
        zobrist_key ^= Zobrist.turn[parent.getTurn()];
        zobrist_key ^= Zobrist.turn[turn];

        Line l = move.line;
        long z1_parent = Zobrist.points[l.p1.y][l.p1.x][Line.NO_COLOR];
        long z2_parent = Zobrist.points[l.p2.y][l.p2.x][Line.NO_COLOR];
        long z1 = Zobrist.points[l.p1.y][l.p1.x][move.team];
        long z2 = Zobrist.points[l.p2.y][l.p2.x][move.team];
        zobrist_key ^= (z1_parent + z2_parent);
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
        s.append("TURN: ").append(turn).append("\t");
        for (Line l : lines)
            s.append(l.toString()).append(";\t");
        return s.toString();
    }
}
