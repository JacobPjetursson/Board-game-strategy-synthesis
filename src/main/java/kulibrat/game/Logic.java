package kulibrat.game;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;

import java.awt.*;
import java.util.ArrayList;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Logic {
    public static final int POS_NONBOARD = -1;

    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, Node node) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Point p : node.getPieces(team)) {
            moves.addAll(legalMovesFromPiece(p.y, p.x, team, node.getBoard()));
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
    public static void doTurn(Move m, Node node) {
        if (gameOver(node)) return;
        else if (m.team != node.getTurn()) {
            System.out.println("Not your turn");
            return;
        }
        // Set new entry to team value (RED or BLACK)
        if (m.newCol == POS_NONBOARD && m.newRow == POS_NONBOARD) {
            node.addPoint(m.team);
        } else {
            if (node.getBoard()[m.newRow][m.newCol] == PLAYER1) {
                node.addUnPlaced(PLAYER1);
            } else if (node.getBoard()[m.newRow][m.newCol] == PLAYER2) {
                node.addUnPlaced(PLAYER2);
            }
            node.setBoardEntry(m.newRow, m.newCol, m.team);
        }
        // Set old to 0, or decrement unplaced
        if (m.oldRow == POS_NONBOARD && m.oldCol == POS_NONBOARD) {
            node.removeUnPlaced(m.team);
        } else {
            node.setBoardEntry(m.oldRow, m.oldCol, 0);
        }
        // Change turn
        if (node.getTurn() == PLAYER1) node.setTurn(PLAYER2);
        else node.setTurn(PLAYER1);
        // If the new player has no move, pass that turn
        if (legalMoves(node.getTurn(), node).isEmpty()) {
            Logic.passTurn(node);
        }
    }

    public static boolean gameOver(Node node) {
        return node.getScore(PLAYER1) == node.getScoreLimit() ||
                node.getScore(PLAYER2) == node.getScoreLimit() || locked(node);
    }

    // Passes the turn for the current player
    private static void passTurn(Node node) {
        if (node.getTurn() == PLAYER1) node.setTurn(PLAYER2);
        else node.setTurn(PLAYER1);
    }

    // Finds the winner, granted that the game is over
    public static int getWinner(Node node) {
        if (gameOver(node)) {
            if (locked(node)) return (node.getMove().team) == PLAYER1 ? PLAYER2 : PLAYER1;
            else return node.getMove().team;
        }
        return 0;
    }

    // This is called when checking for game over, and checks if no agents can move
    private static boolean locked(Node node) {
        return legalMoves(PLAYER1, node).isEmpty() && legalMoves(PLAYER2, node).isEmpty();
    }

    public boolean gameOver(FFTNode node) {
        return gameOver((Node) node);
    }

    public int getWinner(FFTNode node) {
        return getWinner((Node) node);
    }

    public boolean isLegalMove(FFTNode node, FFTMove move) {
        Move m = (Move) move;
        return legalMoves(move.getTeam(), (Node) node).contains(m);
    }
}
