package tictactoe.game;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;

import java.util.ArrayList;

import static misc.Config.TIC_TAC_TOE_RULES;
import static misc.Globals.*;


public class Logic implements FFTLogic {

    static ArrayList<Move> legalMoves(int team, Node node) {
        ArrayList<Move> list = new ArrayList<>();
        int[][] board = node.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 0) {
                    list.add(new Move(i, j, team));
                }
            }
        }
        return list;
    }

    public static void doTurn(Move m, Node node) {
        if (getWinner(node) != 0) return;

        node.setBoardEntry(m.row, m.col, m.team);

        // Change turn
        if (node.getTurn() == PLAYER1) node.setTurn(PLAYER2);
        else node.setTurn(PLAYER1);
    }

    public static boolean gameOver(Node node) {
        if (getWinner(node) != 0)
            return true;
        else return isDraw(node);
    }

    private static boolean isDraw(Node node) {
        boolean draw = true;
        for (int[] aBoard : node.getBoard()) {
            for (int anABoard : aBoard) {
                if (anABoard == 0) {
                    draw = false;
                }
            }
        }
        return draw;
    }

    public static int getWinner(Node node) {
        int[][] board = node.getBoard();
        if (TIC_TAC_TOE_RULES == TIC_TAC_TOE_SIMPLE_RULES)
            return getSimpleRuleWinner(node);

        if (TIC_TAC_TOE_RULES == TIC_TAC_TOE_STUPID_RULES)
            return getStupidRuleWinner(node);

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

    private static boolean isLegalMove(Node node, Move move) {
        return legalMoves(move.team, node).contains(move);
    }

    public boolean gameOver(FFTNode node) {
        return gameOver((Node) node);
    }

    public int getWinner(FFTNode node) {
        return getWinner((Node) node);
    }

    public boolean isLegalMove(FFTNode node, FFTMove move) {
        return isLegalMove((Node) node, (Move) move);
    }

    public static int getSimpleRuleWinner(Node node) {
        int[][] board = node.getBoard();
        for (int team = 1; team < 3; team++) {
            if (board[0][0] == team && board[1][1] == team) {
                return team;
            } else if (board[0][0] == team && board[0][1] == team) {
                return team;
            } else if (board[1][1] == team && board[2][2] == team) {
                return team;
            } else if (board[0][2] == team && board[1][1] == team) {
                return team;
            } else if (board[1][1] == team && board[2][0] == team) {
                return team;
            } else if (board[0][0] == team && board[0][1] == team) {
                return team;
            } else if (board[0][1] == team && board[0][2] == team) {
                return team;
            } else if (board[1][0] == team && board[1][1] == team) {
                return team;
            } else if (board[1][1] == team && board[1][2] == team) {
                return team;
            } else if (board[2][0] == team && board[2][1] == team) {
                return team;
            } else if (board[2][1] == team && board[2][2] == team) {
                return team;
            } else if (board[0][0] == team && board[1][0] == team) {
                return team;
            } else if (board[1][0] == team && board[2][0] == team) {
                return team;
            } else if (board[0][1] == team && board[1][1] == team) {
                return team;
            } else if (board[1][1] == team && board[2][1] == team) {
                return team;
            } else if (board[0][2] == team && board[1][2] == team) {
                return team;
            } else if (board[1][2] == team && board[2][2] == team) {
                return team;

            } else if (board[1][0] == team && board[0][1] == team) {
                return team;
            } else if (board[0][1] == team && board[1][2] == team) {
                return team;
            } else if (board[1][0] == team && board[2][1] == team) {
                return team;
            } else if (board[2][1] == team && board[1][2] == team) {
                return team;
            }
        }
        return 0;
    }

    public static int getStupidRuleWinner(Node node) {
        int [][] board = node.getBoard();
        for (int[] ints : board) {
            for (int anInt : ints) {
                if (anInt == 0)
                    return 0;
            }
        }
        return PLAYER1;
    }
}
