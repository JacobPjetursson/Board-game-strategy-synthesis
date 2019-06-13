package tictactoe.FFT;

import fftlib.Action;
import fftlib.Literal;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;
import tictactoe.game.Controller;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.HashSet;

import static fftlib.Literal.PIECEOCC_ANY;
import static fftlib.Literal.PIECEOCC_PLAYER;
import static fftlib.game.Transform.*;
import static misc.Config.*;


public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public InteractiveState interactiveState;

    public GameSpecifics(Controller cont) {
        this.cont = cont;
    }

    @Override
    public FFTMove actionToMove(Action a, int team) {
        if (a == null || (a.addClause.isEmpty() && a.remClause.isEmpty()))
            return null;
        int row = -1;
        int col = -1;
        for (Literal l : a.addClause.literals) {
            row = l.row;
            col = l.col;
        }
        return new Move(row, col, team);
    }

    @Override
    public FFTState preconsToState(HashSet<Literal> literals, int team) {
        State s = new State();
        int opponent = team == PLAYER1 ? PLAYER2 : PLAYER1;
        for (Literal l : literals)
            if (l.boardPlacement && !l.negation && l.pieceOcc != PIECEOCC_ANY) {
                int entry;
                if (l.pieceOcc == PIECEOCC_PLAYER)
                    entry = team;
                else
                    entry = opponent;

                s.setBoardEntry(l.row, l.col, entry);
            }
        s.setTurn(team);
        return s;
    }

    @Override
    public String getFFTFilePath() {
        return "tictactoeFFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        return new int[] {3, 3};
    }

    @Override
    public String[] getPlayerNames() {
        return new String[]{"Cross", "Circle"};
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
            interactiveState = new InteractiveState(cont);
        return interactiveState;
    }

    @Override
    public int getMaxPrecons() {
        int[] dim = getBoardDim();
        return dim[0] * dim[1];
    }

    @Override
    public int getGameWinner() {
        if (Config.simpleTicTacToe)
            return PLAYER1;
        return PLAYER_NONE;
    }
}
