package kulibrat.game;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Literal;
import fftlib.game.FFTMove;
import misc.Config;
import misc.Globals;

import java.util.HashSet;
import java.util.Objects;

import static fftlib.Literal.PIECEOCC_PLAYER;
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
    public Action getAction() {
        HashSet<Literal> addLits = new HashSet<>();
        if (newRow != POS_NONBOARD)
            addLits.add(new Literal(newRow, newCol, PIECEOCC_PLAYER, false));
        HashSet<Literal> remLits = new HashSet<>();
        if (oldRow != POS_NONBOARD)
            remLits.add(new Literal(oldRow, oldCol, PIECEOCC_PLAYER, false));
        Clause addClause = new Clause(addLits);
        Clause remClause = new Clause(remLits);
        return new Action(addClause, remClause);
    }

    @Override
    public String toString() {
        return String.format("(OLDROW: %s, OLDCOL: %s, NEWROW: %s, NEWCOL: %s, TEAM: %s)",
                oldRow, oldCol, newRow, newCol, team);
    }
}
