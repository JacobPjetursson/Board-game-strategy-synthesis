package kulibrat.FFT;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import kulibrat.game.Controller;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.misc.Database;
import misc.Config;

import java.util.ArrayList;

import static fftlib.Literal.PIECEOCC_ANY;
import static misc.Config.*;

public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public InteractiveState interactiveState;

    public GameSpecifics(Controller cont) {
        this.cont = cont;
        this.interactiveState = new InteractiveState(cont);
    }

    @Override
    public FFTMove actionToMove(Action a) {
        int newRow = -1;
        int newCol = -1;
        int oldRow = -1;
        int oldCol = -1;

        for (Literal l : a.addClause.literals) {
            newRow = l.row;
            newCol = l.col;
        }
        for (Literal l : a.remClause.literals) {
            oldRow = l.row;
            oldCol = l.col;
        }
        return new Move(oldRow, oldCol, newRow, newCol, -1);
    }

    @Override
    public FFTState clauseToState(Clause c) {
        State s = new State();

        for (Literal l : c.literals) {
            if (!l.boardPlacement) {
                String[] slSplit = l.name.toLowerCase().split("sl=");
                if (slSplit.length > 1) {
                    int sl = Integer.parseInt(slSplit[1]);
                    s.setScoreLimit(sl);
                }
            }
            else if (l.pieceOcc == PIECEOCC_ANY) {
                // TODO - Can be either of players
            } else {
                s.setBoardEntry(l.row, l.col, l.pieceOcc);
            }
        }
        return s;
    }

    @Override
    public String getFFTFilePath() {
        return "kulibratFFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        return new int[] {Config.kuliBHeight, Config.kuliBWidth};
    }

    @Override
    public int[] getSymmetries() {
        return new int[]{SYM_NONE, SYM_HREF};
    }

    @Override
    public FFTState getInitialState() {
        return new State();
    }

    @Override
    public FFTLogic getLogic() {
        return new Logic();
    }

    @Override
    public FFTDatabase getDatabase() {
        return new Database();
    }

    @Override
    public FFTFailState getFailState() {
        return new FailStatePane(cont);
    }

    @Override
    public InteractiveFFTState getInteractiveState() {
        return interactiveState;
    }
}
