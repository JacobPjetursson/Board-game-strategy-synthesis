package fftlib.game;

import java.util.ArrayList;

public interface FFTDatabase {

    FFTMinimaxPlay queryPlay(FFTState state);

    ArrayList<? extends FFTMove> nonLosingPlays(FFTState state);

    boolean connectAndVerify();
}
