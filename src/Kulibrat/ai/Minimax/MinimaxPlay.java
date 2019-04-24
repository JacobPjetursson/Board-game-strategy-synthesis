package kulibrat.ai.Minimax;

import fftlib.game.FFTMinimaxPlay;
import kulibrat.game.Move;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;
import static misc.Config.PLAYER_NONE;

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

    @Override
    public int getWinner() {
        if (score > 1000)
            return PLAYER1;
        else if (score > 0)
            return PLAYER_NONE;
        else if (score < -1000)
            return PLAYER2;
        return -1;
    }

    public int getScore() {
        return score;
    }

    public Move getMove() {
        return move;
    }
}
