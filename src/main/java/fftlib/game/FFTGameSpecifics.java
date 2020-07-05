package fftlib.game;

import fftlib.auxiliary.Position;
import fftlib.gui.FFTFailNode;
import fftlib.gui.interactiveFFTNode;
import fftlib.logic.Action;
import fftlib.logic.LiteralSet;
import fftlib.logic.Rule;
import fftlib.logic.SymmetryRule;

import java.util.ArrayList;
import java.util.HashSet;

public interface FFTGameSpecifics {

    String getFFTFilePath(); // Misc

    int[] getBoardDim(); // Domain specific
    FFTNode getInitialNode(); // Domain specific
    HashSet<SymmetryRule> getSymmetryRules(Rule rule); // Domain specific
    int getMaxStateLiterals(); // Domain specific
    ArrayList<Integer> legalIndices(); // Domain specific

    FFTMove actionToMove(Action a); // Interface
    Action moveToAction(FFTMove m); // Interface
    LiteralSet nodeToLiterals(FFTNode n); // Interface
    Rule gdlToRule(String precons, String action); // Interface

    String[] getPlayerNames(); // Visual
    FFTFailNode getFailNode(); // Visual
    interactiveFFTNode getInteractiveNode(); // Visual

    ArrayList<Integer> getGameAtoms(); // Logic
    String getAtomName(int atom); // Logic
    int getAtomId(String name); // Logic
    int posToId(Position pos); // Logic
    Position idToPos(int id); // Logic
    LiteralSet getActionPreconditions(Action action); // Logic



    /** Similar to getNumberOfCoveredStates, this function uses domain-specifc
     * knowledge to find the covered states given a rule with free preconditions.
     * The domain-free version computes the powerset of all free preconditions and
     * inserts each subset in the existing preconditions. This is unnecessarily costly
     * and can be replaced by domain-specific methods. Certain preconditions can not
     * co-exist such as P(0, 0) and E(0, 0) for Tic-tac-toe
     * @param rule
     * @return
     */
    HashSet<LiteralSet> getCoveredStates(Rule rule);

    /** Find the numer of covered states given a set of preconditions
     * It is quite easy to find an upper bound by using a domain-free method
     * of finding the powerset of all the free variables. For instance
     * for tic-tac-toe, the powerset will be 2^n, where n is the
     * number of free preconditions (s.t. empty board has powerset of size 2^18).
     * However, this doesn't use any of the game's limits to force down the bound.
     * A free cell can only have 3 values which can't co-exist, so we can instead
     * compute it as 3^n. This is domain-specific, which is why it's here.
     * @param rule
     * @return Upper bound for number of covered states as integer.
     */
    long getNumberOfCoveredStates(Rule rule);

}
