package fftlib.game;


import fftlib.FFTManager;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class FFTSolution{
    private static HashMap<? extends FFTState, StateMapping> lookupTable;
    private static HashMap<FFTState, ArrayList<? extends FFTMove>> optimalMovesMap = new HashMap<>();

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

    public static ArrayList<? extends FFTMove> optimalMoves(FFTState state) {
        ArrayList<? extends FFTMove> optMoves = optimalMovesMap.get(state);
        if (optMoves != null)
            return optMoves;
        ArrayList<FFTMove> optimalMoves = new ArrayList<>();
        StateMapping mapping = queryState(state);
        if (mapping.getMove() == null) {
            optimalMovesMap.put(state, new ArrayList<>());
            return optimalMoves;
        }
        FFTState next = state.getNextState(mapping.getMove());
        // Cant assume that best move will end the game, in fact its the opposite
        // In case of game over
        if (FFTManager.logic.gameOver(next)) {
            int winner = FFTManager.logic.getWinner(next);
            for (FFTState child : state.getChildren()) {
                FFTMove m = child.getMove();
                if (FFTManager.logic.gameOver(child)) {
                    if (FFTManager.logic.getWinner(child) == winner)
                        optimalMoves.add(m);
                } else {
                    if (winner == queryState(child).getWinner())
                        optimalMoves.add(m);
                }
            }
            optimalMovesMap.put(state, optimalMoves);
            return optimalMoves;
        }

        int bestMoveWinner = queryState(next).getWinner();
        int team = state.getTurn();

        for (FFTState child : state.getChildren()) {
            FFTMove m = child.getMove();
            if (FFTManager.logic.gameOver(child)) {
                if (team == PLAYER1 && bestMoveWinner == PLAYER2)
                    optimalMoves.add(m);
                else if (team == PLAYER2 && bestMoveWinner == PLAYER1)
                    optimalMoves.add(m);
                continue;
            }
            int childWinner = queryState(child).getWinner();
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
        optimalMovesMap.put(state, optimalMoves);
        return optimalMoves;
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

    public static int getWinner() {
        return lookupTable.get(FFTManager.initialFFTState).getWinner();
    }

}
