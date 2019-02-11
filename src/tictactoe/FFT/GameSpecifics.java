package tictactoe.FFT;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Literal;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import tictactoe.game.Controller;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.gui.FailStatePane;
import tictactoe.gui.InteractiveState;
import tictactoe.misc.Database;


import static misc.Config.*;


public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;

    public GameSpecifics(Controller cont) {
        this.cont = cont;
    }

    @Override
    public FFTMove actionToMove(Action a) {
        int row = -1;
        int col = -1;
        for (Literal l : a.addClause.literals) {
            row = l.row;
            col = l.col;
        }
        return new Move(row, col, -1);
    }

    @Override
    public FFTState clauseToState(Clause c) {
        return null;
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
    public int[] getSymmetries() {
        return new int[]{SYM_NONE, SYM_HREF, SYM_VREF, SYM_HVREF, SYM_ROT, SYM_HREF_ROT, SYM_VREF_ROT, SYM_HVREF_ROT};
    }

    @Override
    public FFTState getInitialState() {
        return new State(cont.getState());
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
        return new InteractiveState();
    }
}
