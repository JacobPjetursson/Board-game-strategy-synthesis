package kulibrat.gui.board.PlayBox;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.board.Board;
import kulibrat.gui.board.BoardTile;
import kulibrat.gui.board.Goal;
import kulibrat.gui.board.Player;
import kulibrat.gui.menu.Arrow;

import java.util.ArrayList;

import static kulibrat.game.Logic.POS_NONBOARD;
import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class PlayBox extends Group {
    Board board;
    Goal goalRed;
    Goal goalBlack;
    Player playerRed;
    Player playerBlack;
    VBox playBox;
    Controller cont;
    int clickMode;
    private ArrayList<Arrow> arrows;

    public PlayBox(int tilesize, int clickMode, Controller cont) {
        this.cont = cont;
        this.clickMode = clickMode;
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: rgb(255, 255, 255);");
        int pieceRadius = tilesize / 3;
        int goalHeight = (tilesize / 6) * 5;

        board = new Board(tilesize, pieceRadius, clickMode, cont);
        playerRed = new Player(PLAYER1, cont, tilesize, pieceRadius, clickMode);
        playerBlack = new Player(PLAYER2, cont, tilesize, pieceRadius, clickMode);
        goalRed = new Goal(3 * tilesize, goalHeight, clickMode, cont);
        goalBlack = new Goal(3 * tilesize, goalHeight, clickMode, cont);
        vbox.getChildren().addAll(playerBlack, goalRed, board, goalBlack, playerRed);
        getChildren().add(vbox);
        this.playBox = vbox;
        arrows = new ArrayList<>();
        updateBounds();
    }

    public void update(State s) {
        board.update(cont, s);
        playerRed.update(cont, s);
        playerBlack.update(cont, s);
    }

    protected void layoutChildren() {
        super.layoutChildren();
    }

    public Board getBoard() {
        return board;
    }

    public Goal getGoal(int team) {
        if (team == PLAYER1)
            return goalRed;
        else return goalBlack;
    }

    public Player getPlayer(int team) {
        if (team == PLAYER1)
            return playerRed;
        else return playerBlack;
    }

    public void addArrow(Move m, Color color) {
        double startX, startY, endX, endY;
        Bounds b;
        if (m.oldCol == POS_NONBOARD) {
            if (m.team == PLAYER1)
                b = playerRed.localToParent(playerRed.getBoundsInLocal());
            else
                b = playerBlack.localToParent(playerBlack.getBoundsInLocal());
        } else {
            BoardTile t = board.getTiles()[m.oldRow][m.oldCol];
            Bounds localBounds = new BoundingBox(0, 0, 0, t.tilesize, t.tilesize, 0);
            b = board.localToParent(t.localToParent(localBounds));
        }
        startX = (b.getMaxX() + b.getMinX()) / 2.0;
        startY = (b.getMaxY() + b.getMinY()) / 2.0;

        if (m.newCol == POS_NONBOARD) {
            if (m.team == PLAYER1) b = goalRed.localToParent(goalRed.getBoundsInLocal());
            else b = goalBlack.localToParent(goalBlack.getBoundsInLocal());
        } else {
            BoardTile t = board.getTiles()[m.newRow][m.newCol];
            b = board.localToParent(t.localToParent(t.getBoundsInLocal()));
        }
        endX = (b.getMaxX() + b.getMinX()) / 2.0;
        endY = (b.getMaxY() + b.getMinY()) / 2.0;
        Arrow a = new Arrow(startX, startY, endX, endY, color);
        arrows.add(a);
        getChildren().add(a);
    }

    public void removeArrows() {
        getChildren().removeAll(arrows);
        arrows.clear();
    }
}
