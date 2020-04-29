package tictactoe.FFT;

import fftlib.game.FFTMove;
import fftlib.game.FFTNodeAndMove;
import fftlib.gui.FFTFailNode;
import javafx.application.Platform;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.Node;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.PlayBox.PlayBox;

import java.util.ArrayList;

import static misc.Globals.CLICK_DISABLED;

public class FailNodePane implements FFTFailNode {

    Controller cont;

    FailNodePane(Controller cont) {
        this.cont = cont;
    }

    private PlayBox getFailNodePane(Node n, Move move, ArrayList<Move> optimalMoves) {
        int tilesize = 60;
        PlayBox pb = new PlayBox(tilesize, CLICK_DISABLED, cont);
        pb.update(n);
        Platform.runLater(() -> {
            pb.addHighlight(move.row, move.col, move.team, BoardTile.blueStr);
            for (Move m : optimalMoves) {
                if (m.equals(move))
                    continue;
                pb.addHighlight(m.row, m.col, m.team, BoardTile.greenStr);
            }
        });
        return pb;
    }

    @Override
    public javafx.scene.Node getFailNode(FFTNodeAndMove ps, ArrayList<? extends FFTMove> optimalMoves) {
        return getFailNodePane((Node) ps.getNode(), (Move) ps.getMove(), (ArrayList<Move>) optimalMoves);
    }
}
