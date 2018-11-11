package tictactoe.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.gui.board.Board;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.Player;

import static misc.Config.PLAYER1;

public class PlayBox extends Group {
    private Board board;
    private SimpleBooleanProperty isRendered = new SimpleBooleanProperty();

    public PlayBox(Player playerBlack, Board board, Player playerRed) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.board = board;
        vbox.getChildren().addAll(playerBlack, board, playerRed);
        getChildren().add(vbox);
        updateBounds();
    }

    public void update(State s) {
        board.update(s);
    }

    protected void layoutChildren() {
        super.layoutChildren();
        isRendered.setValue(true);
    }

    public void addHighlight(Move m, Color color) {
        isRendered.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            BoardTile t = board.getTiles()[m.row][m.col];
            t.highlight(color, m.team);
        }));
    }
}
