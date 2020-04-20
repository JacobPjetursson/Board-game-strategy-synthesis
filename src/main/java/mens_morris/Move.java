package mens_morris;

import fftlib.Action;
import fftlib.FFTManager;
import fftlib.game.FFTMove;

import java.util.Objects;



public class Move implements FFTMove {

    public int team;
    public int oldRow, oldCol, newRow, newCol;

    public Move (int oldRow, int oldCol, int newRow, int newCol, int team) {
        this.team = team;
        this.newRow = newRow;
        this.newCol = newCol;
        this.oldRow = oldRow;
        this.oldCol = oldCol;
    }

    @Override
    public int getTeam() {
        return team;
    }

    @Override
    public Action getAction() {
        return FFTManager.moveToAction.apply(this);
    }

    @Override
    public void setTeam(int team) {
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

    @Override
    public String toString() {
        return String.format("(OLDROW: %s, OLDCOL: %s, NEWROW: %s, NEWCOL: %s, TEAM: %s)",
                oldRow, oldCol, newRow, newCol, team);
    }
}
