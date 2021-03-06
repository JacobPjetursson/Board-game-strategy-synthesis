package tictactoe.gui.board.PlayBox;

import fftlib.FFTManager;
import fftlib.auxiliary.Position;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.Rule;
import javafx.application.Platform;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.gui.board.BoardTile;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class InteractivePlayBox extends PlayBox {

    public InteractivePlayBox(int tilesize, int clickMode, Controller cont) {
        super(tilesize, clickMode, cont);
    }

    public void update(Rule r) {
        // start by resetting
        clear();

        BoardTile[][] tiles = board.getTiles();
        for (Literal l : r.getPreconditions()) {
            Position pos = FFTManager.getPosFromId.apply(l.id);
            boolean negated = pos.occ < 0;
            BoardTile tile = tiles[pos.row][pos.col];

            // if an atom appears for both players for same cell, and this one is negated, then skip
            Position otherPos = Position.getOtherPlayerPos(pos);
            Literal otherPlayerLit = new PropLiteral(FFTManager.getIdFromPos.apply(otherPos));
            otherPlayerLit.setNegated(false);
            if (r.getPreconditions().contains(otherPlayerLit))
                continue;

            // if an atom appears for both players for same cell, we only set that cell once
            // in that case, the cell is set with an occ of 0
            otherPlayerLit = new PropLiteral(FFTManager.getIdFromPos.apply(otherPos));
            if (r.getPreconditions().contains(otherPlayerLit))
                tile.set(0, negated);
            else
                tile.set(Math.abs(pos.occ), negated);

        }

        // set action
        Move m = (Move) r.getAction().convert();
        Platform.runLater(() -> {
            if (m != null) {
                board.getTiles()[m.row][m.col].setAction(m.team);
            }
        });
    }

    public void clear() {
        for (BoardTile[] bt : board.getTiles()) {
            for (BoardTile aTile : bt) {
                aTile.clear();
            }
        }
    }
}
