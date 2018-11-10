package tictactoe.ai;

import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.HashMap;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class LookupTableMinimax implements AI {
    private int CURR_MAX_DEPTH;
    private int unevaluatedNodes = 0;
    private int team;
    private HashMap<State, MinimaxPlay> lookupTable;


    public LookupTableMinimax(int team, tictactoe.game.State state) {
        this.team = team;
        lookupTable = new HashMap<>();
        iterativeDeepeningMinimax(state);
        Database.fillLookupTable(lookupTable);
    }

    // This function fetches the best move from lookuptable, if it exists
    public Move makeMove(tictactoe.game.State state) {
        State key = new State(state);
        MinimaxPlay play = lookupTable.get(key);
        Move move = play.move;
        String winner = (play.score >= 1000) ? "PLAYER1" : (play.score == 0) ? "DRAW" : "PLAYER2";
        System.out.print("BEST PLAY:  " + "row: " + move.row + ", col: " + move.col +
                ", WINNER IS: " + winner);
        System.out.println(" in " + (play.score >= 1000 ? 2000 - play.score : (play.score == 0) ? "∞" : 2000 + play.score) + " moves!");
        return move;
    }

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    private MinimaxPlay iterativeDeepeningMinimax(tictactoe.game.State state) {
        CURR_MAX_DEPTH = 0;
        boolean done = false;
        MinimaxPlay play = null;
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
                String player = (team == PLAYER1) ? "PLAYER1" : "PLAYER2";
                String opponent = (player.equals("PLAYER1")) ? "PLAYER2" : "PLAYER1";
                String winner = (play.score >= 1000) ? player : opponent;
                System.out.println("A SOLUTION HAS BEEN FOUND, WINNING STRAT GOES TO: " + winner);
            }
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
        MinimaxPlay transpoPlay = lookupTable.get(state);
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
            lookupTable.put(state,
                    new MinimaxPlay(bestMove, bestScore, depth));
        }
        if (!evaluated) unevaluatedNodes++;
        return new MinimaxPlay(bestMove, bestScore, depth);
    }

    // Heuristic function which values red with 2000 for a win, and -2000 for a loss. All other nodes are 0
    private int heuristic(tictactoe.game.State state) {
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
