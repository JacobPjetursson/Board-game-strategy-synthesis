package mens_morris;

import fftlib.*;
import fftlib.auxiliary.Position;
import fftlib.game.FFTGameSpecifics;
import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;

import static misc.Config.THREE_MENS;

public class GameSpecifics implements FFTGameSpecifics {
    @Override
    public FFTMove actionToMove(Action a) { // TODO
        return null;
    }

    @Override
    public Action moveToAction(FFTMove m) {
        return null;
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
        return null;
    }

    @Override
    public InteractiveFFTState getInteractiveState() {
        return null;
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
