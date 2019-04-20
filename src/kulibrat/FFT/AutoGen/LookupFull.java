package kulibrat.FFT.AutoGen;

import kulibrat.ai.Minimax.Minimax;
import kulibrat.ai.Minimax.MinimaxPlay;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.misc.Database;


import java.util.HashMap;
import java.util.HashSet;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class LookupFull {
    private int team;
    private HashMap<State, MinimaxPlay> lookupTableFull;
    public static boolean BUILD_FULL_STATE_SPACE = false;

    private int CURR_MAX_DEPTH;
    private int unevaluatedNodes = 0;


    public LookupFull(int team) {
        BUILD_FULL_STATE_SPACE = true;
        this.team = team;
        lookupTableFull = new HashMap<>();
        HashSet<State> statespace = getStateSpace();

        BUILD_FULL_STATE_SPACE = false;
        for (State s : statespace)
            iterativeDeepeningMinimax(s);

        Database.setLookupTableFull(lookupTableFull);
    }

    private HashSet<State> getStateSpace() {
        HashSet<State> statespace = new HashSet<>();
        State initial_state = new State();
        State enemy_initial_state = new State();
        enemy_initial_state.setTurn(team == PLAYER1 ? PLAYER2 : PLAYER1);

        stateSpaceSearch(initial_state, statespace);
        stateSpaceSearch(enemy_initial_state, statespace);

        return statespace;
    }

    private void stateSpaceSearch(State state, HashSet<State> statespace) {
        if (Logic.gameOver(state) || statespace.contains(state))
            return;

        statespace.add(state);

        for (State child : state.getChildren())
            stateSpaceSearch(child, statespace);
    }

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    private MinimaxPlay iterativeDeepeningMinimax(State state) {
        CURR_MAX_DEPTH = 0;
        boolean done = false;
        MinimaxPlay play = null;
        int doneCounter = 0;
        while (!done) {
            State simState = new State(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            int prevSize = lookupTableFull.size();
            int prevUnevaluatedNodes = unevaluatedNodes;
            unevaluatedNodes = 0;
            CURR_MAX_DEPTH += 1;
            play = minimax(simState, CURR_MAX_DEPTH);
            if (lookupTableFull.size() == prevSize && unevaluatedNodes == prevUnevaluatedNodes) {
                doneCounter++;
            } else
                doneCounter = 0;
            if (doneCounter == 2) done = true;
        }
        return play;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private MinimaxPlay minimax(State state, int depth) {
        Move bestMove = null;
        int bestScore = (state.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (Logic.gameOver(state) || depth == 0) {
            return new MinimaxPlay(bestMove, heuristic(state), depth);
        }
        MinimaxPlay transpoPlay = lookupTableFull.get(state);
        if (transpoPlay != null && depth <= transpoPlay.depth) {
            return transpoPlay;
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
        if (transpoPlay == null || depth > transpoPlay.depth) {
            lookupTableFull.put(state,
                    new MinimaxPlay(bestMove, bestScore, depth));
        }
        if (!evaluated) unevaluatedNodes++;
        return new MinimaxPlay(bestMove, bestScore, depth);
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
