package mens_morris;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.util.ArrayList;

import static misc.Config.THREE_MENS;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Logic implements FFTLogic {
    public static final int POS_NONBOARD = -1;

    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, State state) {
        ArrayList<Move> moves = new ArrayList<>();
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;

        for (int i = 0; i < state.board.length; i++) {
            for (int j = 0; j < state.board[i].length; j++) {
                if (state.canRemove) {  // remove
                    if (state.board[i][j] == opponent &&
                            (!isMill(i, j, team, state) || menCounter(state, opponent) == 3))
                        moves.add(new Move(i, j, POS_NONBOARD, POS_NONBOARD, team));
                }
                else if (!state.phase2) {
                    if (state.board[i][j] == 0)
                        moves.add(new Move(POS_NONBOARD, POS_NONBOARD, i, j, team)); // place new piece
                } else if (state.board[i][j] == team) {
                    moves.addAll(getMovesFromPos(i, j, team, state));
                }
            }
        }
        return moves;
    }

    public static boolean isMill(int row, int col, int team, State state) {
        if (THREE_MENS)
            return isMillThreeMens(row, col, team, state);
        int step = 1;
        // increase step if row or col is in edge of board
        if (row == 0 || row == state.board.length -1 || col == 0 || col == state.board.length - 1)
            step = 2;
        // check for 3-in-a-row with the new placement
        // vertical
        int vCounter = 0;
        if (row > (step-1) && state.board[row-step][col] == team)
            vCounter++;
        if (row > (step*2-1) && state.board[row-step*2][col] == team)
            vCounter++;
        if (row < state.board.length-step && state.board[row+step][col] == team)
            vCounter++;
        if (row < state.board.length-step*2 && state.board[row+step*2][col] == team)
            vCounter++;
        if (vCounter == 2) {
            return true;
        }
        int hCounter = 0;
        if (col > (step-1) && state.board[row][col-step] == team)
            hCounter++;
        if (col > (step*2-1) && state.board[row][col-step*2] == team)
            hCounter++;
        if (col < state.board.length-step && state.board[row][col+step] == team)
            hCounter++;
        if (col < state.board.length-step*2 && state.board[row][col+step*2] == team)
            hCounter++;
        return hCounter == 2;
    }

    // Check rows and cols in all 4 directions
    public static ArrayList<Move> getMovesFromPos(int row, int col, int team, State state) {
        ArrayList<Move> moves = new ArrayList<>();
        int vStep = 1;
        int hStep = 1;
        // increase step if row or col is in edge of board and not 3-mens
        if (!THREE_MENS) {
            if (row == 0 || row == state.board.length -1)
                hStep = 2;
            if (col == 0 || col == state.board.length - 1)
                vStep = 2;
        }

        // north
        if (row > (vStep-1) && state.board[row-vStep][col] == 0) {
            moves.add(new Move(row, col, row-vStep, col, team));
        }
        // south
        if (row < state.board.length-vStep && state.board[row+vStep][col] == 0) {
            moves.add(new Move(row, col, row+vStep, col, team));
        }
        // east
        if (col < state.board.length-hStep && state.board[row][col+hStep] == 0) {
            moves.add(new Move(row, col, row, col+hStep, team));
        }
        // west
        if (col > (hStep-1) && state.board[row][col - hStep] == 0) {
            moves.add(new Move(row, col, row, col-hStep, team));
        }

        // Move diagonally if three mens
        if (THREE_MENS) {
            // north west
            if (col < state.board.length-1 && row > 0 &&
                    state.board[row-1][col+1] == 0)
                moves.add(new Move(row, col, row-1, col+1, team));
            // south west
            if (col < state.board.length-1 && row < state.board.length-1 &&
                    state.board[row+1][col+1] == 0)
                moves.add(new Move(row, col, row+1, col+1, team));
            // north east
            if (col > 0 && row > 0 &&
                    state.board[row-1][col-1] == 0)
                moves.add(new Move(row, col, row-1, col-1, team));
            // south east
            if (col > 0 && row < state.board.length-1 &&
                    state.board[row+1][col-1] == 0)
                moves.add(new Move(row, col, row+1, col-1, team));

        }
        return moves;
    }

    private static int menCounter(State state, int team) {
        int counter = 0;
        for (int i = 0; i < state.board.length; i++) {
            for (int j = 0; j < state.board[i].length; j++) {
                if (state.board[i][j] == team)
                    counter++;
            }
        }
        return counter;
    }

    public static boolean gameOver(State state) {
        if (legalMoves(state.getTurn(), state).isEmpty())
            return true;
        if (THREE_MENS) {
            Move m = state.getMove();
            if (m == null)
                return false;
            return isMillThreeMens(m.newRow, m.newCol, m.team, state);
        }
        if (!state.phase2)
            return false;
        int p1Counter = 0;
        int p2Counter = 0;
        for (int i = 0; i < state.board.length; i++) {
            for (int j = 0; j < state.board[i].length; j++) {
                if (state.board[i][j] == PLAYER1)
                    p1Counter++;
                else if (state.board[i][j] == PLAYER2)
                    p2Counter++;
            }
        }
        return (p1Counter < 3 || p2Counter < 3);
    }

    // Finds the winner, granted that the game is over
    public static int getWinner(State state) {
        if (gameOver(state)) {
            return state.getMove().team == PLAYER1 ? PLAYER1 : PLAYER2;
        }
        return 0;
    }

    static void doTurn(Move m, State state) {
        if (gameOver(state)) return;

        if (m.team != state.getTurn()) {
            System.out.println("Not your turn");
            return;
        }
        if (state.canRemove) {
            state.board[m.oldRow][m.oldCol] = 0;
            state.canRemove = false;
            state.changeTurn();
            return;
        }
        if (state.phase2) {
            state.board[m.oldRow][m.oldCol] = 0;
        }
        state.board[m.newRow][m.newCol] = m.team;
        if (state.unplaced != 0)
            state.unplaced--;

        state.phase2 = isPhase2(state);
        if (isMill(m.newRow, m.newCol, m.team, state)) {
            state.canRemove = true;
            return;
        }

        // Change turn
        state.changeTurn();
    }

    private static boolean isPhase2(State state) {
        return state.unplaced == 0;
    }

    public boolean gameOver(FFTState state) {
        return gameOver((State) state);
    }

    public int getWinner(FFTState state) {
        return getWinner((State) state);
    }

    public boolean isLegalMove(FFTState state, FFTMove move) {
        Move m = (Move) move;
        return legalMoves(move.getTeam(), (State) state).contains(m);
    }

    public static boolean isMillThreeMens(int row, int col, int team, State state) {
        // horizontal
        int counter = 0;
        for (int i = 0; i < 3; i++) {
            if (state.board[row][i] == team)
                counter++;
        }
        if (counter == 3)
            return true;
        counter = 0;
        // vertical
        for (int i = 0; i < 3; i++) {
            if (state.board[i][col] == team)
                counter++;
        }
        if (counter == 3)
            return true;
        counter = 0;
        boolean onLine = false;
        // NW/SE
        for (int i = 0; i < 3; i++) {
            if (state.board[i][i] == team)
                counter++;
            if (row == i && col == i)
                onLine = true;
        }
        if (counter == 3 && onLine)
            return true;
        counter = 0;
        int j = 0;
        // NE / SW
        for (int i = 2; i >= 0; i--) {
            if (state.board[i][j] == team)
                counter++;
            if (row == i && col == j)
                onLine = true;
            j++;
        }
        return counter == 3 && onLine;
    }
}
