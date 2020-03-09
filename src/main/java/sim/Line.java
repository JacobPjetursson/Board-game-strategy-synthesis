package sim;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Line {
    public static final int NO_COLOR = 0;

    Point p1, p2;
    int color;
    int id;

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = NO_COLOR;
    }

    Line(int x1, int y1, int x2, int y2) {
        this.p1 = new Point(x1, y1);
        this.p2 = new Point(x2, y2);
        this.color = NO_COLOR;
    }

    Line(Point p1, Point p2, int id) {
        this.p1 = p1;
        this.p2 = p2;
        this.id = id;
    }

    Line(Line l) {
        this.p1 = new Point(l.p1.x, l.p1.y);
        this.p2 = new Point(l.p2.x, l.p2.y);
        this.color = l.color;
        this.id = l.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return color == line.color && samePos(line);
    }

    public ArrayList<Point> getPoints() {
        ArrayList<Point> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        return points;
    }

    @Override
    public int hashCode() {
        return Objects.hash(p1, p2, color);
    }

    public String toString() {
        String colorStr = (color == PLAYER1) ? "P1: " : (color == PLAYER2) ? "P2: " : "NO COLOR: ";
        return colorStr + "(" + p1.x + "," + p1.y + ")," + "(" + p2.x + "," + p2.y + ")";
    }

    boolean samePos(Line l) {
        return (this.p1.equals(l.p1) && this.p2.equals(l.p2)) ||
                (this.p2.equals(l.p1) && this.p1.equals(l.p2));
    }
}
