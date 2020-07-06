package fftlib.game;


import java.util.ArrayList;
import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class FFTSolution{

    private static HashMap<? extends FFTNode, NodeMapping> solution;

    public static void setSolution(HashMap<? extends FFTNode, NodeMapping> lookupTable) {
        solution = lookupTable;
    }

    public static HashMap<? extends FFTNode, NodeMapping> getSolution() {
        return solution;
    }

    public static ArrayList<? extends FFTMove> optimalMoves(FFTNode node) {
        ArrayList<FFTMove> optimalMoves = new ArrayList<>();
        NodeMapping mapping = solution.get(node);
        if (mapping == null || mapping.getMove() == null) {
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
        return optimalMoves;
    }

    public static NodeMapping queryNode(FFTNode n) {
        return solution.get(n);
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int turn, FFTNode n) {
        if (queryNode(n) == null)
            return "0";
        int score = queryNode(n).getScore();

        if (score > 0 && score < 1000) { // Draw
            return "" + score;
        }
        else if (score > 0) {
            if (turn == PLAYER2) {
                return "" + (-2000 + score);
            } else {
                return "" + (2000 - score);
            }
        } else {
            if (turn == PLAYER2) {
                return "" + (2000 + score);
            } else {
                return "" + (-2000 - score);
            }
        }
    }

    public static int size() {
        return solution.size();
    }
}
