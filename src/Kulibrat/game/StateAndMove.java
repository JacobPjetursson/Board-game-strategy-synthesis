package game;

public class StateAndMove {
    private State state;
    private Move move;
    private int turnNo;
    public boolean random;

    public StateAndMove(State state, Move move, int turnNo) {
        this.state = state;
        this.move = move;
        this.turnNo = turnNo;
    }

    // used for fft
    public StateAndMove(State state, Move move, boolean random) {
        this.state = state;
        this.move = move;
        this.random = random;
    }

    public int getTurnNo() {
        return turnNo;
    }

    public Move getMove() {
        return move;
    }

    public State getState() {
        return state;
    }
}
