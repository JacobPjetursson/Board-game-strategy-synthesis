package sim;

import fftlib.game.FFTMove;

import java.util.Objects;

import static misc.Globals.PLAYER1;

public class Move extends FFTMove {

    public int team;
    public Line line;

    public Move (int team, Line line) {
        this.team = team;
        this.line = line;
    }

    public Move (Move dup) {
        this.team = dup.team;
        this.line = new Line(dup.line);
    }

    @Override
    public FFTMove clone() {
        return new Move(this);
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
