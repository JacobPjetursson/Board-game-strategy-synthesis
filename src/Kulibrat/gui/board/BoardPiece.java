package gui.board;

import game.Controller;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import misc.Config;

import static misc.Config.*;

public class BoardPiece extends Circle {
    private Controller cont;
    private Color color;
    private Color green = new Color(0.2, 0.7, 0, 1);
    private boolean selected;
    private boolean best;
    private int team;
    private int row;
    private int col;
    private boolean clickable;

    BoardPiece(int team, Controller cont, int radius, boolean clickable) {
        this.clickable = clickable;
        this.cont = cont;
        this.team = team;
        this.row = -1;
        this.col = -1;
        this.color = (team == RED) ? Color.RED : Color.BLACK;
        setRadius(radius);
        setStrokeWidth(3.5);
        setColor(color, color);

        setOnMouseEntered(me -> {
            if (!isControllable()) return;
            if (team == RED && !selected) {
                Color lightRed = new Color(1.0, 0.5, 0.5, 1.0);
                if (best) {
                    setColor(lightRed, green);
                } else {
                    setColor(lightRed, lightRed);
                }
            } else if (team == BLACK && !selected) {
                Color gray = new Color(0.3, 0.3, 0.3, 1.0);
                if (best) {
                    setColor(gray, green);
                } else {
                    setColor(gray, gray);
                }
            }
        });

        setOnMouseExited(me -> {
            if (!selected && isControllable()) {
                if (best) {
                    setColor(color, green);
                } else setColor(color, color);
            }
        });

        setOnMouseClicked(event -> {
            if (!selected && isControllable()) select();
        });
    }

    BoardPiece(int team, Controller cont, int row, int col, int radius, boolean clickable) {
        this(team, cont, radius, clickable);
        this.row = row;
        this.col = col;
    }

    private void select() {
        selected = true;
        setFill(color);
        setStroke(Color.BLUE);
        cont.setSelected(this);
    }

    public void setBest(boolean best) {
        this.best = best;
        if (best) {
            setColor(color, green);
        } else {
            setColor(color, color);
        }
    }

    private boolean isControllable() {
        return Config.CUSTOMIZABLE || clickable && cont.getState().getTurn() == this.team &&
                (cont.getPlayerInstance(team) == HUMAN || (cont.getPlayerInstance(team) == FFT && cont.getFFTAllowInteraction()));
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

    public void deselect() {
        selected = false;
        if (best) {
            setColor(color, green);
        } else {
            setColor(color, color);
        }
    }
}
