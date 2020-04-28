package fftlib.game;

import fftlib.*;
import fftlib.auxiliary.Position;
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

    LiteralSet getActionPreconditions(Action action);

    /** Similar to getNumberOfCoveredStates, this function uses domain-specifc
     * knowledge to find the covered states given a rule with free preconditions.
     * The domain-free version computes the powerset of all free preconditions and
     * inserts each subset in the existing preconditions. This is unnecessarily costly
     * and can be replaced by domain-specific methods. Certain preconditions can not
     * co-exist such as P(0, 0) and E(0, 0) for Tic-tac-toe
     * @param rule
     * @return
     */
    // TODO - can we do this smarter and more domain-independent?
    // TODO - does there exist a more general version of this that is less ad-hoc?
    HashSet<Long> getCoveredStateBitCodes(Rule rule);

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
