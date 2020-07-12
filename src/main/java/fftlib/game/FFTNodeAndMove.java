package fftlib.game;

public class FFTNodeAndMove {
    public boolean random;
    private FFTNode node;
    private FFTMove move;

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
