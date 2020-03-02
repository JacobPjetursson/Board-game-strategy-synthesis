package tictactoe.gui.board.PlayBox;

import fftlib.Rule;
import javafx.application.Platform;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.gui.board.BoardTile;

import static fftlib.Literal.*;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;
import static tictactoe.gui.board.BoardTile.blueStr;

public class InteractivePlayBox extends PlayBox {

    public InteractivePlayBox(int tilesize, int clickMode, Controller cont) {
        super(tilesize, clickMode, cont);
    }

    public void update(Rule r, int perspective) {
        int[][] preconBoard = Rule.preconsToBoard(r.preconditions);
        Move m = (Move) r.action.getMove(perspective);

        int enemy = (perspective == PLAYER1) ? PLAYER2 : PLAYER1;
        BoardTile[][] tiles = board.getTiles();

        for (int i = 0; i < preconBoard.length; i++) {
            for (int j = 0; j < preconBoard[i].length; j++) {
                if (m != null && m.row == i && m.col == j)
                    continue;
                int occ = preconBoard[i][j];
                BoardTile tile = tiles[i][j];
                tile.setMandatory(occ != 0);
                if (Math.abs(occ) == PIECEOCC_PLAYER)
                    tile.addPiece(perspective);
                else if (Math.abs(occ) == PIECEOCC_ENEMY)
                    tile.addPiece(enemy);


                if (occ == -PIECEOCC_ANY)
                    tile.setNegated(false);
                else if (occ == PIECEOCC_ANY)
                    tile.setNegated(true);
                else
                    tile.setNegated(occ < 0);
            }
        }

        Platform.runLater(() -> {
            removeHighlights();
            if (m != null) {
                addHighlight(m.row, m.col, perspective, blueStr);
            }
        });


    }
}
