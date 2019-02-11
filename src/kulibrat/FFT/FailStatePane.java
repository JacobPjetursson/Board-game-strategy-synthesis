package kulibrat.FFT;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import fftlib.gui.FFTFailState;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.PlayBox;
import kulibrat.gui.board.Board;
import kulibrat.gui.board.Goal;
import kulibrat.gui.board.Player;

import java.util.ArrayList;

import static misc.Config.CLICK_DISABLED;
import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class FailStatePane implements FFTFailState {

    Controller cont;

    FailStatePane(Controller cont) {
        this.cont = cont;
    }

    private PlayBox getFailState(State s, Move move, ArrayList<Move> nonLosingPlays) {
        int tilesize = 60;
        PlayBox pb = new PlayBox(tilesize, CLICK_DISABLED, cont);
        pb.update(s);
        Platform.runLater(() -> {
            pb.addArrow(move, Color.BLUE);
            for (Move m : nonLosingPlays) {
                if (m.equals(move))
                    continue;
                pb.addArrow(m, Color.GREEN);
            }
        });
        pb.addScore(s.getScoreLimit(), s.getScore(PLAYER1), s.getScore(PLAYER2), false);
        return pb;
    }

    @Override
    public Node getFailState(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingPlays) {
        return getFailState((State) ps.getState(), (Move) ps.getMove(), (ArrayList<Move>) nonLosingPlays);
    }
}
