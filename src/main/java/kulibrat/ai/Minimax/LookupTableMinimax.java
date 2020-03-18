package kulibrat.ai.Minimax;

import fftlib.game.StateMapping;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;

import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;


public class LookupTableMinimax {
    private static int team = PLAYER1;
    private static int CURR_MAX_DEPTH;
    private static int unevaluatedNodes = 0;
    private static HashMap<Long, StateMapping> lookupTable;

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    public static HashMap<Long, StateMapping> solveGame(State state) {
        CURR_MAX_DEPTH = 0;
        lookupTable = new HashMap<>();
        boolean done = false;
        StateMapping play;
        int doneCounter = 0;
        while (!done) {
            State simState = new State(state); // Start from fresh (Don't reuse previous game tree in new iterations)
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
    private static StateMapping minimax(State state, int depth) {
        Move bestMove = null;
        int bestScore = (state.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (Logic.gameOver(state) || depth == 0) {
            return new StateMapping(bestMove, heuristic(state), depth);
        }
        StateMapping mapping = lookupTable.get(state.getZobristKey());
        if (mapping != null && depth <= mapping.depth) {
            return mapping;
        }
        boolean evaluated = true;
        for (State child : state.getChildren()) {
            score = minimax(child, depth - 1).score;
            if (score > 1000) score--;
            else if (score < -1000) score++;
            else evaluated = false;

            if (state.getTurn() == team) {
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
            lookupTable.put(state.getZobristKey(),
                    new StateMapping(bestMove, bestScore, depth));
        }
        if (!evaluated) unevaluatedNodes++;
        return new StateMapping(bestMove, bestScore, depth);
    }

    // Heuristic function which values red with 2000 for a win, and -2000 for a loss. All other nodes are 0
    private static int heuristic(State state) {
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        if (Logic.gameOver(state)) {
            int winner = Logic.getWinner(state);
            if (winner == team)
                return 2000;
            else if (winner == opponent)
                return -2000;
        }
        return 0;
    }
}
