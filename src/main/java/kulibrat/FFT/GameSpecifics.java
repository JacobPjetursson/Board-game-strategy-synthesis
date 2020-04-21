package kulibrat.FFT;

import fftlib.Action;
import fftlib.auxiliary.Position;
import fftlib.Rule;
import fftlib.SymmetryRule;
import fftlib.game.FFTGameSpecifics;
import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import kulibrat.game.Controller;
import kulibrat.game.Logic;
import kulibrat.game.State;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;

public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public InteractiveState interactiveState;

    public GameSpecifics(Controller cont) {
        this.cont = cont;
    }

    public GameSpecifics() {

    }

/*    public FFTMove actionToMove(Action a) {
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

 */

    @Override // TODO
    public FFTMove actionToMove(Action a) {
        return null;
    }

    @Override
    public Action moveToAction(FFTMove m) {
        return null;
    }

    @Override // TODO
    public Rule gdlToRule(String precons, String action) {
        return null;
    }

    @Override
    public String getFFTFilePath() {
        return "FFTs/kulibratFFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        return new int[] {Config.BHEIGHT, Config.BWIDTH};
    }

    @Override
    public String[] getPlayerNames() {
        return new String[]{"Red", "Black"};
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
    public ArrayList<Integer> getGameAtoms() {
        return null;
    }

    @Override
    public String getAtomName(int atom) {
        return null;
    }

    @Override
    public int getAtomId(String name) {
        return 0;
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
    public HashSet<SymmetryRule> getSymmetryRules(Rule rule) {
        return null;
    }

    @Override
    public int posToId(Position pos) {
        return 0;
    }

    @Override
    public Position idToPos(int id) {
        return null;
    }

    @Override
    public HashSet<Long> getCoveredStateBitCodes(Rule rule) {
        return null;
    }

    @Override
    public long getNumberOfCoveredStates(Rule rule) {
        return 0;
    }
}
