package tictactoe.gui.board.PlayBox;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.Node;
import tictactoe.gui.board.Board;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.Player;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class PlayBox extends Group {
    Board board;
    private Player circle;
    private Player cross;
    int clickMode;

    public PlayBox(int tilesize, int clickMode, Controller cont) {
        this.clickMode = clickMode;
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: rgb(255, 255, 255);");

        board = new Board(tilesize, clickMode, cont);
        cross = new Player(PLAYER1, cont, tilesize, clickMode);
        circle = new Player(PLAYER2, cont, tilesize, clickMode);
        vbox.getChildren().addAll(circle, board, cross);
        getChildren().add(vbox);
        updateBounds();
    }

    public void update(Node n) {
        board.update(n);
    }

    protected void layoutChildren() {
        super.layoutChildren();
    }

    public void addHighlight(int row, int col, int team, String color) {
        BoardTile t = board.getTiles()[row][col];
        t.addHighlight(color, team);
    }

    void removeHighlights() {
        for (BoardTile[] bt : board.getTiles())
            for (BoardTile aTile : bt)
                aTile.removeHighlight();
    }

    public void addPiece(Move m) {
        BoardTile t = board.getTiles()[m.row][m.col];
        t.drawPiece(m.team);
    }

    public Board getBoard() {
        return board;
    }
}
