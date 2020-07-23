package fftlib.game;

public class FFTNodeAndMove {
    public boolean random;
    private final FFTNode node;
    private final FFTMove move;

    // used for fft
    public FFTNodeAndMove(FFTNode node, FFTMove move, boolean random) {
        this.node = node;
        this.move = move;
        this.random = random;
    }

    public FFTNodeAndMove(FFTNode node, FFTMove move) {
        this.node = node;
        this.move = move;
        this.random = false;
    }

    public FFTNodeAndMove(FFTNodeAndMove nm) {
        this.node = nm.node.clone();
        this.move = nm.move.clone();
        this.random = nm.random;
    }

    public FFTMove getMove() {
        return move;
    }

    public FFTNode getNode() {
        return node;
    }

    public String toString() {
        return "state: " + node + " , sub-optimal move: " + move;
    }
}
