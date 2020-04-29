package mens_morris;

import fftlib.Literal;
import fftlib.auxiliary.Position;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.game.LiteralSet;
import tictactoe.FFT.Atoms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static mens_morris.Logic.POS_NONBOARD;
import static misc.Config.THREE_MENS;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class State implements FFTState {
    private Move move;
    private int turn;
    private long zobrist_key;
    int[][] board;
    boolean canRemove; // can the player remove opponent piece?
    boolean phase2; // did phase 2 start?
    int unplaced; // need to place 12 pieces for phase2 (half for each player)

    // Reachability
    HashSet<State> reachableParents;
    boolean reachable;

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
    public LiteralSet getLiterals() { // Including negatives, used for creating rules
        LiteralSet literals = new LiteralSet();
        if (phase2)
            literals.add(new Literal("phase2"));
        if (!THREE_MENS && canRemove)
            literals.add(new Literal("canRemove"));
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (!validPos(i, j))
                    continue;
                int occ = board[i][j];
                Position pos = new Position(i, j, occ);
                int id = Atoms.posToId.get(pos);
                literals.add(new Literal(id, false));
            }
        }
        return literals;
    }

    @Override
    public LiteralSet getAllLiterals() {
        LiteralSet literals = getLiterals();
        literals.add(new Literal("phase2", phase2));
        if (!THREE_MENS)
            literals.add(new Literal("canRemove", canRemove));
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

    public static boolean validPos(int i, int j) {
        if (THREE_MENS)
            return true;
        return (i != 0 || j != 1) && (i != 0 || j != 3) &&
                (i != 1 || j != 0) && (i != 1 || j != 4) &&
                (i != 2 || j != 2) && (i != 3 || j != 0) &&
                (i != 3 || j != 4) && (i != 4 || j != 1) &&
                (i != 4 || j != 3);
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

    @Override
    public HashSet<State> getReachableParents() {
        return reachableParents;
    }

    @Override
    public void addReachableParent(FFTState parent) {
        addReachableParent((State) parent);
    }

    @Override
    public void removeReachableParent(FFTState parent) {
        removeReachableParent((State) parent);
    }

    @Override
    public long getBitString() {
        return getLiterals().getBitString();
    }

    @Override
    public boolean isReachable() {
        return reachable;
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
