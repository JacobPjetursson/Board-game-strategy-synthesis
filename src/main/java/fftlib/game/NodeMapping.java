package fftlib.game;

import static misc.Globals.*;

// Class binds a move to a score for the minimax algorithm
public class NodeMapping {
    public int score;
    public FFTMove move;
    public FFTNode node;
    public int depth;

    public NodeMapping(FFTNode node, FFTMove move, int score, int depth) {
        this.node = node;
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

    public FFTMove getMove() {
        return move;
    }

    public FFTNode getNode() {
        return node;
    }

    public int getWinner() {
        if (score > 1000)
            return PLAYER1;
        else if (score >= 0)
            return PLAYER_NONE;
        else if (score < -1000)
            return PLAYER2;
        return -1;
    }

    public String toString() {
        return move.toString() + " , SCORE: " + score + " , WINNER: " + getWinner();
    }
}
