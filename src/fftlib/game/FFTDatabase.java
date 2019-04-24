package fftlib.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public interface FFTDatabase {

    FFTMinimaxPlay queryPlay(FFTState state);

    ArrayList<? extends FFTMove> nonLosingMoves(FFTState state);

    boolean connectAndVerify();

    // Used for Autogen
    HashMap<? extends FFTState, ? extends FFTMinimaxPlay> getSolution();
}
