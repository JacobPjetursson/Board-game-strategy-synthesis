package fftlib.game;

import java.util.ArrayList;

public interface FFTDatabase {

    FFTMinimaxPlay queryPlay(FFTNode node);

    ArrayList<? extends FFTMove> nonLosingPlays(FFTNode node);
}
