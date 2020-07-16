package kulibrat.FFT;

import fftlib.logic.*;
import fftlib.auxiliary.Position;
import fftlib.game.*;
import fftlib.gui.FFTFailNode;
import fftlib.gui.interactiveFFTNode;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.SymmetryRule;
import kulibrat.game.Controller;
import kulibrat.game.Node;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;

public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public InteractiveNode interactiveNode;

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

    @Override
    public FFTMove actionToMove(Action a) {
        return null;
    }

    @Override
    public Action moveToAction(FFTMove m) {
        return null;
    }

    @Override
    public LiteralSet nodeToLiterals(FFTNode n) {
        return null;
    }

    @Override
    public PropRule gdlToRule(String precons, String action) {
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
    public FFTNode getInitialNode() {
        return new Node();
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
    public FFTFailNode getFailNode() {
        return new FailNodePane(cont);
    }

    @Override
    public interactiveFFTNode getInteractiveNode() {
        if (interactiveNode == null)
            this.interactiveNode = new InteractiveNode(cont);
        return interactiveNode;
    }

    @Override
    public HashSet<SymmetryRule> getSymmetryRules(PropRule propRule) {
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
    public LiteralSet getActionPreconditions(Action action) {
        return null;
    }

    @Override
    public int getMaxStateLiterals() {
        return 0;
    }

    @Override
    public ArrayList<Integer> legalIndices() {
        return null;
    }
}
