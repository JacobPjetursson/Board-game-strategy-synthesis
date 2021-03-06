package fftlib.game;

import fftlib.FFTManager;
import misc.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static misc.Config.SINGLE_THREAD;
import static misc.Globals.*;

public class FFTSolver{
    public static boolean solved = false;
    private static int CURR_MAX_DEPTH;
    private static int unexploredNodes = 0;
    private static int team = PLAYER1; // Always from player1
    private static Map<FFTNode, NodeMapping> lookupTable;

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    public static Map<? extends FFTNode, NodeMapping> solveGame() {
        if (solved) {
            System.out.println("Game already solved once, returning immediately");
            return lookupTable;
        }
        if (SINGLE_THREAD)
            lookupTable = new HashMap<>();
        else
            lookupTable = new ConcurrentHashMap<>();

        CURR_MAX_DEPTH = 0;
        boolean done = false;

        int doneCounter = 0;
        long timeStart = System.currentTimeMillis();
        FFTNode initialNode = FFTManager.getInitialNode();
        while (!done) {
            CURR_MAX_DEPTH += 1;
            int prevSize = lookupTable.size();
            int prevvUnexploredNodes = unexploredNodes;
            unexploredNodes = 0;
            minimax(initialNode, CURR_MAX_DEPTH);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH + ", LOOKUP TABLE SIZE: " + lookupTable.size()
                    + ", UNEVALUATED NODES: " + unexploredNodes);

            if (lookupTable.size() == prevSize && unexploredNodes == prevvUnexploredNodes) {
                System.out.println("State space explored, and unevaluated nodes unchanged between runs");
                doneCounter++;
            } else {
                doneCounter = 0;
            }
            if (doneCounter == 2)
                done = true;
        }

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on solving game: " + timeSpent);
        int winner = lookupTable.get(initialNode).getWinner();
        FFTManager.winner = winner;
        String winnerStr = winner == PLAYER1 ? "Player 1" : (winner == PLAYER2) ? "Player 2" : "Draw";
        System.out.println("Winner is " + winnerStr);
        solved = true;
        FFTSolution.setSolution(lookupTable);
        // Set autogen_team to winner unless it's a draw
        if (winner == PLAYER1 || winner == PLAYER2)
            Config.AUTOGEN_TEAM = winner;
        return lookupTable;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private static NodeMapping minimax(FFTNode node, int depth) {
        FFTMove bestMove = null;
        int bestScore = (node.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        boolean gameover = node.isTerminal();
        if (gameover || depth == 0) {
            return new NodeMapping(node, bestMove, heuristic(node), depth);
        }
        NodeMapping mapping = lookupTable.get(node);
        if (mapping != null && depth <= mapping.getDepth()) {
            return mapping;
        }
        boolean explored = true;
        for (FFTNode child : node.getChildren()) {
            NodeMapping childMapping = minimax(child, depth - 1);
            score = childMapping.getScore();

            if (childMapping.getWinner() == PLAYER1) {
                score--;
            }
            else {
                score++;
                if (childMapping.getWinner() == PLAYER_NONE)
                    explored = false;
            }

            if (node.getTurn() == team) {
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
        if (mapping == null || depth > mapping.getDepth()) {
            lookupTable.put(node,
                    new NodeMapping(node, bestMove, bestScore, depth));
        }
        if (!explored)
            unexploredNodes++;
        return new NodeMapping(node, bestMove, bestScore, depth);
    }

    // Heuristic function which values player1 with 2000 for a win, and -2000 for a loss. All other nodes are 0
    private static int heuristic(FFTNode node) {
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        if (node.isTerminal()) {
            int winner = node.getWinner();
            if (winner == team)
                return 2000;
            else if (winner == opponent)
                return -2000;
        }
        return 0;
    }
}
