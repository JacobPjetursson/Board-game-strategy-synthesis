package kulibrat.game;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.awt.*;
import java.util.ArrayList;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class Logic implements FFTLogic {
    public static final int POS_NONBOARD = -1;

    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, State state) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Point p : state.getPieces(team)) {
            moves.addAll(legalMovesFromPiece(p.y, p.x, team, state.getBoard()));
        }
        return moves;
    }

    // Outputs a list of legal moves from a single piece
    public static ArrayList<Move> legalMovesFromPiece(int oldRow, int oldCol, int team, int[][] board) {
        ArrayList<Move> list = new ArrayList<>();
        int maxRow = board.length - 1;
        int maxCol = board[0].length - 1;

        if (team == PLAYER1) {
            // INITIAL MOVE
            if (oldRow == POS_NONBOARD && oldCol == POS_NONBOARD) {
                for (int i = 0; i < maxCol + 1; i++) {
                    if (board[maxRow][i] <= 0) {
                        list.add(new Move(oldRow, oldCol, maxRow, i, team));
                    }
                }
                // POINT GAINED
            } else if (oldRow == 0) {
                list.add(new Move(oldRow, oldCol, POS_NONBOARD, POS_NONBOARD, team));
            } else {
                // DIAGONAL MOVE
                if (oldCol + 1 <= maxCol && board[oldRow - 1][oldCol + 1] <= 0) {
                    list.add(new Move(oldRow, oldCol, oldRow - 1, oldCol + 1, team));
                }
                if (oldCol - 1 >= 0 && board[oldRow - 1][oldCol - 1] <= 0) {
                    list.add(new Move(oldRow, oldCol, oldRow - 1, oldCol - 1, team));
                }
                // ATTACK MOVE
                if (board[oldRow - 1][oldCol] == PLAYER2) {
                    list.add(new Move(oldRow, oldCol, oldRow - 1, oldCol, team));
                }
                // JUMP MOVE
                int jump = 1;
                while (board[oldRow - jump][oldCol] == PLAYER2) {
                    jump++;
                    if (oldRow - jump < 0) {
                        list.add(new Move(oldRow, oldCol, POS_NONBOARD, POS_NONBOARD, team));
                        break;
                    }
                    if (board[oldRow - jump][oldCol] <= 0) {
                        list.add(new Move(oldRow, oldCol, oldRow - jump, oldCol, team));
                        break;
                    }
                }
            }
        } else {
            // INITIAL MOVE
            if (oldRow == POS_NONBOARD && oldCol == POS_NONBOARD) {
                for (int i = 0; i < maxCol + 1; i++) {
                    if (board[0][i] <= 0) {
                        list.add(new Move(oldRow, oldCol, 0, i, team));
                    }
                }
                // POINT GAINED
            } else if (oldRow == maxRow) {
                list.add(new Move(oldRow, oldCol, POS_NONBOARD, POS_NONBOARD, team));
            } else {
                // DIAGONAL MOVE
                if (oldCol + 1 <= maxCol && board[oldRow + 1][oldCol + 1] <= 0) {
                    list.add(new Move(oldRow, oldCol, oldRow + 1, oldCol + 1, team));
                }
                if (oldCol - 1 >= 0 && board[oldRow + 1][oldCol - 1] <= 0) {
                    list.add(new Move(oldRow, oldCol, oldRow + 1, oldCol - 1, team));
                }
                // ATTACK MOVE
                if (board[oldRow + 1][oldCol] == PLAYER1) {
                    list.add(new Move(oldRow, oldCol, oldRow + 1, oldCol, team));
                }
                // JUMP MOVE
                int jump = 1;

                while (board[oldRow + jump][oldCol] == PLAYER1) {
                    jump++;
                    if (oldRow + jump > maxRow) {
                        list.add(new Move(oldRow, oldCol, POS_NONBOARD, POS_NONBOARD, team));
                        break;
                    }
                    if (board[oldRow + jump][oldCol] <= 0) {
                        list.add(new Move(oldRow, oldCol, oldRow + jump, oldCol, team));
                        break;
                    }
                }
            }
        }
        return list;
    }

    // Do a turn based on a move from a given state. Checks if the move is from the correct player, but does not check for illegal moves.
    // It also adds the points and changes the board based on the move
    public static void doTurn(Move m, State state) {
        if (gameOver(state)) return;
        else if (m.team != state.getTurn()) {
            System.out.println("Not your turn");
            return;
        }
        // Set new entry to team value (RED or BLACK)
        if (m.newCol == POS_NONBOARD && m.newRow == POS_NONBOARD) {
            state.addPoint(m.team);
        } else {
            if (state.getBoard()[m.newRow][m.newCol] == PLAYER1) {
                state.addUnPlaced(PLAYER1);
            } else if (state.getBoard()[m.newRow][m.newCol] == PLAYER2) {
                state.addUnPlaced(PLAYER2);
            }
            state.setBoardEntry(m.newRow, m.newCol, m.team);
        }
        // Set old to 0, or decrement unplaced
        if (m.oldRow == POS_NONBOARD && m.oldCol == POS_NONBOARD) {
            state.removeUnPlaced(m.team);
        } else {
            state.setBoardEntry(m.oldRow, m.oldCol, 0);
        }
        // Change turn
        if (state.getTurn() == PLAYER1) state.setTurn(PLAYER2);
        else state.setTurn(PLAYER1);
        // If the new player has no move, pass that turn
        if (legalMoves(state.getTurn(), state).isEmpty()) {
            Logic.passTurn(state);
        }
    }

    public static boolean gameOver(State state) {
        return state.getScore(PLAYER1) == state.getScoreLimit() ||
                state.getScore(PLAYER2) == state.getScoreLimit() || locked(state);
    }

    // Passes the turn for the current player
    private static void passTurn(State state) {
        if (state.getTurn() == PLAYER1) state.setTurn(PLAYER2);
        else state.setTurn(PLAYER1);
    }

    // Finds the winner, granted that the game is over
    public static int getWinner(State state) {
        if (gameOver(state)) {
            if (locked(state)) return (state.getMove().team) == PLAYER1 ? PLAYER2 : PLAYER1;
            else return state.getMove().team;
        }
        return 0;
    }

    // This is called when checking for game over, and checks if no agents can move
    private static boolean locked(State state) {
        return legalMoves(PLAYER1, state).isEmpty() && legalMoves(PLAYER2, state).isEmpty();
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
}
