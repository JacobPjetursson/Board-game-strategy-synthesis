package tictactoe.FFT;

import tictactoe.ai.MinimaxPlay;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.HashMap;
import java.util.HashSet;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;
import static tictactoe.FFT.FFTAutoGen.INCLUDE_ILLEGAL_STATES;

public class LookupTableFullGen {
    private int team;
    private HashMap<State, MinimaxPlay> lookupTableAll;


    public LookupTableFullGen(int team) {
        this.team = team;
        lookupTableAll = new HashMap<>();
        for (State s : getStateSpace())
            minimax(s, 0);
        Database.fillLookupTableAll(lookupTableAll);
        INCLUDE_ILLEGAL_STATES = true;
    }

    private HashSet<State> getStateSpace() {
        INCLUDE_ILLEGAL_STATES = true;
        HashSet<State> statespace = new HashSet<>();
        State initial_state = new State();
        stateSpaceSearch(initial_state, statespace);
        INCLUDE_ILLEGAL_STATES = false;
        return statespace;
    }

    private void stateSpaceSearch(State state, HashSet<State> statespace) {
        if (Logic.gameOver(state) || statespace.contains(state))
            return;

        for (State child : state.getChildren())
            stateSpaceSearch(child, statespace);

        statespace.add(state);
    }

    private MinimaxPlay minimax(State state, int depth) {
        Move bestMove = null;
        int bestScore = (state.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (Logic.gameOver(state)) {
            return new MinimaxPlay(bestMove, heuristic(state), depth);
        }
        MinimaxPlay transpoPlay = lookupTableAll.get(state);
        if (transpoPlay != null) {
            return transpoPlay;
        }
        for (State child : state.getChildren()) {
            score = minimax(child, depth + 1).score;
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

        lookupTableAll.put(state, new MinimaxPlay(bestMove, bestScore, depth));
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
