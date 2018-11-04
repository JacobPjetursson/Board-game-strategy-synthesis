package ai.Minimax;

import game.Logic;
import game.Move;
import game.State;

import java.io.Serializable;
import java.util.ArrayList;

import static misc.Config.BLACK;
import static misc.Config.RED;

public class Node implements Serializable {
    private State state;
    private long zobrist_key;

    // Starting Root state
    public Node(State startState)  {
        this.state = new State(startState);
        this.zobrist_key = initHashCode();
    }

    // Non-root state
    private Node(Node parent, Move m) {
        this.state = new State(parent.state);
        zobrist_key = parent.zobrist_key;

        this.state.setMove(m);
        Logic.doTurn(m, this.state);
        updateHashCode(parent.state);
    }

    // Duplicate constructor, for "root" state
    public Node(Node node) {
        this.state = new State(node.state);
        zobrist_key = node.zobrist_key;
        this.state.setMove(node.state.getMove());
    }

    public Node getNextNode(Move m) {
        return new Node(this, m);
    }

    public ArrayList<Node> getChildren() {
        ArrayList<Node> children = new ArrayList<>();
        for (Move m : state.getLegalMoves()) {
            Node child = new Node(this, m);
            children.add(child);
        }
        return children;
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;
        return (((Node) obj).getHashCode() == getHashCode());
    }

    @Override
    public int hashCode() {
        return (int) zobrist_key;
    }

    public long getHashCode() {
        return this.zobrist_key;
    }


    private long initHashCode() {
        long hash = 0L;
        int[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0) {
                    int k = board[i][j]; // team occupying spot
                    hash = hash ^ Zobrist.board[i][j][k];
                }
            }
        }
        hash = hash ^ Zobrist.turn[state.getTurn()];
        hash = hash ^ Zobrist.redPoints[state.getScore(RED)];
        hash = hash ^ Zobrist.blackPoints[state.getScore(BLACK)];
        return hash;
    }

    private void updateHashCode(State parent) {
        int[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != parent.getBoard()[i][j]) {
                    int k_parent = parent.getBoard()[i][j]; // team occupying spot
                    int k = board[i][j];
                    if (k_parent != 0) zobrist_key ^= Zobrist.board[i][j][k_parent];
                    if (k != 0) zobrist_key ^= Zobrist.board[i][j][k];
                }
            }
        }
        if (state.getTurn() != parent.getTurn()) {
            zobrist_key ^= Zobrist.turn[parent.getTurn()];
            zobrist_key ^= Zobrist.turn[state.getTurn()];
        }
        if (state.getScore(RED) != parent.getScore(RED)) {
            zobrist_key ^= Zobrist.redPoints[parent.getScore(RED)];
            zobrist_key ^= Zobrist.redPoints[state.getScore(RED)];
        }
        if (state.getScore(BLACK) != parent.getScore(BLACK)) {
            zobrist_key ^= Zobrist.blackPoints[parent.getScore(BLACK)];
            zobrist_key ^= Zobrist.blackPoints[state.getScore(BLACK)];
        }
    }
}
