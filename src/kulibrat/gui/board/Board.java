package kulibrat.gui.board;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import kulibrat.game.Controller;
import kulibrat.game.State;
import misc.Config;
import misc.Globals;

public class Board extends GridPane {
    private static final int boardRows = Config.BHEIGHT;
    private static final int boardColumns = Config.BWIDTH;
    private BoardTile[][] tiles;
    private int pieceRadius;
    private int clickMode;

    public Board(int tilesize, int pieceRadius, int clickMode, Controller cont) {
        this.pieceRadius = pieceRadius;
        this.clickMode = clickMode;
        setAlignment(Pos.CENTER);

        tiles = new BoardTile[boardRows][boardColumns];
        for (int i = 0; i < boardRows; i++) {
            for (int j = 0; j < boardColumns; j++) {
                BoardTile bt = new BoardTile(i, j, tilesize, clickMode, cont);
                tiles[i][j] = bt;
                add(bt, j, i);
            }
        }
    }

    public BoardTile[][] getTiles() {
        return tiles;
    }

    public void update(Controller cont, State state) {
        int[][] stateBoard = state.getBoard();
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                BoardTile tile = tiles[i][j];
                int stateTile = stateBoard[i][j];
                // moved to tile
                if (stateTile != 0) {
                    tile.addPiece(stateTile, clickMode);
                }
                // moved from tile
                else
                    tile.removePiece();

                tile.update();
            }
        }
    }
}
