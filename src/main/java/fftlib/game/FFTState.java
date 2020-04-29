package fftlib.game;

import fftlib.Literal;

import java.util.ArrayList;
import java.util.HashSet;

public interface FFTState {

    /**
     *
     * @return Set of positive literals that uniquely defines this state
     * Negative literals are not included
     */
    LiteralSet getLiterals();

    /**
     *
     * @return Set of all literals where negative literals are included
     * This function is used when creating rules based on states
     */
    LiteralSet getAllLiterals();

    long getZobristKey();

    int getTurn();

    ArrayList<? extends FFTMove> getLegalMoves();

    ArrayList<? extends FFTState> getChildren();

    FFTState getNextState(FFTMove move);

    FFTMove getMove();

    void addReachableParent(FFTState parent);

    void removeReachableParent(FFTState parent);

    HashSet<? extends FFTState> getReachableParents();

    long getBitString(); // TODO could be abstract

    boolean isReachable();

    FFTState clone();
}
