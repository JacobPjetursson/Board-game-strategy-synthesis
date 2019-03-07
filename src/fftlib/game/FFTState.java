package fftlib.game;

import fftlib.Literal;

import java.util.ArrayList;
import java.util.HashSet;

public interface FFTState {

    HashSet<Literal> getLiterals();

    HashSet<Literal> getAllLiterals();

    int getTurn();

    ArrayList<? extends FFTMove> getLegalMoves();

    ArrayList<? extends FFTState> getChildren();

    FFTState getNextState(FFTMove move);
}