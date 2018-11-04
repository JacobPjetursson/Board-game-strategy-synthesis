package gui;

import game.Controller;
import game.Move;
import game.State;
import gui.board.Board;
import gui.board.BoardTile;
import gui.board.Goal;
import gui.board.Player;
import gui.menu.Arrow;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import static misc.Config.RED;

public class PlayBox extends Group {
    private Board board;
    private Goal goalRed;
    private Goal goalBlack;
    private Player playerRed;
    private Player playerBlack;
    private SimpleBooleanProperty isRendered = new SimpleBooleanProperty();

    public PlayBox(Player playerBlack, Goal goalRed, Board board, Goal goalBlack, Player playerRed) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.board = board;
        this.goalBlack = goalBlack;
        this.goalRed = goalRed;
        this.playerBlack = playerBlack;
        this.playerRed = playerRed;
        vbox.getChildren().addAll(playerBlack, goalRed, board, goalBlack, playerRed);
        getChildren().add(vbox);
        updateBounds();
    }

    public void update(Controller cont, State s) {
        board.update(cont, s);
        playerRed.update(cont, s);
        playerBlack.update(cont, s);
    }

    protected void layoutChildren() {
        super.layoutChildren();
        isRendered.setValue(true);
    }

    public void addArrow(Move m, Color color) {
        isRendered.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            double startX, startY, endX, endY;
            Bounds b;
            if (m.oldCol == -1) {
                if (m.team == RED) b = playerRed.localToParent(playerRed.getBoundsInLocal());
                else b = playerBlack.localToParent(playerBlack.getBoundsInLocal());
            } else {
                BoardTile t = board.getTiles()[m.oldRow][m.oldCol];
                b = board.localToParent(t.localToParent(t.getBoundsInLocal()));
            }
            startX = (b.getMaxX() + b.getMinX()) / 2.0;
            startY = (b.getMaxY() + b.getMinY()) / 2.0;

            if (m.newCol == -1) {
                if (m.team == RED) b = goalRed.localToParent(goalRed.getBoundsInLocal());
                else b = goalBlack.localToParent(goalBlack.getBoundsInLocal());
            } else {
                BoardTile t = board.getTiles()[m.newRow][m.newCol];
                b = board.localToParent(t.localToParent(t.getBoundsInLocal()));
            }
            endX = (b.getMaxX() + b.getMinX()) / 2.0;
            endY = (b.getMaxY() + b.getMinY()) / 2.0;

            getChildren().add(new Arrow(startX, startY, endX, endY, color));
        }));
    }
}
