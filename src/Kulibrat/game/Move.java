package kulibrat.game;

import fftlib.game.FFTMove;
import misc.Config;

import java.util.Objects;

import static kulibrat.game.Logic.POS_NONBOARD;

public class Move implements FFTMove {
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

    public Move() {

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


    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    @Override
    public String print() {
        return String.format("OLDROW: %s, OLDCOL: %s, NEWROW: %s, NEWCOL: %s",
                oldRow, oldCol, newRow, newCol);
    }

    public Move reflect() {
        int[][] board = new int[Config.kuliBWidth][Config.kuliBHeight];
        if (oldCol != POS_NONBOARD)
            board[oldRow][oldCol] = -team;
        if (newCol != POS_NONBOARD)
            board[newRow][newCol] = team;

        int[][] ref = new int[Config.kuliBWidth][Config.kuliBHeight];
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
            refMove.newRow = POS_NONBOARD;
            refMove.newCol = POS_NONBOARD;
        } else if (!oldSet) {
            refMove.oldCol = POS_NONBOARD;
            refMove.oldRow = POS_NONBOARD;
        }
        return refMove;
    }
}
