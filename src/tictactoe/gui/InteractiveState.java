package tictactoe.gui;

import fftlib.game.FFTState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import tictactoe.game.State;

public class InteractiveState implements InteractiveFFTState {

    public PlayBox getInteractiveState(State s) {
        return null;
    }

    @Override
    public Node getInteractiveFFTState(FFTState fftState) {
        return getInteractiveState((State) fftState);
    }
}
