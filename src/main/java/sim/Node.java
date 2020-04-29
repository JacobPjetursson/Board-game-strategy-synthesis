package sim;

import fftlib.Literal;
import fftlib.auxiliary.Position;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import static misc.Globals.*;

public class Node extends FFTNode {
    private Move move;
    private int turn;
    private long zobrist_key;
    LinkedList<Line> lines;

    // Reachability
    HashSet<Node> reachableParents;
    boolean reachable;

    // initial state
    public Node() {
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
    public Node(Node parent, Move m) {
        this(parent);
        this.move = m;
        Logic.doTurn(m, this);
        reachableParents = new HashSet<>();
        updateHashCode(parent);
    }

    // Duplicate constructor, for "root" node
    public Node(Node node) {
        lines = new LinkedList<>();
        for (Line l : node.lines)
            lines.add(new Line(l));

        turn = node.turn;
        move = node.move;
        zobrist_key = node.zobrist_key;
    }

    @Override
    public int hashCode() {
        return (int) zobrist_key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;

        Node node = (Node) obj;
        return this == node ||
                this.zobrist_key == node.zobrist_key;
    }

/*
    public LiteralSet getLiterals() {
        LiteralSet literals = new LiteralSet();
        for (Line l : lines) {
            int occ = l.color;
            Position pos = new Position(l.n1, l.n2, occ);
            literals.add(new Literal(Atoms.posToId.get(pos), false));
        }
        return literals;
    }

 */

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

    private void updateHashCode(Node parent) {
        zobrist_key ^= Zobrist.turn[parent.getTurn()];
        zobrist_key ^= Zobrist.turn[turn];

        Line l = move.line;

        long z1 = Zobrist.points[l.n1][move.team];
        long z2 = Zobrist.points[l.n2][move.team];
        zobrist_key ^= (z1 + z2);
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int newTurn) {
        zobrist_key ^= Zobrist.turn[turn];
        zobrist_key ^= Zobrist.turn[newTurn];
        this.turn = newTurn;
    }

    public ArrayList<Node> getChildren() {
        ArrayList<Node> children = new ArrayList<>();
        for (Move m : getLegalMoves()) {
            Node child = new Node(this, m);
            children.add(child);
        }
        return children;
    }

    @Override
    public FFTNode getNextNode(FFTMove move) {
        return getNextState((Move) move);
    }

    public Node getNextState(Move m) {
        return new Node(this, m);
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
