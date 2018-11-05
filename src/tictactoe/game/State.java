package tictactoe.game;

import fftlib.Clause;
import fftlib.game.FFTState;
import misc.Config;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import static misc.Config.PLAYER1;

public class State implements FFTState {
    private int[][] board;
    private int turn;
    private ArrayList<Move> legalMoves;
    private Move move;

    // Starting state
    public State() {
        int rows = Config.bHeight;
        int columns = Config.bWidth;
        board = new int[rows][columns];
        turn = PLAYER1;
    }

    // Duplicate constructor
    public State(State state) {
        board = new int[state.board.length][];
        for (int i = 0; i < state.board.length; i++) {
            board[i] = Arrays.copyOf(state.board[i], state.board[i].length);
        }
        turn = state.turn;
        move = state.move;
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

    public HashSet<Clause> getClauses() {
        HashSet<Clause> clauses = new HashSet<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int pieceOcc = board[i][j];
                if (pieceOcc > 0) {
                    if (turn == PLAYER1)
                        clauses.add(new Clause(i, j, pieceOcc, false));
                    else {
                        pieceOcc = (pieceOcc == 1) ? 2 : 1;
                        clauses.add(new Clause(i, j, pieceOcc, false));
                    }
                }
            }
        }
        return clauses;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    // Get a list of pieces/points from this state
    public ArrayList<Point> getPieces(int team) {
        ArrayList<Point> entries = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == team) {
                    entries.add(new Point(j, i));
                }
            }
        }
        return entries;
    }

    // Get the next state based on the input move
    public State getNextState(Move m) {
        State state = new State(this);
        Logic.doTurn(m, state);
        state.move = m;
        return state;
    }

    public ArrayList<State> getChildren() {
        ArrayList<State> children = new ArrayList<>();
        for (Move m : getLegalMoves()) {
            State child = new State(this).getNextState(m);
            children.add(child);
        }
        return children;
    }

    // Creates and/or returns a list of new state objects which correspond to the children of the given state.
    public ArrayList<Move> getLegalMoves() {
        if (legalMoves != null) return legalMoves;
        legalMoves = Logic.legalMoves(turn, this);
        return legalMoves;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State)) return false;
        State state = (State) obj;
        return this == state || turn == state.getTurn() &&
                (Arrays.deepEquals(board, state.board));
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(turn);
        result += Arrays.deepHashCode(board);
        //result += Arrays.deepHashCode(this.reflect().board);
        return result;

    }

    public String print() {
        String boardStr = "";
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                boardStr += board[i][j] + " ";
            }
            boardStr += "\n";
        }
        return boardStr;
    }
/*
    private State reflect() {
        State copy = new State(this);
        for (int i = 0; i < copy.board.length; i++) {
            for (int j = 0; j < copy.board[i].length; j++) {
                copy.board[i][j] = board[i][board[i].length - 1 - j];
            }
        }
        return copy;
    }
*/

}
