package tictactoe.game;

import fftlib.Literal;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import tictactoe.ai.Zobrist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER_ANY;


public class State implements FFTState {
    private int[][] board;
    private int turn;
    private Move move;
    private long zobrist_key;

    // save results if already computed once (e.g. several verifications)
    private HashSet<Literal> literals;

    // Reachability
    HashSet<State> reachableParents;
    boolean reachable;

    // Starting state
    public State() {
        board = new int[3][3];
        turn = PLAYER1;
        zobrist_key = initHashCode();
        // TODO - put in separate class or something to avoid clutter in lookupTable (where this is absolutely not needed)
        reachable = true; // initial state always reachable;
    }

    // Duplicate constructor
    public State(State state) {
        board = new int[state.board.length][];
        for (int i = 0; i < state.board.length; i++) {
            board[i] = Arrays.copyOf(state.board[i], state.board[i].length);
        }
        turn = state.turn;
        move = state.move;
        this.zobrist_key = state.zobrist_key;

    }

    // Non-root state
    private State(State parent, Move m) {
        this(parent);
        Logic.doTurn(m, this);
        this.move = m;
        reachableParents = new HashSet<>();
        updateHashCode(parent);
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

    private void updateHashCode(State parent) {
        zobrist_key ^= Zobrist.turn[parent.getTurn()];
        zobrist_key ^= Zobrist.turn[turn];

        int k_parent = parent.board[move.row][move.col];
        int k = board[move.row][move.col];
        zobrist_key ^= Zobrist.board[move.row][move.col][k_parent];
        zobrist_key ^= Zobrist.board[move.row][move.col][k];
    }

    @Override
    public State getNextState(FFTMove move) {
        return getNextState((Move) move);
    }

    public State getNextState(Move m) {
        return new State(this, m);
    }

    public ArrayList<State> getChildren() {
        ArrayList<State> children = new ArrayList<>();
        for (Move m : getLegalMoves()) {
            State child = new State(this, m);
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

    @Override
    public void addReachableParent(FFTState parent) {
        addReachableParent((State)parent);
    }

    @Override
    public void removeReachableParent(FFTState parent) {
        removeReachableParent((State) parent);
    }

    @Override
    public FFTState clone() {
        return new State(this);
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public HashSet<Literal> getLiterals() {
        if (literals != null)
            return literals;
        HashSet<Literal> literals = new HashSet<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int pieceOcc = board[i][j];
                if (pieceOcc > 0) {
                    if (turn == PLAYER1)
                        literals.add(new Literal(i, j, pieceOcc, false));
                    else {
                        pieceOcc = (pieceOcc == 1) ? 2 : 1;
                        literals.add(new Literal(i, j, pieceOcc, false));
                    }
                }
            }
        }
        this.literals = literals;
        return literals;
    }

    public HashSet<Literal> getAllLiterals() { // Including negatives, used for creating rules
        HashSet<Literal> literals = new HashSet<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int pieceOcc = board[i][j];
                if (pieceOcc > 0) {
                    if (turn == PLAYER1)
                        literals.add(new Literal(i, j, pieceOcc, false));
                    else {
                        pieceOcc = (pieceOcc == 1) ? 2 : 1;
                        literals.add(new Literal(i, j, pieceOcc, false));
                    }
                } else
                    literals.add(new Literal(i, j, PLAYER_ANY, true));
            }
        }
        return literals;
    }

    @Override
    public long getZobristKey() {
        return zobrist_key;
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
        if (!(obj instanceof State)) return false;

        State state = (State) obj;
        return this == state ||
                this.zobrist_key == state.zobrist_key;
    }

    @Override
    public int hashCode() {
        return (int) zobrist_key;
    }
}
