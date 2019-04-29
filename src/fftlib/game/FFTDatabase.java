package fftlib.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public interface FFTDatabase {

    FFTMinimaxPlay queryState(FFTState state);

    ArrayList<? extends FFTMove> nonLosingMoves(FFTState state);

    boolean connectAndVerify();

    boolean isLosingChild(FFTMinimaxPlay bestPlay, FFTState bestChild, FFTState state);

    // Used for Autogen
    HashMap<? extends FFTState, ? extends FFTMinimaxPlay> getSolution();
}
