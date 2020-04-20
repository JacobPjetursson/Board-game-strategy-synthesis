package fftlib.game;

import fftlib.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;

import java.util.ArrayList;
import java.util.HashSet;

public interface FFTGameSpecifics {

    FFTMove actionToMove(Action a);

    Action moveToAction(FFTMove m);

    Rule gdlToRule(String precons, String action);

    String getFFTFilePath();

    int[] getBoardDim(); // row, col

    String[] getPlayerNames(); // VISUAL

    FFTState getInitialState();

    FFTLogic getLogic();

    ArrayList<Integer> getGameAtoms();

    String getAtomName(int atom);

    int getAtomId(String name);

    FFTFailState getFailState(); // VISUAL

    InteractiveFFTState getInteractiveState(); // VISUAL

    HashSet<SymmetryRule> getSymmetryRules(Rule rule);

    int posToId(Position pos);

    Position idToPos(int id);
}
