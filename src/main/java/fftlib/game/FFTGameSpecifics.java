package fftlib.game;

import fftlib.auxiliary.Position;
import fftlib.gui.FFTFailNode;
import fftlib.gui.InteractiveFFTNode;
import fftlib.logic.rule.Action;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.SymmetryRule;

import java.util.ArrayList;
import java.util.HashSet;

public interface FFTGameSpecifics {

    String getFFTFilePath(); // Misc

    int[] getBoardDim(); // Domain specific
    FFTNode getInitialNode(); // Domain specific
    HashSet<SymmetryRule> getSymmetryRules(PropRule propRule); // Domain specific
    int getMaxStateLiterals(); // Domain specific
    ArrayList<Integer> legalIndices(); // Domain specific

    FFTMove actionToMove(Action a); // Interface
    Action moveToAction(FFTMove m); // Interface
    LiteralSet nodeToLiterals(FFTNode n); // Interface
    PropRule gdlToRule(String precons, String action); // Interface

    String[] getPlayerNames(); // Visual
    FFTFailNode getFailNode(); // Visual
    InteractiveFFTNode getInteractiveNode(); // Visual

    ArrayList<Integer> getGameAtoms(); // Logic
    String getAtomName(int atom); // Logic
    int getAtomId(String name); // Logic
    int posToId(Position pos); // Logic
    Position idToPos(int id); // Logic
    LiteralSet getActionPreconditions(Action action); // Logic
}
