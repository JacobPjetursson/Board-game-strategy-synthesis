package tictactoe.misc;

import fftlib.game.FFTDatabase;
import fftlib.game.FFTStateMapping;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import tictactoe.ai.StateMapping;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class Database implements FFTDatabase {
    private static HashMap<? extends FFTState, ? extends FFTStateMapping> lookupTable;

    // Outputs a list of the best plays from a given node. Checks through the children of a node to find the ones
    // which have the least amount of turns to terminal for win, or most for loss.
    public static ArrayList<Move> bestMoves(State n) {
        ArrayList<Move> bestMoves = new ArrayList<>();
        if (Logic.gameOver(n))
            return bestMoves;
        StateMapping mapping = queryState(n);
        int bestScore = 0;
        if (!Logic.gameOver(n.getNextState(mapping.move))) {
            bestScore = queryState(n.getNextState(mapping.move)).score;
        }
        for (State child : n.getChildren()) {
            Move m = child.getMove();

            State state = n.getNextState(m);
            if (Logic.gameOver(state)) {
                if (Logic.getWinner(state) == m.getTeam())
                    bestMoves.add(m);
            } else if (queryState(child).score == bestScore) {
                bestMoves.add(m);
            }
        }
        return bestMoves;
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int turn, State n) {
        int score = queryState(n).score;

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

    public static ArrayList<Move> nonLosingMoves(State state) {
        ArrayList<Move> nonLosingMoves = new ArrayList<>();
        if (Logic.gameOver(state))
            return nonLosingMoves;
        StateMapping mapping = queryState(state);
        State next = state.getNextState(mapping.move);

        // In case of game over
        if (Logic.gameOver(next)) {
            int winner = Logic.getWinner(next);
            for (State child : state.getChildren()) {
                Move m = child.getMove();
                if (Logic.gameOver(child)) {
                    if (Logic.getWinner(child) == winner)
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

        for (State child : state.getChildren()) {
            Move m = child.getMove();
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
    private static StateMapping queryState(State n) {
        return (StateMapping) lookupTable.get(n);
    }

    public boolean connectAndVerify() {
        // There is no db
        return true;
    }

    @Override
    public boolean isLosingChild(FFTStateMapping mapping, FFTState bestChild, FFTState child) {
        return mapping == null;

    }

    public static void fillLookupTable(HashMap<State, StateMapping> lookup) {
        lookupTable = lookup;
    }

    public ArrayList<? extends FFTMove> nonLosingMoves(FFTState s) {
        return nonLosingMoves((State) s);
    }

    public ArrayList<? extends FFTMove> bestMoves(FFTState state) {
        return bestMoves((State) state);
    }

    public FFTStateMapping queryState(FFTState s) {
        return queryState((State) s);
    }

    public HashMap<? extends FFTState, ? extends FFTStateMapping> getSolution() {
        return lookupTable;
    }

}
