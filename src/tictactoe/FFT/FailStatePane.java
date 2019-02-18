package tictactoe.FFT;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import fftlib.gui.FFTFailState;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.gui.PlayBox;
import tictactoe.gui.board.Board;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.Player;

import java.util.ArrayList;

import static misc.Config.CLICK_DISABLED;
import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class FailStatePane implements FFTFailState {

    Controller cont;

    FailStatePane(Controller cont) {
        this.cont = cont;
    }

    private PlayBox getFailStatePane(State s, Move move, ArrayList<Move> nonLosingPlays) {
        int tilesize = 60;
        PlayBox pb = new PlayBox(tilesize, CLICK_DISABLED, cont);
        pb.update(s);
        Platform.runLater(() -> {
            pb.addHighlight(move.row, move.col, BoardTile.blueStr);
            pb.addPiece(move);
            for (Move m : nonLosingPlays) {
                if (m.equals(move))
                    continue;
                pb.addHighlight(m.row, m.col, BoardTile.greenStr);
            }
        });
        return pb;
    }

    @Override
    public Node getFailState(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingPlays) {
        return getFailStatePane((State) ps.getState(), (Move) ps.getMove(), (ArrayList<Move>) nonLosingPlays);
    }
}
