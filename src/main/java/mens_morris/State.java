package mens_morris;

import fftlib.Literal;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static mens_morris.Logic.POS_NONBOARD;
import static misc.Config.THREE_MENS;
import static misc.Globals.*;

public class State implements FFTState {
    private Move move;
    private int turn;
    private long zobrist_key;
    int[][] board;
    boolean canRemove; // can the player remove opponent piece?
    boolean phase2; // did phase 2 start?
    int unplaced; // need to place 12 pieces for phase2 (half for each player)

    // varies depending on game type
    private static final int MEN = (THREE_MENS) ? 3*2 : 6*2;
    public static final int BOARD_SIZE = (THREE_MENS) ? 3 : 5;

    // initial state
    public State () {
        turn = PLAYER1;
        board = initBoard();
        phase2 = false;
        canRemove = false;
        // each player starts with specific amount of men
        unplaced = MEN;
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
        board = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < state.board.length; i++) {
            board[i] = Arrays.copyOf(state.board[i], state.board[i].length);
        }
        turn = state.turn;
        move = state.move;
        this.phase2 = state.phase2;
        this.canRemove = state.canRemove;
        unplaced = state.unplaced;
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
    public HashSet<Literal> getLiterals() { // TODO
        HashSet<Literal> literals = new HashSet<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int pieceOcc = board[i][j];
                if (pieceOcc > 0) {
                    if (turn != PLAYER1) {
                        pieceOcc = (pieceOcc == 1) ? 2 : 1;
                    }
                    //literals.add(new Literal(i, j, pieceOcc, false));
                } else if (pieceOcc == 0) {
                    //literals.add(new Literal(i, j, PLAYER_ANY, true));
                }
            }
        }

        String phaseStr = (phase2) ? "" : "!";
        String removeStr = (canRemove) ? "" : "!";
        if (!THREE_MENS) literals.add(new Literal(phaseStr + "phase2"));
        if (!THREE_MENS) literals.add(new Literal(removeStr + "canRemove"));
        return literals;
    }

    @Override
    public long getZobristKey() {
        return this.zobrist_key;
    }

    private long initZobrist() {
        long hash = 0L;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0) {
                    int k = board[i][j]; // team occupying spot
                    if (k != POS_NONBOARD)
                        hash ^= Zobrist.board[i][j][k];
                }
            }
        }
        hash ^= Zobrist.turn[turn];
        return hash;
    }

    private void updateHashCode(State parent) {
        zobrist_key ^= Zobrist.turn[parent.getTurn()];
        zobrist_key ^= Zobrist.turn[turn];

        if (move.newRow != POS_NONBOARD) {
            int parent_occ = parent.board[move.newRow][move.newCol];
            zobrist_key ^= Zobrist.board[move.newRow][move.newCol][parent_occ];
            int this_occ = board[move.newRow][move.newCol];
            zobrist_key ^= Zobrist.board[move.newRow][move.newCol][this_occ];
        }

        // move piece
        if (move.oldCol != POS_NONBOARD) {
            int parent_occ = parent.board[move.oldRow][move.oldCol];
            zobrist_key ^= Zobrist.board[move.oldRow][move.oldCol][parent_occ];
            int this_occ = board[move.oldRow][move.oldCol];
            zobrist_key ^= Zobrist.board[move.oldRow][move.oldCol][this_occ];
        }

        if (parent.canRemove != this.canRemove)
            zobrist_key ^= Zobrist.canRemove;
        if (parent.phase2 != this.phase2)
            zobrist_key ^= Zobrist.phase2;

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
        return Logic.legalMoves(turn, this);
    }

    public int[][] initBoard() {
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
        if (THREE_MENS)
            return board;
        // all empty spots
        board[1][0] = -1;
        board[3][0] = -1;
        board[0][1] = -1;
        board[4][1] = -1;
        board[2][2] = -1;

        board[0][3] = -1;
        board[4][3] = -1;
        board[1][4] = -1;
        board[3][4] = -1;
        return board;
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
    public HashSet<? extends FFTState> getReachableParents() {
        return null;
    }

    @Override
    public boolean isReachable() {
        return false;
    }

    public void changeTurn() {
        if (turn == PLAYER1)
            turn = PLAYER2;
        else
            turn = PLAYER1;
    }

    public void setBoardEntry(int row, int col, int team) {
        board[row][col] = team;
    }

    @Override
    public FFTState clone() {
        return new State(this);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        String phaseStr = (phase2) ? "2" : "1";
        s.append("TURN: ").append(turn).append(", PHASE: ").append(phaseStr).append("\n");
        for (int[] ints : board) {
            for (int anInt : ints) {
                s.append(anInt).append("\t");
            }
            s.append("\n");
        }
        return s.toString();
    }
}
