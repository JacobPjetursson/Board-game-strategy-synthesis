package fftlib.game;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static misc.Config.CACHE_OPTIMAL_MOVES;
import static misc.Config.SINGLE_THREAD;
import static misc.Globals.*;

public class FFTSolution{

    private static Map<? extends FFTNode, NodeMapping> solution;
    private static final Map<FFTNode, List<FFTMove>> cachedOptimalMoves;

    static {
        if (SINGLE_THREAD)
            cachedOptimalMoves = new HashMap<>();
        else
            cachedOptimalMoves = new ConcurrentHashMap<>();
    }

    public static void setSolution(Map<? extends FFTNode, NodeMapping> lookupTable) {
        solution = lookupTable;
    }

    public static Map<? extends FFTNode, NodeMapping> getSolution() {
        return solution;
    }

    public static List<? extends FFTMove> optimalMoves(FFTNode node) {
        if (CACHE_OPTIMAL_MOVES) {
            List<FFTMove> optimalMoves = cachedOptimalMoves.get(node);
            if (optimalMoves != null)
                return optimalMoves;
        }

        List<FFTMove> optimalMoves = new ArrayList<>();
        NodeMapping mapping = solution.get(node);
        if (mapping == null || mapping.getMove() == null) {
            if (CACHE_OPTIMAL_MOVES)
                cachedOptimalMoves.put(node, optimalMoves);
            return optimalMoves;
        }
        FFTNode next = node.getNextNode(mapping.getMove());
        // Cant assume that best move will end the game, in fact its the opposite
        // In case of game over
        if (next.isTerminal()) {
            int winner = next.getWinner();
            for (FFTNode child : node.getChildren()) {
                FFTMove m = child.getMove();
                if (child.isTerminal()) {
                    if (child.getWinner() == winner)
                        optimalMoves.add(m);
                } else {
                    if (winner == solution.get(child).getWinner())
                        optimalMoves.add(m);
                }
            }
            if (CACHE_OPTIMAL_MOVES)
                cachedOptimalMoves.put(node, optimalMoves);
            return optimalMoves;
        }

        int bestMoveWinner = solution.get(next).getWinner();
        int team = node.getTurn();

        for (FFTNode child : node.getChildren()) {
            FFTMove m = child.getMove();
            if (child.isTerminal()) {
                if (team == PLAYER1 && bestMoveWinner == PLAYER2)
                    optimalMoves.add(m);
                else if (team == PLAYER2 && bestMoveWinner == PLAYER1)
                    optimalMoves.add(m);
                continue;
            }
            int childWinner = solution.get(child).getWinner();
            if (team == PLAYER1) {
                if (bestMoveWinner == childWinner)
                    optimalMoves.add(m);
                else if (bestMoveWinner == PLAYER2)
                    optimalMoves.add(m);
            } else {
                if (bestMoveWinner == childWinner)
                    optimalMoves.add(m);
                else if (bestMoveWinner == PLAYER1)
                    optimalMoves.add(m);
            }
        }
        if (CACHE_OPTIMAL_MOVES)
            cachedOptimalMoves.put(node, optimalMoves);
        return optimalMoves;
    }

    public static NodeMapping queryNode(FFTNode n) {
        return solution.get(n);
    }

    public static FFTNode get(FFTNode n) {
        NodeMapping nm = solution.get(n);
        if (nm == null)
            return null;
        return nm.getNode();
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int team, FFTNode n) {
        if (queryNode(n) == null)
            return "0";
        NodeMapping nm = queryNode(n);

        if (nm.getWinner() == PLAYER_NONE) { // Draw
            return "" + nm.getScore();
        }
        else if (nm.getWinner() == PLAYER1) {
            if (team == PLAYER1) {
                return "" + (2000 - nm.getScore());
            } else {
                return "" + (-2000 + nm.getScore());
            }
        } else {
            if (team == PLAYER2) {
                return "" + (2000 + nm.getScore());
            } else {
                return "" + (-2000 - nm.getScore());
            }
        }
    }

    public static int size() {
        return solution.size();
    }
}
