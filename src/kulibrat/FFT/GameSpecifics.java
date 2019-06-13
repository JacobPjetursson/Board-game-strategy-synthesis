package kulibrat.FFT;

import fftlib.Action;
import fftlib.Literal;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import kulibrat.game.Controller;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.misc.Database;
import misc.Config;

import java.util.HashSet;

import static fftlib.Literal.PIECEOCC_ANY;
import static fftlib.game.Transform.TRANS_HREF;
import static kulibrat.game.Logic.POS_NONBOARD;
import static misc.Config.*;

public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public InteractiveState interactiveState;

    public GameSpecifics(Controller cont) {
        this.cont = cont;
    }

    @Override
    public FFTMove actionToMove(Action a, int team) {
        if (a.actionErr || (a.addClause.isEmpty() && a.remClause.isEmpty()))
            return null;
        int newRow = POS_NONBOARD;
        int newCol = POS_NONBOARD;
        int oldRow = POS_NONBOARD;
        int oldCol = POS_NONBOARD;

        for (Literal l : a.addClause.literals) {
            newRow = l.row;
            newCol = l.col;
        }
        for (Literal l : a.remClause.literals) {
            oldRow = l.row;
            oldCol = l.col;
        }
        return new Move(oldRow, oldCol, newRow, newCol, team);
    }

    @Override
    public FFTState preconsToState(HashSet<Literal>literals, int team) {
        State s = new State();

        for (Literal l : literals) {
            if (!l.boardPlacement) {
                String[] slSplit = l.name.toUpperCase().split("SL=");
                if (slSplit.length > 1) {
                    int sl = Integer.parseInt(slSplit[1]);
                    s.setScoreLimit(sl);
                }
                String[] p1PointSplit = l.name.toUpperCase().split("P1SCORE=");
                if (p1PointSplit.length > 1) {
                    int p1Point = Integer.parseInt(p1PointSplit[1]);
                    s.setScore(PLAYER1, p1Point);
                }
                String[] p2PointSplit = l.name.toUpperCase().split("P2SCORE=");
                if (p2PointSplit.length > 1) {
                    int p2Point = Integer.parseInt(p2PointSplit[1]);
                    s.setScore(PLAYER2, p2Point);
                }
            }
            else if (l.pieceOcc == PIECEOCC_ANY) {
                // TODO - Can be either of players. This can not occur atm
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
    public String[] getPlayerNames() {
        return new String[]{"Red", "Black"};
    }

    @Override
    public int[] getAllowedTransformations() {
        return new int[]{TRANS_HREF};
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
        if (interactiveState == null)
            this.interactiveState = new InteractiveState(cont);
        return interactiveState;
    }

    @Override
    public int getMaxPrecons() {
        int[] dim = getBoardDim();
        return dim[0] * dim[1] + 2; // tiles, score, score
    }

    @Override
    public int getGameWinner() {
        if (Config.kuliBHeight == 3 || Config.SCORELIMIT < 8)
            return PLAYER1;
        return PLAYER_NONE;
    }
}
