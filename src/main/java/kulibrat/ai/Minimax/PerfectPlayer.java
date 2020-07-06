package kulibrat.ai.Minimax;

import fftlib.game.FFTSolution;
import fftlib.game.NodeMapping;
import kulibrat.ai.AI;
import kulibrat.game.Move;
import kulibrat.game.Node;
import kulibrat.misc.Database;

import static misc.Config.USE_DB;
import static misc.Globals.PLAYER2;

public class PerfectPlayer extends AI {

    public PerfectPlayer(int team) {
        super(team);
        if (USE_DB)
            Database.connectAndVerify();
    }
    // This function fetches the best move from the DB, if it exists
    @Override
    public Move makeMove(Node node) {
        String teamstr = (team == PLAYER2) ? "BLACK" : "RED";
        System.out.println("Finding best play for " + teamstr);
        if (node.getLegalMoves().size() == 1) {
            return node.getLegalMoves().get(0);
        }
        // table lookup
        Node simNode = new Node(node);
        NodeMapping mapping;
        if (USE_DB) {
            mapping = Database.queryState(simNode);
        } else {
            mapping = FFTSolution.queryNode(simNode);
            mapping = null;
        }
        if (mapping == null) {
            System.err.println("DB Table is empty and needs to be rebuilt. Exiting");
            System.exit(0);
        }
        Move move = (Move)mapping.move;
        String winner = (mapping.score >= 1000) ? "RED" : (mapping.score == 0) ? "DRAW" : "BLACK";
        System.out.print("BEST PLAY:  " + "oldRow: " + move.oldRow +
                ", oldCol: " + move.oldCol + ", row: " + move.newRow + ", col: " + move.newCol +
                ", WINNER IS: " + winner);
        System.out.println(" in " + (mapping.score >= 1000 ? 2000 - mapping.score : (mapping.score == 0) ? "∞" : 2000 + mapping.score) + " moves!");
        return move;
    }
}
