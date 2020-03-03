package tictactoe.gui.board;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import tictactoe.game.Controller;
import tictactoe.game.State;

public class Board extends GridPane {
    private static final int boardRows = 3;
    private static final int boardColumns = 3;
    private BoardTile[][] tiles;

    public Board(int tilesize, int clickMode, Controller cont) {
        setAlignment(Pos.CENTER);
        tiles = new BoardTile[boardRows][boardColumns];
        for (int i = 0; i < boardRows; i++) {
            for (int j = 0; j < boardColumns; j++) {
                BoardTile bt = new BoardTile(i, j, tilesize, clickMode, cont);
                add(bt, j, i);
                tiles[i][j] = bt;
            }
        }
    }

    public BoardTile[][] getTiles() {
        return tiles;
    }

    public void update(State state) {
        int[][] stateBoard = state.getBoard();
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                BoardTile tile = tiles[i][j];
                int stateTile = stateBoard[i][j];
                // new piece added
                if (tile.getPiece() == null && stateTile != 0) {
                    tile.addPiece(stateTile);
                }
                tile.update();
            }
        }
    }
}
