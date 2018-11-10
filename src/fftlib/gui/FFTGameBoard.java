package fftlib.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;

import java.util.ArrayList;

public interface FFTGameBoard {

    javafx.scene.Node getGameBoard(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingPlays);
}
