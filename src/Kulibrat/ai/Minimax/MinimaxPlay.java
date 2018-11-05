package kulibrat.ai.Minimax;

import fftlib.game.FFTMinimaxPlay;
import kulibrat.game.Move;

// Class binds a move to a score for the minimax algorithm
public class MinimaxPlay implements FFTMinimaxPlay {
    public int score;
    public Move move;
    public int depth;

    public MinimaxPlay(Move move, int score, int depth) {
        this.move = move;
        this.score = score;
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public int getScore() {
        return score;
    }

    public Move getMove() {
        return move;
    }
}
