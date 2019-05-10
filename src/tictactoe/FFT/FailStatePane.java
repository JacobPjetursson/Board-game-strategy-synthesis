package tictactoe.FFT;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import fftlib.gui.FFTFailState;
import javafx.application.Platform;
import javafx.scene.Node;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.PlayBox.PlayBox;

import java.util.ArrayList;

import static misc.Config.CLICK_DISABLED;

public class FailStatePane implements FFTFailState {

    Controller cont;

    FailStatePane(Controller cont) {
        this.cont = cont;
    }

    private PlayBox getFailStatePane(State s, Move move, ArrayList<Move> nonLosingMoves) {
        int tilesize = 90;
        PlayBox pb = new PlayBox(tilesize, CLICK_DISABLED, cont);
        pb.update(s);
        Platform.runLater(() -> {
            pb.addHighlight(move.row, move.col, move.team, BoardTile.blueStr);
            for (Move m : nonLosingMoves) {
                if (m.equals(move))
                    continue;
                pb.addHighlight(m.row, m.col, m.team, BoardTile.greenStr);
            }
        });
        return pb;
    }

    @Override
    public Node getFailState(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingMoves) {
        return getFailStatePane((State) ps.getState(), (Move) ps.getMove(), (ArrayList<Move>) nonLosingMoves);
    }
}
