package tictactoe.gui.board.PlayBox;

import fftlib.logic.rule.Rule;
import tictactoe.game.Controller;

public class InteractivePlayBox extends PlayBox {

    public InteractivePlayBox(int tilesize, int clickMode, Controller cont) {
        super(tilesize, clickMode, cont);
    }

    // TODO
    public void update(Rule r) {
        /*
        int[][] preconBoard = Transform.preconsToBoard(r.preconditions);
        Move m = (Move) r.action.getMove();

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

         */
    }
}
