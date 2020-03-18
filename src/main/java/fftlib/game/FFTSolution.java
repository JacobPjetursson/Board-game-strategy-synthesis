package fftlib.game;


import fftlib.FFTManager;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class FFTSolution{
    private static HashMap<? extends FFTState, StateMapping> lookupTable;

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int turn, FFTState n) {
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

    public static ArrayList<? extends FFTMove> nonLosingMoves(FFTState state) {
        ArrayList<FFTMove> nonLosingMoves = new ArrayList<>();
        if (FFTManager.logic.gameOver(state))
            return nonLosingMoves;
        StateMapping mapping = queryState(state);
        if (mapping.getMove() == null)
            return nonLosingMoves;
        FFTState next = state.getNextState(mapping.getMove());
        // Cant assume that best move will end the game, in fact its the opposite
        // In case of game over
        if (FFTManager.logic.gameOver(next)) {
            int winner = FFTManager.logic.getWinner(next);
            for (FFTState child : state.getChildren()) {
                FFTMove m = child.getMove();
                if (FFTManager.logic.gameOver(child)) {
                    if (FFTManager.logic.getWinner(child) == winner)
                        nonLosingMoves.add(m);
                } else {
                    if (winner == queryState(child).getWinner())
                        nonLosingMoves.add(m);
                }
            }
            return nonLosingMoves;
        }

        int bestMoveWinner = queryState(next).getWinner();
        int team = state.getTurn();

        for (FFTState child : state.getChildren()) {
            FFTMove m = child.getMove();
            if (FFTManager.logic.gameOver(child)) {
                if (team == PLAYER1 && bestMoveWinner == PLAYER2)
                    nonLosingMoves.add(m);
                else if (team == PLAYER2 && bestMoveWinner == PLAYER1)
                    nonLosingMoves.add(m);
                continue;
            }
            int childWinner = queryState(child).getWinner();
            if (team == PLAYER1) {
                if (bestMoveWinner == childWinner)
                    nonLosingMoves.add(m);
                else if (bestMoveWinner == PLAYER2)
                    nonLosingMoves.add(m);
            } else {
                if (bestMoveWinner == childWinner)
                    nonLosingMoves.add(m);
                else if (bestMoveWinner == PLAYER1)
                    nonLosingMoves.add(m);
            }
        }
        return nonLosingMoves;
    }

    // Fetches the best play corresponding to the input node
    public static StateMapping queryState(FFTState n) {
        return lookupTable.get(n);
    }

    public static void setLookupTable(HashMap<FFTState, StateMapping> lookup) {
        lookupTable = lookup;
    }

    public static HashMap<? extends FFTState, StateMapping> getLookupTable() {
        return lookupTable;
    }

}
