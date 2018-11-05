package fftlib.game;

import java.util.ArrayList;

public interface FFTNode {

    FFTState getState();

    ArrayList<? extends FFTNode> getChildren();

    FFTNode getNextNode(FFTMove move);
}
