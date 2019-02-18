package tictactoe.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.gui.board.Board;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.Player;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class PlayBox extends Group {
    private Board board;
    private Player circle;
    private Player cross;

    public PlayBox(int tilesize, int CLICK_MODE, Controller cont) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: rgb(255, 255, 255);");

        board = new Board(tilesize, CLICK_MODE, cont);
        circle = new Player(PLAYER1, cont, tilesize, CLICK_MODE);
        cross = new Player(PLAYER2, cont, tilesize, CLICK_MODE);
        vbox.getChildren().addAll(cross, board, circle);
        getChildren().add(vbox);
        updateBounds();
    }

    public void update(State s) {
        board.update(s);
    }

    protected void layoutChildren() {
        super.layoutChildren();
    }

    public void addHighlight(int row, int col, String color) {
        BoardTile t = board.getTiles()[row][col];
        t.highlight(color);
    }

    public void addPiece(Move m) {
        BoardTile t = board.getTiles()[m.row][m.col];
        t.addPiece(m.team);
    }

    public void removeHighlight() {
        BoardTile[][] tiles = board.getTiles();
        for (BoardTile[] tile : tiles) {
            for (BoardTile aTile : tile) {
                if (aTile.isAction()) {
                    aTile.setMandatory(false);
                    aTile.setAction(false);
                }
            }
        }

    }

    public Board getBoard() {
        return board;
    }
}
