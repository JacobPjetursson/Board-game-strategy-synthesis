package fftlib.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;

import java.util.ArrayList;

public interface FFTFailState {

    javafx.scene.Node getFailState(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingPlays);
}
