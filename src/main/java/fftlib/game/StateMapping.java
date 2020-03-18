package fftlib.game;

import static misc.Globals.*;

// Class binds a move to a score for the minimax algorithm
public class StateMapping {
    public int score;
    public FFTMove move;
    public int depth;
    boolean explored;

    public StateMapping(FFTMove move, int score, int depth) {
        this.move = move;
        this.score = score;
        this.depth = depth;
        this.explored = false;
    }

    public StateMapping(FFTMove move, int score, int depth, boolean explored) {
        this.move = move;
        this.score = score;
        this.depth = depth;
        this.explored = explored;
    }

    public int getScore() {
        return score;
    }

    public int getDepth() {
        return depth;
    }

    public FFTMove getMove() {
        return move;
    }

    public int getWinner() {
        if (score > 1000)
            return PLAYER1;
        else if (score > 0)
            return PLAYER_NONE;
        else if (score < -1000)
            return PLAYER2;
        return -1;
    }

    public String toString() {
        return move.toString() + " , SCORE: " + score;
    }
}
