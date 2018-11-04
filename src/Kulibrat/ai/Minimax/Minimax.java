package ai.Minimax;

import ai.AI;
import game.Logic;
import game.Move;
import game.State;

import java.util.HashMap;
import java.util.Random;

import static misc.Config.BLACK;
import static misc.Config.RED;

public class Minimax extends AI {
    private long calculationTime;
    private boolean searchCutOff;
    private int CURR_MAX_DEPTH;
    private boolean moveOrdering = true;
    private boolean useTranspo = true;
    private HashMap<Long, MinimaxPlay> transTable;
    private Node prevBestNode;

    public Minimax(int team, int calculationTime) {
        super(team);
        this.calculationTime = calculationTime;
        transTable = new HashMap<>();
    }

    // Runs the iterative deepening minimax with a set timelimit
    public Move makeMove(State state) {
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
    private MinimaxPlay iterativeDeepeningMinimax(State state, long startTime) {
        resetVariables();
        MinimaxPlay bestPlay = null;
        boolean winCutOff = false;
        while (!outOfTime(startTime) && !winCutOff) {
            Node simNode = new Node(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            CURR_MAX_DEPTH++;
            MinimaxPlay play = minimax(simNode, CURR_MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime);
            if (!searchCutOff) bestPlay = play;
            if (Math.abs(play.score) >= 1000) winCutOff = true;
        }
        // random move if null (No time to calculate minimax)
        if (bestPlay == null) {
            int r = new Random().nextInt(state.getLegalMoves().size());
            bestPlay = new MinimaxPlay(state.getLegalMoves().get(r), Integer.MIN_VALUE, 0);
        }
        System.out.println("Score: " + bestPlay.score + ", Depth: " + CURR_MAX_DEPTH + ", Play:  oldRow: " + bestPlay.move.oldRow + ", oldCol: " +
                bestPlay.move.oldCol + ", newRow: " + bestPlay.move.newRow + ", newCol: " + bestPlay.move.newCol + ", team: " + bestPlay.move.team);
        return bestPlay;
    }

    // Minimax with pruning, move ordering and a detailed heuristic
    public MinimaxPlay minimax(Node node, int depth, int alpha, int beta, long startTime) {
        Move bestMove = null;
        int bestScore = (node.getState().getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (outOfTime(startTime)) searchCutOff = true;
        if (Logic.gameOver(node.getState()) || depth <= 0 || searchCutOff)
            return new MinimaxPlay(null, heuristic(node.getState()), depth);
        MinimaxPlay transpoPlay = null;
        if (useTranspo) {
            transpoPlay = transTable.get(node.getHashCode());
            if (transpoPlay != null && (depth <= transpoPlay.depth || Math.abs(transpoPlay.score) >= 1000)) {
                return transpoPlay;
            }
        }
        if (moveOrdering && depth == CURR_MAX_DEPTH && prevBestNode != null) {
            score = minimax(prevBestNode, depth - 1, alpha, beta, startTime).score;
            if (node.getState().getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = prevBestNode.getState().getMove();
                }
                alpha = Math.max(score, alpha);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = prevBestNode.getState().getMove();
                }
                beta = Math.min(score, beta);
            }
        }
        for (Node child : node.getChildren()) {
            if (moveOrdering && depth == CURR_MAX_DEPTH) if (child.equals(prevBestNode)) continue;
            score = minimax(child, depth - 1, alpha, beta, startTime).score;
            if (node.getState().getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
                alpha = Math.max(score, alpha);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
                beta = Math.min(score, beta);
            }
            if (beta <= alpha) break;
        }
        if (moveOrdering && depth == CURR_MAX_DEPTH) {
            prevBestNode = node.getNextNode(bestMove);
        }
        if (useTranspo && !searchCutOff) {
            if (transpoPlay == null || depth > transpoPlay.depth) {
                transTable.put(node.getHashCode(), new MinimaxPlay(bestMove, bestScore, depth));
            }
        }
        return new MinimaxPlay(bestMove, bestScore, depth);
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
        prevBestNode = null;
        searchCutOff = false;

    }
    // Either returns 1000 or -1000 if terminal, or the material of a state, if intermediate.
    // The material is the objective value of a state
    private int heuristic(State state) {
        int opponent = (team == BLACK) ? BLACK : RED;
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
