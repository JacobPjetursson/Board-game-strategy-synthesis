package tictactoe.ai;

import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class LookupTableMinimax implements AI {
    private int CURR_MAX_DEPTH;
    private int unevaluatedNodes = 0;
    private int team;
    private HashMap<State, StateMapping> lookupTable;


    public LookupTableMinimax(int team, State state) {
        long timeStart = System.currentTimeMillis();
        this.team = team;
        lookupTable = new HashMap<>();
        iterativeDeepeningMinimax(state);
        Database.fillLookupTable(lookupTable);

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on solving game: " + timeSpent);
    }

    // This function fetches the best move from lookuptable, if it exists
    public Move makeMove(State state) {
        State key = new State(state);
        StateMapping mapping = lookupTable.get(key);
        Move move = mapping.move;
        String winner = (mapping.score >= 1000) ? "PLAYER1" : (mapping.score <= -1000) ? "PLAYER2" : "DRAW";
        System.out.print("BEST PLAY:  " + "row: " + move.row + ", col: " + move.col +
                ", WINNER IS: " + winner);
        System.out.println(" in " + (mapping.score >= 1000 ? 2000 - mapping.score :
                (mapping.score <= -1000) ? 2000 + mapping.score : mapping.score) + " moves!");
        return move;
    }

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    private StateMapping iterativeDeepeningMinimax(State state) {
        CURR_MAX_DEPTH = 0;
        boolean done = false;
        StateMapping mapping = null;
        int doneCounter = 0;
        while (!done) {
            State simState = new State(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            int prevSize = lookupTable.size();
            int prevUnevaluatedNodes = unevaluatedNodes;
            unevaluatedNodes = 0;
            CURR_MAX_DEPTH += 1;
            mapping = minimax(simState, CURR_MAX_DEPTH);
            //System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH + ", LOOKUP TABLE SIZE: " + lookupTable.size() + ", UNEVALUATED NODES: " + unevaluatedNodes);
            if (lookupTable.size() == prevSize && unevaluatedNodes == prevUnevaluatedNodes) {
                //System.out.println("State space explored, and unevaluated nodes unchanged between runs. I'm done");
                doneCounter++;
            } else
                doneCounter = 0;
            if (doneCounter == 2) done = true;
        }
        return mapping;
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
        for (State child : state.getChildren()) {
            score = minimax(child, depth - 1).score;
            if (score > 1000) score--;
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
        if (mapping == null || depth > mapping.depth) {
            lookupTable.put(state,
                    new StateMapping(bestMove, bestScore, depth));
        }
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
}
