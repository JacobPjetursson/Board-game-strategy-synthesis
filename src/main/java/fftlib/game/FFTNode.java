package fftlib.game;

import fftlib.FFTManager;

import java.util.ArrayList;

public abstract class FFTNode {
    public int turn;

    public int getTurn() {
        return turn;
    }

    public State getState() {
        return FFTManager.nodeToState.apply(this);
    }

    public abstract ArrayList<? extends FFTMove> getLegalMoves();

    public abstract ArrayList<? extends FFTNode> getChildren();

    public abstract FFTNode getNextNode(FFTMove move);

    /**
     *
     * @return The move used by the parent to expand into this node
     */
    public abstract FFTMove getMove();
}
