package fftlib.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import javafx.scene.Node;

import java.util.ArrayList;

public interface FFTFailState {

    Node getFailState(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingPlays);
}
