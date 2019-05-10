package kulibrat.FFT.AutoGen;

import kulibrat.ai.Minimax.StateMapping;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.misc.Database;

import java.util.HashMap;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


public class LookupSimple {
    private int CURR_MAX_DEPTH;
    private int unevaluatedNodes = 0;
    private HashMap<State, StateMapping> lookupTable;
    private int team;

    public LookupSimple(int team, State state) {
        System.out.println("Solving the game");
        this.team = team;
        lookupTable = new HashMap<>();
        long timeStart = System.currentTimeMillis();
        buildLookupTable(state);
        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on solving game: " + timeSpent);
    }

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    private StateMapping iterativeDeepeningMinimax(State state) {
        CURR_MAX_DEPTH = 0;
        boolean done = false;
        StateMapping play = null;
        int doneCounter = 0;
        while (!done) {
            CURR_MAX_DEPTH += 1;
            System.out.print("At depth " + CURR_MAX_DEPTH);
            State simState = new State(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            int prevSize = lookupTable.size();
            int prevUnevaluatedNodes = unevaluatedNodes;
            unevaluatedNodes = 0;
            play = minimax(simState, CURR_MAX_DEPTH);
            if (lookupTable.size() == prevSize && unevaluatedNodes == prevUnevaluatedNodes) {
                doneCounter++;
                System.out.print(": game solved");
            } else
                doneCounter = 0;
            if (doneCounter == 2) done = true;
            System.out.println();
        }
        return play;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private StateMapping minimax(State state, int depth) {
        Move bestMove = null;
        int bestScore = (state.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (Logic.gameOver(state) || depth == 0) {
            return new StateMapping(bestMove, heuristic(state), depth);
        }
        StateMapping mapping = lookupTable.get(state);
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
            lookupTable.put(state,
                    new StateMapping(bestMove, bestScore, depth));
        }
        if (!evaluated) unevaluatedNodes++;
        return new StateMapping(bestMove, bestScore, depth);
    }

    // Heuristic function which values red with 2000 for a win, and -2000 for a loss. All other nodes are 0
    private int heuristic(State state) {
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

    // This function builds the lookup table from scratch
    private void buildLookupTable(State state) {
        iterativeDeepeningMinimax(state);
        Database.setLookupTable(lookupTable);
    }
}
