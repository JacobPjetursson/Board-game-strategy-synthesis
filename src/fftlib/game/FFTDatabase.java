package fftlib.game;

import java.util.ArrayList;
import java.util.Iterator;

public interface FFTDatabase {

    FFTMinimaxPlay queryPlay(FFTState state);

    ArrayList<? extends FFTMove> nonLosingPlays(FFTState state);

    boolean connectAndVerify();
}
