package tictactoe.ai;

import fftlib.game.FFTMinimaxPlay;
import tictactoe.game.Move;

import static misc.Config.*;

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

    public int getScore() {
        return score;
    }

    public int getDepth() {
        return depth;
    }

    public Move getMove() {
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
}
