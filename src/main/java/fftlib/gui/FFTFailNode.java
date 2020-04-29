package fftlib.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTNodeAndMove;
import javafx.scene.Node;

import java.util.ArrayList;

public interface FFTFailNode {

    Node getFailNode(FFTNodeAndMove ps, ArrayList<? extends FFTMove> optimalMoves);
}
