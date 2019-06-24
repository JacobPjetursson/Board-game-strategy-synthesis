package fftlib.game;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public interface FFTDatabase {

    FFTStateMapping queryState(FFTState state); // Can be uniform?

    ArrayList<? extends FFTMove> nonLosingMoves(FFTState state); // Uniform?

    boolean connectAndVerify(); // Not needed?

    // Used for Autogen
    HashMap<? extends FFTState, ? extends FFTStateMapping> getSolution(); // Uniform
}
