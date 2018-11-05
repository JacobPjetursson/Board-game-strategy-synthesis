package tictactoe.gui.board;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static misc.Config.PLAYER1;

public class BoardPiece extends Circle {
    private Color color;
    private int team;
    private int row;
    private int col;

    BoardPiece(int team, int radius) {
        this.team = team;
        this.row = -1;
        this.col = -1;
        this.color = (team == PLAYER1) ? Color.RED : Color.BLACK;
        setRadius(radius);
        setStrokeWidth(3.5);
        setColor(color, color);
    }

    public BoardPiece(int team, int row, int col, int radius) {
        this(team, radius);
        this.row = row;
        this.col = col;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public int getTeam() {
        return team;
    }

    private void setColor(Color fill, Color stroke) {
        setFill(fill);
        setStroke(stroke);
    }
}
