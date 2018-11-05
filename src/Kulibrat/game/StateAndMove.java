package kulibrat.game;

public class StateAndMove {
    private State state;
    private Move move;
    private int turnNo;

    public StateAndMove(State state, Move move, int turnNo) {
        this.state = state;
        this.move = move;
        this.turnNo = turnNo;
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
