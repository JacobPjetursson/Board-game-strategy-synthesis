package kulibrat.gui.board;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import kulibrat.game.Controller;

import static misc.Config.*;


public class BoardPiece extends Circle {
    private Controller cont;
    private Color color;
    private Color green = new Color(0.2, 0.7, 0, 1);
    private Color lightRed = new Color(1.0, 0.5, 0.5, 1.0);
    private Color gray = new Color(0.3, 0.3, 0.3, 1.0);
    private boolean selected;
    private boolean best;
    private int team;
    private int row;
    private int col;
    private int clickMode;

    BoardPiece(int team, Controller cont, int radius, int clickMode) {
        this.clickMode = clickMode;
        this.cont = cont;
        this.team = team;
        this.row = -1;
        this.col = -1;
        this.color = (team == PLAYER1) ? Color.RED : Color.BLACK;
        setRadius(radius);
        setStrokeWidth(3.5);
        setColor(color, color);

        setListeners();
    }

    private void setListeners() {
        setOnMouseEntered(me -> {
            if (!isControllable()) return;
            if (!selected) {
                if (team == PLAYER1) {
                    if (best)
                        setColor(lightRed, green);
                    else
                        setColor(lightRed, lightRed);
                }
                else {
                    if (best)
                        setColor(gray, green);
                    else
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
            if (isControllable()) {
                if (clickMode == CLICK_DEFAULT)
                    select();
                else if (clickMode == CLICK_INTERACTIVE) {
                    select();
                    event.consume();
                }
            }
        });
    }

    BoardPiece(int team, Controller cont, int row, int col, int radius, int clickMode) {
        this(team, cont, radius, clickMode);
        this.row = row;
        this.col = col;
    }

    private void select() {
        if (clickMode == CLICK_INTERACTIVE) {
            cont.getInteractiveState().setSelected(this);
        } else {
            cont.setSelected(this);
        }
        selected = true;
        setFill(color);
        setStroke(Color.BLUE);
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
        if (clickMode == CLICK_INTERACTIVE) {
            BoardTile bt = getTile();
            return bt == null || !getTile().highlight;
        }
        return clickMode != CLICK_DISABLED && cont.getState().getTurn() == this.team &&
                (cont.getPlayerInstance(team) == HUMAN || (cont.getPlayerInstance(team) == FFT &&
                        cont.getFFTAllowInteraction()));
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

    private BoardTile getTile() {
        Node n = getParent();
        if (n instanceof BoardTile)
            return (BoardTile) n;
        else return null;
    }
}
