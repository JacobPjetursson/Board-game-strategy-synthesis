/**
 * This class attempts to find a winning strategy by focing a winning cycle. It fails to find it
 */
package ai.Minimax.Experimental;

import ai.Minimax.MinimaxPlay;
import ai.Minimax.Node;
import ai.Minimax.Zobrist;
import game.Logic;
import game.Move;
import game.State;

import java.util.HashMap;
import java.util.HashSet;

import static misc.Config.RED;

public class FindWinningCycles {
    private static HashMap<Long, MinimaxPlay> transpoTable = new HashMap<>();
    private static int team = RED;

    private static String iterativeDeepeningMinimax(State state) {
        int CURR_MAX_DEPTH = 0;
        boolean done = false;
        while (!done) {
            Node simNode = new Node(state);
            CURR_MAX_DEPTH += 1;
            HashSet<Long> loops = new HashSet<>();
            int prevSize = transpoTable.size();
            MinimaxPlay play = minimax(simNode, loops, CURR_MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH + ", LOOKUP TABLE SIZE: " + transpoTable.size() +
                    ", SCORE: " + play.score);

            if (Math.abs(play.score) >= 1) {
                String player = (team == RED) ? "RED" : "BLACK";
                String opponent = (player.equals("RED")) ? "BLACK" : "RED";
                return (play.score >= 1) ? player : opponent;
            }
            if (prevSize == transpoTable.size()) {
                done = true;
            }
        }
        return "None";
    }

    private static MinimaxPlay minimax(Node node, HashSet<Long> loops, int depth, int alpha, int beta) {
        Move bestMove = null;
        int bestScore = (node.getState().getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (loops.contains(node.getHashCode())) {
            int eval = (node.getState().getTurn() == team) ? 1 : -1;
            return new MinimaxPlay(bestMove, eval, depth);
        }

        if (Logic.gameOver(node.getState()) || depth == 0) {
            return new MinimaxPlay(bestMove, 0, depth);
        }
        State state = new State(node.getState());
        for (int i = 0; i < state.getScoreLimit() - state.getScore(state.getTurn()); i++) {
            state.addPoint(state.getTurn());
            Node addPoint = new Node(state);
            loops.add(addPoint.getHashCode());
        }

        MinimaxPlay transpoPlay = transpoTable.get(node.getHashCode());
        if (transpoPlay != null && (depth <= transpoPlay.depth || Math.abs(transpoPlay.score) == 1)) {
            return transpoPlay;
        }
        for (Node child : node.getChildren()) {
            score = minimax(child, new HashSet<>(loops), depth - 1, alpha, beta).score;
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
        if (transpoPlay == null || depth > transpoPlay.depth) {
            transpoTable.put(node.getHashCode(),
                    new MinimaxPlay(bestMove, bestScore, depth));
        }
        return new MinimaxPlay(bestMove, bestScore, depth);
    }

    public static void main(String args[]) {
        Zobrist.initialize();
        State state = new State();
        String winner = iterativeDeepeningMinimax(state);
        System.out.print("Algorithm is finished running. ");
        System.out.println(winner + " is able to force a winning cycle");
    }
}
