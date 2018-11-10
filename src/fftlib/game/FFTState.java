package fftlib.game;

import fftlib.Clause;

import java.util.ArrayList;
import java.util.HashSet;

public interface FFTState {

    HashSet<Clause> getClauses();

    int getTurn();

    ArrayList<? extends FFTMove> getLegalMoves();

    ArrayList<? extends FFTState> getChildren();

    FFTState getNextState(FFTMove move);
}
