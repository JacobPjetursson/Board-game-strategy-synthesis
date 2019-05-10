package kulibrat.ai.Minimax;

import kulibrat.ai.AI;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;

import java.util.HashMap;
import java.util.Random;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class Minimax extends AI {
    private long calculationTime;
    private boolean searchCutOff;
    private int CURR_MAX_DEPTH;
    private boolean moveOrdering = true;
    private boolean useTranspo = true;
    private HashMap<Long, StateMapping> transTable;
    private State prevBestState;

    public Minimax(int team, int calculationTime) {
        super(team);
        this.calculationTime = calculationTime;
        transTable = new HashMap<>();
    }

    // Runs the iterative deepening minimax with a set timelimit
    public Move makeMove(kulibrat.game.State state) {
        long startTime = System.currentTimeMillis();
        if (state.getLegalMoves().size() == 1) {
            chill(startTime);
            return state.getLegalMoves().get(0);
        }
        Move move = iterativeDeepeningMinimax(state, startTime).move;
        // This happens when the minimax returns faster after having found a winning move
        chill(startTime);
        return move;
    }

    // Iteratively increases the depth limit while called minimax continuously. Stops when win is ensured or time is up.
    private StateMapping iterativeDeepeningMinimax(kulibrat.game.State state, long startTime) {
        resetVariables();
        StateMapping info = null;
        boolean winCutOff = false;
        while (!outOfTime(startTime) && !winCutOff) {
            State simState = new State(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            CURR_MAX_DEPTH++;
            StateMapping mapping = minimax(simState, CURR_MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime);
            if (!searchCutOff) info = mapping;
            if (Math.abs(mapping.score) >= 1000) winCutOff = true;
        }
        // random move if null (No time to calculate minimax)
        if (info == null) {
            int r = new Random().nextInt(state.getLegalMoves().size());
            info = new StateMapping(state.getLegalMoves().get(r), Integer.MIN_VALUE, 0);
        }
        System.out.println("Score: " + info.score + ", Depth: " + CURR_MAX_DEPTH + ", Play:  oldRow: " + info.move.oldRow + ", oldCol: " +
                info.move.oldCol + ", row: " + info.move.newRow + ", col: " + info.move.newCol + ", team: " + info.move.team);
        return info;
    }

    // Minimax with pruning, move ordering and a detailed heuristic
    public StateMapping minimax(State state, int depth, int alpha, int beta, long startTime) {
        Move bestMove = null;
        int bestScore = (state.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (outOfTime(startTime)) searchCutOff = true;
        if (Logic.gameOver(state) || depth <= 0 || searchCutOff)
            return new StateMapping(null, heuristic(state), depth);
        StateMapping mapping = null;
        if (useTranspo) {
            mapping = transTable.get(state.getZobristKey());
            if (mapping != null && (depth <= mapping.depth || Math.abs(mapping.score) >= 1000)) {
                return mapping;
            }
        }
        if (moveOrdering && depth == CURR_MAX_DEPTH && prevBestState != null) {
            score = minimax(prevBestState, depth - 1, alpha, beta, startTime).score;
            if (state.getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = prevBestState.getMove();
                }
                alpha = Math.max(score, alpha);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = prevBestState.getMove();
                }
                beta = Math.min(score, beta);
            }
        }
        for (State child : state.getChildren()) {
            if (moveOrdering && depth == CURR_MAX_DEPTH) if (child.equals(prevBestState)) continue;
            score = minimax(child, depth - 1, alpha, beta, startTime).score;
            if (state.getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = child.getMove();
                }
                alpha = Math.max(score, alpha);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = child.getMove();
                }
                beta = Math.min(score, beta);
            }
            if (beta <= alpha) break;
        }
        if (moveOrdering && depth == CURR_MAX_DEPTH) {
            prevBestState = state.getNextState(bestMove);
        }
        if (useTranspo && !searchCutOff) {
            if (mapping == null || depth > mapping.depth) {
                transTable.put(state.getZobristKey(), new StateMapping(bestMove, bestScore, depth));
            }
        }
        return new StateMapping(bestMove, bestScore, depth);
    }

    private boolean outOfTime(long startTime) {
        return System.currentTimeMillis() - startTime >= calculationTime;
    }

    // Used by MCTS
    public void setTeam(int team) {
        this.team = team;
    }

    // These variables are reset inbetween turns of the minimax
    private void resetVariables() {
        CURR_MAX_DEPTH = 0;
        prevBestState = null;
        searchCutOff = false;

    }

    // Either returns 1000 or -1000 if terminal, or the material of a state, if intermediate.
    // The material is the objective value of a state
    private int heuristic(kulibrat.game.State state) {
        int opponent = (team == PLAYER2) ? PLAYER2 : PLAYER1;
        if (Logic.gameOver(state)) {
            int winner = Logic.getWinner(state);
            if (winner == team) {
                return 1000;
            } else if (winner == opponent) return -1000;
        }
        if (state.getTurn() == team) return state.getMaterial();
        else return -state.getMaterial();
    }

    // Used if a win has been ensured, to make sure the algorithm fulfills all its allocated time.
    private void chill(long startTime) {
        while (!outOfTime(startTime)) {
            //chill
        }
    }

    public void setUseTranspo(boolean transpo) {
        useTranspo = transpo;
    }
}
