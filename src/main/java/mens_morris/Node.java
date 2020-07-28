package mens_morris;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;

import java.util.ArrayList;
import java.util.Arrays;

import static mens_morris.Logic.POS_NONBOARD;
import static misc.Config.THREE_MENS;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Node extends FFTNode {
    private Move move;
    private long zobrist_key;
    int[][] board;
    boolean canRemove; // can the player remove opponent piece?
    boolean phase2; // did phase 2 start?
    int unplaced; // need to place 12 pieces for phase2 (half for each player)

    // varies depending on game type
    private static final int MEN = (THREE_MENS) ? 3*2 : 6*2;
    public static final int BOARD_SIZE = (THREE_MENS) ? 3 : 5;

    // initial state
    public Node() {
        turn = PLAYER1;
        board = initBoard();
        phase2 = false;
        canRemove = false;
        // each player starts with specific amount of men
        unplaced = MEN;
        this.zobrist_key = initZobrist();
    }

    // next state
    public Node(Node parent, Move m) {
        this(parent);
        this.move = m;
        Logic.doTurn(m, this);
        updateHashCode(parent);
    }

    // Duplicate constructor, for "root" node
    public Node(Node node) {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < node.board.length; i++) {
            board[i] = Arrays.copyOf(node.board[i], node.board[i].length);
        }
        if (node.move != null)
            this.move = new Move(node.move);
        this.turn = node.turn;
        this.phase2 = node.phase2;
        this.canRemove = node.canRemove;
        unplaced = node.unplaced;
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

    private void updateHashCode(Node parent) {
        zobrist_key ^= Zobrist.turn[parent.turn];
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
    public FFTNode getNextNode(FFTMove move) {
        return getNextState((Move) move);
    }

    @Override
    public boolean isTerminal() {
        return Logic.gameOver(this);
    }

    @Override
    public int getWinner() {
        return Logic.getWinner(this);
    }

    public Node getNextState(Move m) {
        return new Node(this, m);
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

    public Move getMove() {
        return move;
    }

    @Override
    public FFTNode clone() {
        return new Node(this);
    }

    public void changeTurn() {
        if (turn == PLAYER1)
            turn = PLAYER2;
        else
            turn = PLAYER1;
    }

    public String toString() {
        String phaseStr = (phase2) ? "2" : "1";
        return Arrays.deepToString(board) + " , TURN: " + turn + ", PHASE: " + phaseStr;
    }
}
