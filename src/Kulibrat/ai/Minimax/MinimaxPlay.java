package kulibrat.ai.Minimax;

import kulibrat.game.Move;

// Class binds a move to a score for the minimax algorithm
public class MinimaxPlay {
    public int score;
    public Move move;
    public int depth;

    public MinimaxPlay(Move move, int score, int depth) {
        this.move = move;
        this.score = score;
        this.depth = depth;
    }
}
