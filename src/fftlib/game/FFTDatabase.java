package fftlib.game;

import java.util.ArrayList;
import java.util.HashMap;

public interface FFTDatabase {

    FFTStateMapping queryState(FFTState state);

    ArrayList<? extends FFTMove> nonLosingMoves(FFTState state);

    boolean connectAndVerify();

    boolean isLosingChild(FFTStateMapping info, FFTState bestChild, FFTState state);

    // Used for Autogen
    HashMap<? extends FFTState, ? extends FFTStateMapping> getSolution();
}
