package tictactoe.ai;

import fftlib.FFTSolution;
import fftlib.game.NodeMapping;
import tictactoe.game.Move;
import tictactoe.game.Node;

import static misc.Globals.PLAYER2;

public class PerfectPlayer implements AI {
    int team;

    public PerfectPlayer(int team) {
        this.team = team;
    }
    // This function fetches the best move from the DB, if it exists
    @Override
    public Move makeMove(Node node) {
        String teamstr = (team == PLAYER2) ? "Nought" : "Cross";
        System.out.println("Finding best play for " + teamstr);
        if (node.getLegalMoves().size() == 1) {
            return node.getLegalMoves().get(0);
        }
        // table lookup
        NodeMapping mapping = FFTSolution.queryNode(node);
        return (Move)mapping.move;
    }
}
