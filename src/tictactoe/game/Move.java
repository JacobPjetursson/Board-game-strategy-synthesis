package tictactoe.game;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Literal;
import fftlib.game.FFTMove;

import java.util.ArrayList;
import java.util.Objects;

import static fftlib.Literal.PIECEOCC_PLAYER;
import static misc.Config.PLAYER_NONE;

public class Move implements FFTMove {
    public int row;
    public int col;
    public int team;

    public Move(int row, int col, int team) {
        this.row = row;
        this.col = col;
        this.team = team;
    }

    public Move(Move move) {
        this.row = move.row;
        this.col = move.col;
        this.team = move.team;
    }

    public Move() {
        row = -1;
        col = -1;
        team = PLAYER_NONE;
    }

    public Move(int team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return row == move.row &&
                col == move.col &&
                team == move.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, team);
    }

    public int getTeam() {
        return team;
    }

    @Override
    public Action getAction() {
        ArrayList<Literal> addLits = new ArrayList<>();
        addLits.add(new Literal(row, col, PIECEOCC_PLAYER, false));
        ArrayList<Literal> remLits = new ArrayList<>();

        Clause addClause = new Clause(addLits);
        Clause remClause = new Clause(remLits);
        return new Action(addClause, remClause);
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public String print() {
        return "ROW: " + row + " COL: " + col + " TEAM: " + team;
    }
}
