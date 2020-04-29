package kulibrat.FFT;

import fftlib.game.FFTMove;
import fftlib.game.FFTNodeAndMove;
import fftlib.gui.FFTFailNode;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.Node;
import kulibrat.gui.board.PlayBox.PlayBox;
import kulibrat.gui.board.PlayBox.StaticPlayBox;

import java.util.ArrayList;

import static misc.Globals.CLICK_DISABLED;

public class FailNodePane implements FFTFailNode {

    Controller cont;

    FailNodePane(Controller cont) {
        this.cont = cont;
    }

    private PlayBox getFailState(Node n, Move move, ArrayList<Move> optimalMoves) {
        int tilesize = 60;
        StaticPlayBox pb = new StaticPlayBox(tilesize, CLICK_DISABLED, cont);
        pb.update(n);

        pb.addArrow(move, Color.BLUE);
        for (Move m : optimalMoves) {
            if (m.equals(move))
                continue;
            pb.addArrow(m, Color.GREEN);
        }

        return pb;
    }

    @Override
    public javafx.scene.Node getFailNode(FFTNodeAndMove ps, ArrayList<? extends FFTMove> optimalMoves) {
        return getFailState((Node) ps.getNode(), (Move) ps.getMove(), (ArrayList<Move>) optimalMoves);
    }
}
