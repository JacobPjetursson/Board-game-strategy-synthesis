package mens_morris;

import fftlib.Action;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.FFTGameSpecifics;
import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;

import java.util.HashSet;

import static fftlib.game.Transform.*;
import static mens_morris.Logic.POS_NONBOARD;
import static misc.Config.THREE_MENS;

public class GameSpecifics implements FFTGameSpecifics {
    @Override
    public FFTMove actionToMove(Action a, int team) {
        if (a == null || (a.addClause.isEmpty() && a.remClause.isEmpty()))
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
    public FFTState preconsToState(HashSet<Literal> precons, int team) {
        State s = new State();
        for (Literal l : precons) {
            s.setBoardEntry(l.row, l.col, l.pieceOcc);
        }
        s.setTurn(team);
        return s;
    }

    @Override
    public Rule gdlToRule(String precons, String action) {
        return null;
    }

    @Override
    public String getFFTFilePath() {
        if (Config.ENABLE_GGP_PARSER)
            return "FFTs/mens_morris_GGP_FFT.txt";
        return "FFTs/mens_morris_FFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        if (THREE_MENS) {
            return new int[] {3, 3};
        }
        return new int[] {5,5};
    }

    @Override
    public String[] getPlayerNames() {
        return new String[0];
    }

    @Override
    public int[] getAllowedTransformations() {
        return new int[]{TRANS_HREF, TRANS_VREF, TRANS_ROT};
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
    public FFTFailState getFailState() {
        return null;
    }

    @Override
    public InteractiveFFTState getInteractiveState() {
        return null;
    }

    @Override
    public int getMaxPrecons() {
        if (THREE_MENS)
            return 9+2;
        return 16+2;
    }
}
