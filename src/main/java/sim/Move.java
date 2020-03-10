package sim;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Literal;
import fftlib.game.FFTMove;

import java.util.HashSet;
import java.util.Objects;

import static fftlib.Literal.PIECEOCC_PLAYER;
import static misc.Globals.PLAYER1;

public class Move implements FFTMove {

    public int team;
    public Line line;

    public Move (int team, Line line) {
        this.team = team;
        this.line = line;
    }

    @Override
    public int getTeam() {
        return team;
    }

    @Override
    public Action getAction() {
        HashSet<Literal> addLits = new HashSet<>();
        addLits.add(new Literal(line.n1, line.n2, PIECEOCC_PLAYER, false));
        HashSet<Literal> remLits = new HashSet<>();

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
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return this.line.equals(move.line) &&
                this.team == move.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, team);
    }

    public String toString() {
        String colorStr = (team == PLAYER1) ? "P1: " : "P2: ";
        return colorStr + "(" + line.n1 + "," +line.n2 + ");";
    }
}
