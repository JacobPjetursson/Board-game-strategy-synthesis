package fftlib.gui;

import fftlib.logic.Action;
import fftlib.logic.rule.Rule;
import fftlib.game.FFTNode;
import javafx.scene.Node;

public interface interactiveFFTNode {

    Node getInteractiveNode(FFTNode fftNode);

    Node getInteractiveNode(Rule r);

    Rule getRule();

    void setAction(Action a);

    void clear();
}
