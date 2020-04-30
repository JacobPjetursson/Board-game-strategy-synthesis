package fftlib.game;


import fftlib.Action;
import fftlib.FFTManager;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class FFTSolution{

    // fixme: this should be deleted first in case of any memory issues
    private static HashMap<? extends FFTNode, NodeMapping> lookupTable;
    // fixme: this should be shrinked in case of further memory issues (only reachable states)
    private static HashMap<State, ArrayList<Action>> solution;

    public static void setSolution(HashMap<? extends FFTNode, NodeMapping> table) {
        lookupTable = table;
        solution = new HashMap<>();
        for (FFTNode node : lookupTable.keySet()) {
            ArrayList<Action> optimalActions = new ArrayList<>();
            ArrayList<? extends FFTMove> optimalMoves = optimalMoves(node);
            for (FFTMove m : optimalMoves) {
                optimalActions.add(new Action(m.convert()));
            }
            solution.put(node.convert(), optimalActions);
        }
    }

    public static ArrayList<? extends FFTMove> optimalMoves(
            FFTNode node) {
        ArrayList<FFTMove> optimalMoves = new ArrayList<>();
        NodeMapping mapping = lookupTable.get(node);
        if (mapping == null || mapping.getMove() == null) {
            return optimalMoves;
        }
        FFTNode next = node.getNextNode(mapping.getMove());
        // Cant assume that best move will end the game, in fact its the opposite
        // In case of game over
        if (FFTManager.logic.gameOver(next)) {
            int winner = FFTManager.logic.getWinner(next);
            for (FFTNode child : node.getChildren()) {
                FFTMove m = child.getMove();
                if (FFTManager.logic.gameOver(child)) {
                    if (FFTManager.logic.getWinner(child) == winner)
                        optimalMoves.add(m);
                } else {
                    if (winner == lookupTable.get(child).getWinner())
                        optimalMoves.add(m);
                }
            }
            return optimalMoves;
        }

        int bestMoveWinner = lookupTable.get(next).getWinner();
        int team = node.getTurn();

        for (FFTNode child : node.getChildren()) {
            FFTMove m = child.getMove();
            if (FFTManager.logic.gameOver(child)) {
                if (team == PLAYER1 && bestMoveWinner == PLAYER2)
                    optimalMoves.add(m);
                else if (team == PLAYER2 && bestMoveWinner == PLAYER1)
                    optimalMoves.add(m);
                continue;
            }
            int childWinner = lookupTable.get(child).getWinner();
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

    public static ArrayList<Action> getOptimalActions(State state) {
        return solution.get(state);
    }


    public static HashMap<State, ArrayList<Action>> getSolution() {
        return solution;
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    /*
    public static String turnsToTerminal(int turn, State n) {
        if (queryState(n) == null)
            return "0";
        int score = queryState(n).getScore();

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

     */
}
