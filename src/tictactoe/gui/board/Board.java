package tictactoe.gui.board;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import tictactoe.game.State;

public class Board extends GridPane {
    private static final int boardRows = 3;
    private static final int boardColumns = 3;
    private BoardTile[][] tiles;
    private int tilesize;
    private int pieceRadius;

    public Board(int tilesize, int pieceRadius) {
        this.tilesize = tilesize;
        this.pieceRadius = pieceRadius;
        setAlignment(Pos.CENTER);
        tiles = new BoardTile[boardRows][boardColumns];
        for (int i = 0; i < boardRows; i++) {
            for (int j = 0; j < boardColumns; j++) {
                BoardTile bt = new BoardTile(i, j, tilesize);
                add(bt, j, i);
                tiles[i][j] = bt;
            }
        }
    }

    public BoardTile[][] getTiles() {
        return tiles;
    }

    public int getTileSize() {
        return tilesize;
    }

    public void update(State state) {
        int[][] stateBoard = state.getBoard();
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                BoardTile tile = tiles[i][j];
                int stateTile = stateBoard[i][j];
                // moved to tile
                if (tile.getChildren().isEmpty() && stateTile != 0) {
                    tile.getChildren().add(new BoardPiece(stateTile, i, j, pieceRadius));
                }
                // moved from tile
                else if (!tile.getChildren().isEmpty() && stateTile == 0) {
                    tile.getChildren().remove(0);
                }
                // moved to tile already occupied
                else if (!tile.getChildren().isEmpty()) {
                    BoardPiece piece = (BoardPiece) tile.getChildren().get(0);
                    if (piece.getTeam() != stateTile) {
                        tile.getChildren().remove(piece);
                        tile.getChildren().add(new BoardPiece(stateTile, i, j, pieceRadius));
                    }
                }
            }
        }
    }
}
