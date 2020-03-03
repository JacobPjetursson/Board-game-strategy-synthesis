package fftlib.GGPAutogen;

import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

import static misc.Globals.*;

// Class binds a move to a score for the minimax algorithm
public class GGPMapping {
    public int score;
    public Move move;
    public int depth;
    public Role role;

    public GGPMapping(Move move, int score, int depth, Role role) {
        this.move = move;
        this.score = score;
        this.depth = depth;
        this.role = role;
    }

    public int getDepth() {
        return depth;
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

    public int getScore() {
        return score;
    }

    public Move getMove() {
        return move;
    }

    public Role getRole() {
        return role;
    }

    public String toString() {
        String winnerStr = getWinner() == PLAYER1 ? "Player 1" : (getWinner() == PLAYER2) ? "Player 2" : "Draw";
        return "Winner: " + winnerStr + ". Score: " + getScore() + ". Move: " + getMove() + ". Role: " + getRole();
    }
}
