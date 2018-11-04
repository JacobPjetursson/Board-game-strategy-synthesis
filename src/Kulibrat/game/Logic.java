package game;

import java.awt.*;
import java.util.ArrayList;
import static misc.Config.*;

public abstract class Logic {
    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, State state) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Point p : state.getPieces(team)) {
            moves.addAll(legalMovesFromPiece(p.y, p.x, team, state));
        }
        return moves;
    }

    // Outputs a list of legal moves from a single piece
    static ArrayList<Move> legalMovesFromPiece(int oldRow, int oldCol, int team, State state) {
        ArrayList<Move> list = new ArrayList<>();
        int[][] board = state.getBoard();
        int maxRow = board.length - 1;
        int maxCol = board[0].length - 1;

        if (team == RED) {
            // INITIAL MOVE
            if (oldRow == -1 && oldCol == -1) {
                for (int i = 0; i < maxCol + 1; i++) {
                    if (board[maxRow][i] == 0) {
                        list.add(new Move(oldRow, oldCol, maxRow, i, team));
                    }
                }
                // POINT GAINED (Adds (-1,-1) in list)
            } else if (oldRow == 0) {
                list.add(new Move(oldRow, oldCol, -1, -1, team));
            } else {
                // DIAGONAL MOVE
                if (oldCol + 1 <= maxCol && board[oldRow - 1][oldCol + 1] == 0) {
                    list.add(new Move(oldRow, oldCol, oldRow - 1, oldCol + 1, team));
                }
                if (oldCol - 1 >= 0 && board[oldRow - 1][oldCol - 1] == 0) {
                    list.add(new Move(oldRow, oldCol, oldRow - 1, oldCol - 1, team));
                }
                // ATTACK MOVE
                if (board[oldRow - 1][oldCol] == BLACK) {
                    list.add(new Move(oldRow, oldCol, oldRow - 1, oldCol, team));
                }
                // JUMP MOVE
                int jump = 1;
                while (board[oldRow - jump][oldCol] == BLACK) {
                    jump++;
                    if (oldRow - jump < 0) {
                        list.add(new Move(oldRow, oldCol, -1, -1, team));
                        break;
                    }
                    if (board[oldRow - jump][oldCol] == 0) {
                        list.add(new Move(oldRow, oldCol, oldRow - jump, oldCol, team));
                        break;
                    }
                }
            }
        } else {
            // INITIAL MOVE
            if (oldRow == -1 && oldCol == -1) {
                for (int i = 0; i < maxCol + 1; i++) {
                    if (board[0][i] == 0) {
                        list.add(new Move(oldRow, oldCol, 0, i, team));
                    }
                }
                // POINT GAINED (Adds (-1,-1) in list)
            } else if (oldRow == maxRow) {
                list.add(new Move(oldRow, oldCol, -1, -1, team));
            } else {
                // DIAGONAL MOVE
                if (oldCol + 1 <= maxCol && board[oldRow + 1][oldCol + 1] == 0) {
                    list.add(new Move(oldRow, oldCol, oldRow + 1, oldCol + 1, team));
                }
                if (oldCol - 1 >= 0 && board[oldRow + 1][oldCol - 1] == 0) {
                    list.add(new Move(oldRow, oldCol, oldRow + 1, oldCol - 1, team));
                }
                // ATTACK MOVE
                if (board[oldRow + 1][oldCol] == RED) {
                    list.add(new Move(oldRow, oldCol, oldRow + 1, oldCol, team));
                }
                // JUMP MOVE
                int jump = 1;
                while (board[oldRow + jump][oldCol] == RED) {
                    jump++;
                    if (oldRow + jump > maxRow) {
                        list.add(new Move(oldRow, oldCol, -1, -1, team));
                        break;
                    }
                    if (board[oldRow + jump][oldCol] == 0) {
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
        else if (m.team != state.getTurn() && !CUSTOMIZABLE) {
            System.out.println("Not your turn");
            return;
        }
        // Set new entry to team value (RED or BLACK)
        if (m.newCol == -1 && m.newRow == -1) {
            state.addPoint(m.team);
        } else {
            if (state.getBoard()[m.newRow][m.newCol] == RED) {
                state.addUnPlaced(RED);
            } else if (state.getBoard()[m.newRow][m.newCol] == BLACK) {
                state.addUnPlaced(BLACK);
            }
            state.setBoardEntry(m.newRow, m.newCol, m.team);
        }
        // Set old to 0, or decrement unplaced
        if (m.oldRow == -1 && m.oldCol == -1) {
            state.removeUnPlaced(m.team);
        } else {
            state.setBoardEntry(m.oldRow, m.oldCol, 0);
        }
        // Change turn
        if (m.team == RED) state.setTurn(BLACK);
        else state.setTurn(RED);
        // If the new player has no move, pass that turn
        if (legalMoves(state.getTurn(), state).isEmpty()) {
            Logic.passTurn(state);
        }
    }

    // Passes the turn for the current player
    public static boolean gameOver(State state) {
        return state.getScore(RED) == state.getScoreLimit() ||
                state.getScore(BLACK) == state.getScoreLimit() || locked(state);
    }

    private static void passTurn(State state) {
        if (state.getTurn() == RED) state.setTurn(BLACK);
        else state.setTurn(RED);
    }

    // Finds the winner, granted that the game is over
    public static int getWinner(State state) {
        if (gameOver(state)) {
            if (locked(state)) return (state.getMove().team) == RED ? BLACK : RED;
            else return state.getMove().team;
        }
        return 0;
    }

    // This is called when checking for game over, and checks if no agents can move
    private static boolean locked(State state) {
        return legalMoves(RED, state).isEmpty() && legalMoves(BLACK, state).isEmpty();
    }

    public static boolean isLegalMove(State state, Move move) {
        return legalMoves(move.team, state).contains(move);
    }
}
