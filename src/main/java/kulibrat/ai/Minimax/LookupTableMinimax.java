package kulibrat.ai.Minimax;

import fftlib.game.NodeMapping;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.Node;

import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;


public class LookupTableMinimax {
    private static int team = PLAYER1;
    private static int CURR_MAX_DEPTH;
    private static int unevaluatedNodes = 0;
    private static HashMap<Long, NodeMapping> lookupTable;

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    public static HashMap<Long, NodeMapping> solveGame(Node node) {
        CURR_MAX_DEPTH = 0;
        lookupTable = new HashMap<>();
        boolean done = false;
        NodeMapping play;
        int doneCounter = 0;
        while (!done) {
            Node simState = new Node(node); // Start from fresh (Don't reuse previous game tree in new iterations)
            int prevSize = lookupTable.size();
            int prevUnevaluatedNodes = unevaluatedNodes;
            unevaluatedNodes = 0;
            CURR_MAX_DEPTH += 1;
            play = minimax(simState, CURR_MAX_DEPTH);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH + ", LOOKUP TABLE SIZE: " + lookupTable.size() + ", UNEVALUATED NODES: " + unevaluatedNodes);
            if (lookupTable.size() == prevSize && unevaluatedNodes == prevUnevaluatedNodes) {
                System.out.println("State space explored, and unevaluated nodes unchanged between runs. I'm done");
                doneCounter++;
            } else
                doneCounter = 0;
            if (doneCounter == 2) done = true;

            if (Math.abs(play.score) >= 1000) {
                String player = (team == PLAYER1) ? "RED" : "BLACK";
                String opponent = (player.equals("BLACK")) ? "RED" : "BLACK";
                String winner = (play.score >= 1000) ? player : opponent;
                System.out.println("A SOLUTION HAS BEEN FOUND, WINNING STRAT GOES TO: " + winner);
            }
        }
        return lookupTable;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private static NodeMapping minimax(Node node, int depth) {
        Move bestMove = null;
        int bestScore = (node.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (Logic.gameOver(node) || depth == 0) {
            return new NodeMapping(bestMove, heuristic(node), depth);
        }
        NodeMapping mapping = lookupTable.get(node.getZobristKey());
        if (mapping != null && depth <= mapping.depth) {
            return mapping;
        }
        boolean evaluated = true;
        for (Node child : node.getChildren()) {
            score = minimax(child, depth - 1).score;
            if (score > 1000) score--;
            else if (score < -1000) score++;
            else evaluated = false;

            if (node.getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = child.getMove();
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = child.getMove();
                }
            }
        }
        if (mapping == null || depth > mapping.depth) {
            lookupTable.put(node.getZobristKey(),
                    new NodeMapping(bestMove, bestScore, depth));
        }
        if (!evaluated) unevaluatedNodes++;
        return new NodeMapping(bestMove, bestScore, depth);
    }

    // Heuristic function which values red with 2000 for a win, and -2000 for a loss. All other nodes are 0
    private static int heuristic(Node node) {
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        if (Logic.gameOver(node)) {
            int winner = Logic.getWinner(node);
            if (winner == team)
                return 2000;
            else if (winner == opponent)
                return -2000;
        }
        return 0;
    }
}
