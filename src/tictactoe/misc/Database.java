package tictactoe.misc;

import fftlib.game.FFTDatabase;
import fftlib.game.FFTMinimaxPlay;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import tictactoe.ai.MinimaxPlay;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;
import static tictactoe.FFT.FFTAutoGen.INCLUDE_ILLEGAL_STATES;


public class Database implements FFTDatabase {
    private static HashMap<State, MinimaxPlay> lookupTable;
    // Used for autogen
    private static HashMap<State, MinimaxPlay> lookupTableAll;

    // Outputs a list of the best plays from a given node. Checks through the children of a node to find the ones
    // which have the least amount of turns to terminal for win, or most for loss.
    public static ArrayList<Move> bestPlays(State n) {
        ArrayList<Move> bestPlays = new ArrayList<>();
        if (Logic.gameOver(n))
            return bestPlays;
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        if (!Logic.gameOver(n.getNextState(bestPlay.move))) {
            bestScore = queryPlay(n.getNextState(bestPlay.move)).score;
        }
        for (State child : n.getChildren()) {
            Move m = child.getMove();

            State state = n.getNextState(m);
            if (Logic.gameOver(state)) {
                if (Logic.getWinner(state) == bestPlay.move.getTeam())
                    bestPlays.add(m);
            } else if (queryPlay(child).score == bestScore) {
                bestPlays.add(m);
            }
        }
        return bestPlays;
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int turn, State n) {
        int score = queryPlay(n).score;

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

    private static ArrayList<Move> nonLosingPlays(State n) {
        ArrayList<Move> nonLosingPlays = new ArrayList<>();
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        int winner = 0;
        boolean gameover = false;
        State next = n.getNextState(bestPlay.move);
        if (Logic.gameOver(next)) {
            gameover = true;
            winner = Logic.getWinner(next);
        }
        else
            bestScore = queryPlay(next).score;

        for (State child : n.getChildren()) {
            Move m = child.getMove();
            State state = n.getNextState(m);
            if (gameover && Logic.gameOver(state)) {
                int stateWinner = Logic.getWinner(state);
                if (winner == stateWinner)
                    nonLosingPlays.add(m);
            } else {
                int score = queryPlay(child).score;
                if (score == bestScore) {
                    nonLosingPlays.add(m);
                } else if (gameover && m.team == PLAYER1 && score > 0)
                    nonLosingPlays.add(m);
                else if (gameover && m.team == PLAYER2 && score < 0)
                    nonLosingPlays.add(m);
                else if (bestScore > 0 && score > 0) {
                    nonLosingPlays.add(m);
                } else if (bestScore < 0 && score < 0) {
                    nonLosingPlays.add(m);
                }
            }
        }
        return nonLosingPlays;
    }

    // Fetches the best play corresponding to the input node
    private static MinimaxPlay queryPlay(State n) {
        if (INCLUDE_ILLEGAL_STATES)
            return lookupTableAll.get(n);
        return lookupTable.get(n);
    }

    public boolean connectAndVerify() {
        // There is no db
        return true;
    }

    public static void fillLookupTable(HashMap<State, MinimaxPlay> lookup) {
        lookupTable = lookup;
    }

    // Used for autogen
    public static void fillLookupTableAll(HashMap<State, MinimaxPlay> lookup) {
        lookupTableAll = lookup;

    }

    public ArrayList<? extends FFTMove> nonLosingPlays(FFTState s) {
        return nonLosingPlays((State) s);
    }

    public ArrayList<? extends FFTMove> bestPlays(FFTState state) {
        return bestPlays((State) state);
    }

    public FFTMinimaxPlay queryPlay(FFTState s) {
        return queryPlay((State) s);
    }

    public static HashMap<State, MinimaxPlay> getLookupTable() {
        return lookupTable;
    }

    // Used for autogen
    public static HashMap<State, MinimaxPlay> getLookupTableAll() {
        return lookupTableAll;
    }


}
