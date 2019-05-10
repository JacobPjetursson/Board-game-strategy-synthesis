package kulibrat.FFT;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import fftlib.gui.FFTFailState;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.board.PlayBox.PlayBox;
import kulibrat.gui.board.PlayBox.StaticPlayBox;

import java.util.ArrayList;

import static misc.Config.CLICK_DISABLED;

public class FailStatePane implements FFTFailState {

    Controller cont;

    FailStatePane(Controller cont) {
        this.cont = cont;
    }

    private PlayBox getFailState(State s, Move move, ArrayList<Move> nonLosingMoves) {
        int tilesize = 60;
        StaticPlayBox pb = new StaticPlayBox(tilesize, CLICK_DISABLED, cont);
        pb.update(s);

        pb.addArrow(move, Color.BLUE);
        for (Move m : nonLosingMoves) {
            if (m.equals(move))
                continue;
            pb.addArrow(m, Color.GREEN);
        }

        return pb;
    }

    @Override
    public Node getFailState(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingMoves) {
        return getFailState((State) ps.getState(), (Move) ps.getMove(), (ArrayList<Move>) nonLosingMoves);
    }
}
