package tictactoe.game;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.util.ArrayList;

import static misc.Config.*;
import static tictactoe.FFT.FFTAutoGen.INCLUDE_ILLEGAL_STATES;


public class Logic implements FFTLogic {

    static ArrayList<Move> legalMoves(int team, State state) {
        ArrayList<Move> list = new ArrayList<>();
        int enemy = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        int[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 0) {
                    list.add(new Move(i, j, team));
                    if (INCLUDE_ILLEGAL_STATES)
                        list.add(new Move(i, j, enemy));
                }
            }
        }
        return list;
    }

    public static void doTurn(Move m, State state) {
        if (getWinner(state) != 0) return;

        state.setBoardEntry(m.row, m.col, m.team);

        // Change turn
        if (state.getTurn() == PLAYER1) state.setTurn(PLAYER2);
        else state.setTurn(PLAYER1);
    }

    public static boolean gameOver(State state) {
        if (getWinner(state) != 0)
            return true;
        else return isDraw(state);
    }

    private static boolean isDraw(State state) {
        boolean draw = true;
        for (int[] aBoard : state.getBoard()) {
            for (int anABoard : aBoard) {
                if (anABoard == 0) {
                    draw = false;
                }
            }
        }
        return draw;
    }

    public static int getWinner(State state) {
        int[][] board = state.getBoard();
        for (int team = 1; team < 3; team++) {
            if (board[0][0] == team && board[1][1] == team && board[2][2] == team) {
                return team;
            } else if (board[0][2] == team && board[1][1] == team && board[2][0] == team) {
                return team;
            } else if (board[0][0] == team && board[0][1] == team && board[0][2] == team) {
                return team;
            } else if (board[1][0] == team && board[1][1] == team && board[1][2] == team) {
                return team;
            } else if (board[2][0] == team && board[2][1] == team && board[2][2] == team) {
                return team;
            } else if (board[0][0] == team && board[1][0] == team && board[2][0] == team) {
                return team;
            } else if (board[0][1] == team && board[1][1] == team && board[2][1] == team) {
                return team;
            } else if (board[0][2] == team && board[1][2] == team && board[2][2] == team) {
                return team;
            }
        }
        return 0;
    }

    private static boolean isLegalMove(State state, Move move) {
        return legalMoves(move.team, state).contains(move);
    }

    public boolean gameOver(FFTState state) {
        return gameOver((State) state);
    }

    public int getWinner(FFTState state) {
        return getWinner((State) state);
    }

    public boolean isLegalMove(FFTState state, FFTMove move) {
        return isLegalMove((State) state, (Move) move);
    }
}
