package mens_morris;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Literal;
import fftlib.game.FFTMove;

import java.util.HashSet;
import java.util.Objects;

import static fftlib.Literal.PIECEOCC_ENEMY;
import static fftlib.Literal.PIECEOCC_PLAYER;
import static mens_morris.Logic.POS_NONBOARD;


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
        HashSet<Literal> addLits = new HashSet<>();
        HashSet<Literal> remLits = new HashSet<>();

        if (newRow != POS_NONBOARD) {
            addLits.add(new Literal(newRow, newCol, PIECEOCC_PLAYER, false));
        }
        if (oldRow != POS_NONBOARD) {
            if (newRow == POS_NONBOARD) { // remove enemy piece
                remLits.add(new Literal(oldRow, oldCol, PIECEOCC_ENEMY, false));
            } else {
                remLits.add(new Literal(oldRow, oldCol, PIECEOCC_PLAYER, false));
            }
        }
        Clause addClause = new Clause(addLits);
        Clause remClause = new Clause(remLits);
        return new Action(addClause, remClause);
    }

    @Override
    public void setTeam(int team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof kulibrat.game.Move)) return false;
        kulibrat.game.Move move = (kulibrat.game.Move) o;
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
