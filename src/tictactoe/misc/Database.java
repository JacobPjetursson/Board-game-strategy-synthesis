package tictactoe.misc;

import fftlib.game.FFTDatabase;
import fftlib.game.FFTMinimaxPlay;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import tictactoe.ai.MinimaxPlay;
import tictactoe.game.State;
import tictactoe.game.Logic;
import tictactoe.game.Move;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


public class Database implements FFTDatabase {
    public static HashMap<State, MinimaxPlay> lookupTable;

    // Outputs a list of the best plays from a given node. Checks through the children of a node to find the ones
    // which have the least amount of turns to terminal for win, or most for loss.
    public static ArrayList<Move> bestPlays(State n) {
        ArrayList<Move> bestPlays = new ArrayList<>();
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        if (!Logic.gameOver(n.getNextState(bestPlay.move))) {
            bestScore = queryPlay(n.getNextState(bestPlay.move)).score;
        }
        for (State child : n.getChildren()) {
            Move m = child.getMove();
            tictactoe.game.State state = n.getNextState(m);
            if (Logic.gameOver(state)) {
                if (Logic.getWinner(state) == m.team) bestPlays.add(m);
            } else if (queryPlay(child).score == bestScore) {
                bestPlays.add(m);
            }
        }
        return bestPlays;
    }

    public static ArrayList<Move> nonLosingPlays(State n) {
        ArrayList<Move> nonLosingPlays = new ArrayList<>();
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        boolean won = false;
        if (!Logic.gameOver(n.getNextState(bestPlay.move))) {
            bestScore = queryPlay(n.getNextState(bestPlay.move)).score;
        } else
            won = true;

        for (State child : n.getChildren()) {
            Move m = child.getMove();
            tictactoe.game.State state = n.getNextState(m);
            if (Logic.gameOver(state)) {
                if (Logic.getWinner(state) == m.team)
                    nonLosingPlays.add(m);
            } else {
                int score = queryPlay(child).score;
                if (score == bestScore) {
                    nonLosingPlays.add(m);
                } else if (won && m.team == PLAYER1 && score > 0)
                    nonLosingPlays.add(m);
                else if (won && m.team == PLAYER2 && score < 0)
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
    public static MinimaxPlay queryPlay(State n) {
        return lookupTable.get(n);
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int turn, State n) {
        int score = queryPlay(n).score;
        if (score == 0) {
            return "∞";
        }
        if (score > 0) {
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

    public boolean connectAndVerify() {
        // There is no db
        return true;
    }

    public static void fillLookupTable(HashMap<State, MinimaxPlay> lookup) {
        lookupTable = lookup;
    }

    public ArrayList<? extends FFTMove> nonLosingPlays(FFTState s) {
        return nonLosingPlays((State) s);
    }

    public FFTMinimaxPlay queryPlay(FFTState s) {
        return queryPlay((State) s);
    }


}
