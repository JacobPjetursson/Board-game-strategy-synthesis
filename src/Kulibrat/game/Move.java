package game;

import misc.Config;
import java.util.Objects;

public class Move {
    public int oldRow;
    public int oldCol;
    public int newRow;
    public int newCol;
    public int team;

    public Move(int oldRow, int oldCol, int newRow, int newCol, int team) {
        this.oldRow = oldRow;
        this.oldCol = oldCol;
        this.newRow = newRow;
        this.newCol = newCol;
        this.team = team;
    }

    public Move(Move move) {
        this.oldRow = move.oldRow;
        this.oldCol = move.oldCol;
        this.newRow = move.newRow;
        this.newCol = move.newCol;
        this.team = move.team;
    }

    public Move(int team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return oldRow == move.oldRow &&
                oldCol == move.oldCol &&
                newRow == move.newRow &&
                newCol == move.newCol &&
                team == move.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldRow, oldCol, newRow, newCol, team);
    }

    public Move reflect() {
        int[][] board = new int[Config.bWidth][Config.bHeight];
        if(oldCol != -1)
            board[oldRow][oldCol] = -team;
        if (newCol != -1)
            board[newRow][newCol] = team;

        int[][] ref = new int[Config.bWidth][Config.bHeight];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                ref[i][j] = board[i][board.length - 1 - j];
            }
        }
        Move refMove = new Move(team);
        boolean oldSet = false;
        boolean newSet = false;
        for (int i = 0; i < ref.length; i++) {
            for (int j = 0; j < ref[i].length; j++) {
                if (ref[i][j] == team) {
                    refMove.newRow = i;
                    refMove.newCol = j;
                    newSet = true;
                } else if (ref[i][j] == -team) {
                    refMove.oldRow = i;
                    refMove.oldCol = j;
                    oldSet = true;
                }
            }
        }
        if (!newSet) {
            refMove.newRow = -1;
            refMove.newCol = -1;
        } else if (!oldSet) {
            refMove.oldCol = -1;
            refMove.oldRow = -1;
        }
        return refMove;
    }

    public String print() {
        return "A_" + team + ": (" + oldRow + "," + oldCol + ") "
                + "-> " + "(" + newRow + "," + newCol + ")";
    }
}
