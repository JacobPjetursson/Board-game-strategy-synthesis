package fftlib.game;

import java.util.ArrayList;
import java.util.HashMap;

public interface FFTDatabase {

    FFTStateMapping queryState(FFTState state); // Can be uniform?

    ArrayList<? extends FFTMove> nonLosingMoves(FFTState state); // Uniform?

    boolean connectAndVerify(); // Not needed?

    // Used for Autogen
    HashMap<? extends FFTState, ? extends FFTStateMapping> getSolution(); // Uniform
}
