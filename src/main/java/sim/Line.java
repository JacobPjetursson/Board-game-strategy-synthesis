package sim;

import java.util.Objects;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Line {
    public static final int NO_COLOR = 0;

    int n1, n2;
    int color;

    public Line(int n1, int n2) {
        this.n1 = n1;
        this.n2 = n2;
        this.color = NO_COLOR;
    }

    Line(Line l) {
        this.n1 = l.n1;
        this.n2 = l.n2;
        this.color = l.color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return color == line.color && samePos(line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(n1, n2, color);
    }

    public String toString() {
        String colorStr = (color == PLAYER1) ? "P1:" : (color == PLAYER2) ? "P2:" : "B:";
        return colorStr + "(" + n1 + "," + n2 + ")";
    }

    boolean samePos(Line l) {
        return (this.n1 == l.n1 && this.n2 == l.n2 ||
                (this.n2 == l.n1 && this.n1 == l.n2));
    }
}
