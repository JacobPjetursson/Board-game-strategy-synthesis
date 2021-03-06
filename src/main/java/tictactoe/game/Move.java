package tictactoe.game;

import fftlib.game.FFTMove;

import java.util.Objects;

import static misc.Globals.PLAYER_NONE;

public class Move extends FFTMove {
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

    @Override
    public FFTMove clone() {
        return new Move(this);
    }

    public String toString() {
        return "ROW: " + row + " COL: " + col + " TEAM: " + team;
    }
}
