package fftlib.gui;

import fftlib.Rule;
import fftlib.game.FFTState;
import javafx.scene.Node;

public interface InteractiveFFTState {

    Node getInteractiveNode(FFTState fftState);

    Node getInteractiveNode(Rule r);

    Rule getRule();

    void setPerspective(int team);

    void clear();
}
