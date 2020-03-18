package fftlib.game;

import fftlib.FFTManager;

import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class FFTSolver{
    private static boolean solved = false;
    private static int CURR_MAX_DEPTH;
    private static int unevaluatedNodes = 0;
    private static int team = PLAYER1; // Always from player1 perspective
    private static HashMap<FFTState, StateMapping> lookupTable;

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    public static HashMap<? extends FFTState, StateMapping> solveGame(FFTState initialState) {
        if (solved) {
            System.out.println("Game already solved once, returning immediately");
            return lookupTable;
        }
        lookupTable = new HashMap<>();
        CURR_MAX_DEPTH = 0;
        boolean done = false;

        int doneCounter = 0;
        long timeStart = System.currentTimeMillis();
        while (!done) {
            initialState = initialState.clone(); // Start from fresh (Don't reuse previous game tree in new iterations)
            CURR_MAX_DEPTH += 1;
            int prevSize = lookupTable.size();
            int prevUnevaluatedNodes = unevaluatedNodes;
            unevaluatedNodes = 0;
            minimax(initialState, CURR_MAX_DEPTH);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH + ", LOOKUP TABLE SIZE: " + lookupTable.size());

            if (lookupTable.size() == prevSize && unevaluatedNodes == prevUnevaluatedNodes) {
                System.out.println("State space explored, and unevaluated nodes unchanged between runs");
                doneCounter++;
            } else {
                doneCounter = 0;
            }
            if (doneCounter == 2)
                done = true;
        }

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on solving game: " + timeSpent);
        int winner = lookupTable.get(initialState).getWinner();
        String winnerStr = winner == PLAYER1 ? "Player 1" : (winner == PLAYER2) ? "Player 2" : "Draw";
        System.out.println("Winner is " + winnerStr);
        solved = true;
        FFTSolution.setLookupTable(lookupTable);
        return lookupTable;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private static StateMapping minimax(FFTState state, int depth) {
        FFTMove bestMove = null;
        int bestScore = (state.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        boolean gameover = FFTManager.logic.gameOver(state);
        if (gameover || depth == 0) {
            return new StateMapping(bestMove, heuristic(state), depth, gameover);
        }
        StateMapping mapping = lookupTable.get(state);
        if (mapping != null && depth <= mapping.getDepth()) {
            return mapping;
        }
        boolean evaluated = true;
        for (FFTState child : state.getChildren()) {
            StateMapping childMapping = minimax(child, depth - 1);
            score = childMapping.getScore();
            if (!childMapping.evaluated)
                evaluated = false;
            if (childMapping.getWinner() == PLAYER1) score--;
            else score++;
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
        if (mapping == null || depth > mapping.getDepth()) {
            lookupTable.put(state,
                    new StateMapping(bestMove, bestScore, depth, evaluated));
        }
        if (!evaluated)
            unevaluatedNodes++;
        return new StateMapping(bestMove, bestScore, depth, evaluated);
    }

    // Heuristic function which values player1 with 2000 for a win, and -2000 for a loss. All other nodes are 0
    private static int heuristic(FFTState state) {
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        if (FFTManager.logic.gameOver(state)) {
            int winner = FFTManager.logic.getWinner(state);
            if (winner == team)
                return 2000;
            else if (winner == opponent)
                return -2000;
        }
        return 0;
    }
}
