package sim;

import fftlib.Action;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;

import java.util.HashSet;

import static fftlib.Literal.PIECEOCC_ANY;
import static fftlib.Literal.PIECEOCC_PLAYER;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class GameSpecifics implements FFTGameSpecifics {
    @Override
    public FFTMove actionToMove(Action a, int team) {
        if (a == null || (a.addClause.isEmpty() && a.remClause.isEmpty()))
            return null;
        int n1 = -1;
        int n2 = -1;
        for (Literal l : a.addClause.literals) {
            n1 = l.row;
            n2 = l.col;
        }
        return new Move(team, new Line(n1, n2));
    }

    @Override
    public FFTState preconsToState(HashSet<Literal> precons, int team) {
        State s = new State();
        int opponent = team == PLAYER1 ? PLAYER2 : PLAYER1;
        for (Literal l : precons) {
            if (l.boardPlacement && !l.negation && l.pieceOcc != PIECEOCC_ANY) {
                int entry;
                if (l.pieceOcc == PIECEOCC_PLAYER)
                    entry = team;
                else
                    entry = opponent;

                s.setLine(l.row, l.col, entry);
            }
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
            return "FFTs/sim_GGP_FFT.txt";
        return "FFTs/simFFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        return new int[] {6, 6};
    }

    @Override
    public String[] getPlayerNames() {
        return new String[0];
    }

    @Override
    public int[] getAllowedTransformations() {
        return new int[0];
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
        return null;
    }

    @Override
    public InteractiveFFTState getInteractiveState() {
        return null;
    }

    @Override
    public int getMaxPrecons() {
        return 15;
    }

    @Override
    public int getGameWinner() {
        return PLAYER2;
    }
}
