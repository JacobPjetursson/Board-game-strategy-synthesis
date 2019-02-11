package tictactoe.gui;

import fftlib.Rule;
import fftlib.game.FFTState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;

public class InteractiveState implements InteractiveFFTState {

    @Override
    public Node getInteractiveNode(FFTState fftState) {
        return null;
    }

    @Override
    public Node getInteractiveNode(Rule r) {
        return null;
    }

    @Override
    public Rule getRule() {
        return null;
    }

    @Override
    public void setPerspective(int team) {

    }

    @Override
    public void clear() {

    }
}
