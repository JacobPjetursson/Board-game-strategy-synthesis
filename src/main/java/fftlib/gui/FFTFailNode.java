package fftlib.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTNodeAndMove;
import javafx.scene.Node;

import java.util.List;

public interface FFTFailNode {

    Node getFailNode(FFTNodeAndMove ps, List<? extends FFTMove> optimalMoves);
}
