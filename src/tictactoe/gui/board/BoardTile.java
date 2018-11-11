package tictactoe.gui.board;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import tictactoe.game.Controller;

import static misc.Config.*;

public class BoardTile extends StackPane {
    private int row;
    private int col;
    private int tilesize;
    private boolean playMode;
    private Controller cont;
    private Color gray = new Color(0.2, 0.2, 0.2, 1.0);
    private boolean free;

    public BoardTile(int row, int col, int tilesize, Controller cont, boolean playMode) {
        this.row = row;
        this.col = col;
        this.tilesize = tilesize;
        this.playMode = playMode;
        this.cont = cont;
        this.free = true;
        setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setAlignment(Pos.CENTER);
        setPrefSize(tilesize, tilesize);
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setOnMouseEntered(me -> {
            if (isClickable() && getChildren().isEmpty()) {
                addPiece(cont.getState().getTurn(), true);
            }
        });

        setOnMouseExited(me -> {
            if (isClickable() && free) {
                removePiece();
            }
        });
    }

    private boolean isClickable() {
        return playMode && (cont.getPlayerInstance(cont.getState().getTurn()) == HUMAN ||
                (cont.getPlayerInstance(cont.getState().getTurn()) == FFT && cont.getFFTAllowInteraction()));
    }

    public void highlight(Color c, int team) {
        if (c == Color.BLUE) {
            setStyle("-fx-background-color: rgb(0, 0, 255);");
            addPiece(team, false);
        } else {
            setStyle("-fx-background-color: rgb(0, 255, 0);");
        }
    }

    public void addPiece(int team, boolean hover) {
        // circle
        if (team == PLAYER1) {
            Circle c = new Circle();
            System.out.println(tilesize);
            c.setRadius(tilesize/2 - (tilesize/12));
            c.setStrokeWidth(0);
            if (hover)
                c.setFill(gray);
            else
                c.setFill(Color.BLACK);
            getChildren().add(c);
        } else if (team == PLAYER2) {
            drawCross(hover);
        }
    }

    public void drawCross(boolean hover) {
        Group g = new Group();
        Line vertical = new Line(-15, -15, 15, 15);
        Line horizontal = new Line(-15, 15, 15, - 15);
        vertical.setStrokeWidth(7);
        vertical.setSmooth(true);
        horizontal.setStrokeWidth(7);
        horizontal.setSmooth(true);
        if (hover) {
            horizontal.setStroke(gray);
            vertical.setStroke(gray);
        } else {
            horizontal.setStroke(Color.BLACK);
            vertical.setStroke(Color.BLACK);
        }
        g.getChildren().addAll(horizontal, vertical);
        getChildren().add(g);
    }

    public void removePiece() {
        getChildren().clear();
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}

