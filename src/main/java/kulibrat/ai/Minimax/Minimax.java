package kulibrat.ai.Minimax;

import fftlib.game.NodeMapping;
import kulibrat.ai.AI;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.Node;

import java.util.HashMap;
import java.util.Random;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Minimax extends AI {
    private long calculationTime;
    private boolean searchCutOff;
    private int CURR_MAX_DEPTH;
    private boolean moveOrdering = true;
    private boolean useTranspo = true;
    private HashMap<Long, NodeMapping> transTable;
    private Node prevBestState;

    public Minimax(int team, int calculationTime) {
        super(team);
        this.calculationTime = calculationTime;
        transTable = new HashMap<>();
    }

    // Runs the iterative deepening minimax with a set timelimit
    public Move makeMove(Node node) {
        long startTime = System.currentTimeMillis();
        if (node.getLegalMoves().size() == 1) {
            chill(startTime);
            return node.getLegalMoves().get(0);
        }
        Move move = (Move) iterativeDeepeningMinimax(node, startTime).move;
        // This happens when the minimax returns faster after having found a winning move
        chill(startTime);
        return move;
    }

    // Iteratively increases the depth limit while called minimax continuously. Stops when win is ensured or time is up.
    private NodeMapping iterativeDeepeningMinimax(Node node, long startTime) {
        resetVariables();
        NodeMapping info = null;
        boolean winCutOff = false;
        while (!outOfTime(startTime) && !winCutOff) {
            Node simState = new Node(node); // Start from fresh (Don't reuse previous game tree in new iterations)
            CURR_MAX_DEPTH++;
            NodeMapping mapping = minimax(simState, CURR_MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime);
            if (!searchCutOff) info = mapping;
            if (Math.abs(mapping.score) >= 1000) winCutOff = true;
        }
        // random move if null (No time to calculate minimax)
        if (info == null) {
            int r = new Random().nextInt(node.getLegalMoves().size());
            info = new NodeMapping(node.getLegalMoves().get(r), Integer.MIN_VALUE, 0);
        }
        System.out.println(info);
        return info;
    }

    // Minimax with pruning, move ordering and a detailed heuristic
    public NodeMapping minimax(Node node, int depth, int alpha, int beta, long startTime) {
        Move bestMove = null;
        int bestScore = (node.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (outOfTime(startTime)) searchCutOff = true;
        if (Logic.gameOver(node) || depth <= 0 || searchCutOff)
            return new NodeMapping(null, heuristic(node), depth);
        NodeMapping mapping = null;
        if (useTranspo) {
            mapping = transTable.get(node.getZobristKey());
            if (mapping != null && (depth <= mapping.depth || Math.abs(mapping.score) >= 1000)) {
                return mapping;
            }
        }
        if (moveOrdering && depth == CURR_MAX_DEPTH && prevBestState != null) {
            score = minimax(prevBestState, depth - 1, alpha, beta, startTime).score;
            if (node.getTurn() == team) {
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
        for (Node child : node.getChildren()) {
            if (moveOrdering && depth == CURR_MAX_DEPTH) if (child.equals(prevBestState)) continue;
            score = minimax(child, depth - 1, alpha, beta, startTime).score;
            if (node.getTurn() == team) {
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
            prevBestState = node.getNextNode(bestMove);
        }
        if (useTranspo && !searchCutOff) {
            if (mapping == null || depth > mapping.depth) {
                transTable.put(node.getZobristKey(), new NodeMapping(bestMove, bestScore, depth));
            }
        }
        return new NodeMapping(bestMove, bestScore, depth);
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
    private int heuristic(Node node) {
        int opponent = (team == PLAYER2) ? PLAYER2 : PLAYER1;
        if (Logic.gameOver(node)) {
            int winner = Logic.getWinner(node);
            if (winner == team) {
                return 1000;
            } else if (winner == opponent) return -1000;
        }
        if (node.getTurn() == team) return node.getMaterial();
        else return -node.getMaterial();
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
