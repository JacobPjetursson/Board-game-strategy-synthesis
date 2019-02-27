package fftlib.gui;

import fftlib.Action;
import fftlib.Rule;
import fftlib.game.FFTState;
import javafx.scene.Node;

public interface InteractiveFFTState {

    Node getInteractiveNode(FFTState fftState);

    Node getInteractiveNode(Rule r);

    Rule getRule();

    void setAction(Action a);

    void setPerspective(int team);

    int getPerspective();


    void clear();
}
