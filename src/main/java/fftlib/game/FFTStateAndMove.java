package fftlib.game;

public class FFTStateAndMove {
    public boolean random;
    private FFTState state;
    private FFTMove move;

    // used for fft
    public FFTStateAndMove(FFTState state, FFTMove move, boolean random) {
        this.state = state;
        this.move = move;
        this.random = random;
    }

    public FFTMove getMove() {
        return move;
    }

    public FFTState getState() {
        return state;
    }

    public String toString() {
        return "state: " + state + " , sub-optimal move: " + move;
    }
}
