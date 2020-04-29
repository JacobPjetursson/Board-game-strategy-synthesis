package tictactoe.game;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import tictactoe.ai.Zobrist;

import java.util.ArrayList;
import java.util.Arrays;

import static misc.Globals.PLAYER1;


public class Node extends FFTNode {
    private int[][] board;
    private Move move;
    private long zobrist_key;

    // Starting state
    public Node() {
        board = new int[3][3];
        turn = PLAYER1;
        zobrist_key = initHashCode();
    }

    // Duplicate constructor
    public Node(Node node) {
        board = new int[node.board.length][];
        for (int i = 0; i < node.board.length; i++) {
            board[i] = Arrays.copyOf(node.board[i], node.board[i].length);
        }
        turn = node.turn;
        move = node.move;
        this.zobrist_key = node.zobrist_key;
    }

    // Constructor for ease of debugging
    public Node(int[][] board, int turn) {
        this.board = board;
        this.turn = turn;
        this.zobrist_key = initHashCode();
    }

    // Non-root state
    private Node(Node parent, Move m) {
        this(parent);
        Logic.doTurn(m, this);
        this.move = m;
        updateHashCode(parent);
    }

    private long initHashCode() {
        long hash = 0L;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0) {
                    int k = board[i][j]; // team occupying spot
                    hash = hash ^ Zobrist.board[i][j][k];
                }
            }
        }
        hash = hash ^ Zobrist.turn[turn];
        return hash;
    }

    private void updateHashCode(Node parent) {
        zobrist_key ^= Zobrist.turn[parent.getTurn()];
        zobrist_key ^= Zobrist.turn[turn];

        int k_parent = parent.board[move.row][move.col];
        int k = board[move.row][move.col];
        zobrist_key ^= Zobrist.board[move.row][move.col][k_parent];
        zobrist_key ^= Zobrist.board[move.row][move.col][k];
    }

    @Override
    public Node getNextNode(FFTMove move) {
        return getNextState((Move) move);
    }

    public Node getNextState(Move m) {
        return new Node(this, m);
    }

    public ArrayList<Node> getChildren() {
        ArrayList<Node> children = new ArrayList<>();
        for (Move m : getLegalMoves()) {
            Node child = new Node(this, m);
            children.add(child);
        }
        return children;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoardEntry(int row, int col, int team) {
        board[row][col] = team;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public String toString() {
        return Arrays.deepToString(board) + " , TEAM: " + turn;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    // Creates and/or returns a list of new state objects which correspond to the children of the given state.
    public ArrayList<Move> getLegalMoves() {
        return Logic.legalMoves(turn, this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;

        Node node = (Node) obj;
        return this == node ||
                this.zobrist_key == node.zobrist_key;
    }

    @Override
    public int hashCode() {
        return (int) zobrist_key;
    }
}
