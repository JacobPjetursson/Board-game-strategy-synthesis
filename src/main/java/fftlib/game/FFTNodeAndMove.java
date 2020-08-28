package fftlib.game;

public class FFTNodeAndMove {
    private final FFTNode node;
    private final FFTMove move;

    public FFTNodeAndMove(FFTNode node, FFTMove move) {
        this.node = node;
        this.move = move;
    }

    public FFTMove getMove() {
        return move;
    }

    public FFTNode getNode() {
        return node;
    }

    public String toString() {
        return "state: " + node + " , move: " + move;
    }
}
