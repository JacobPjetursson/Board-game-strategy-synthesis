package tictactoe.misc;

import fftlib.game.FFTDatabase;
import fftlib.game.FFTMinimaxPlay;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import tictactoe.ai.MinimaxPlay;
import tictactoe.ai.Node;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


public class Database implements FFTDatabase {
    public static HashMap<Node, MinimaxPlay> lookupTable;


    // Outputs a list of the best plays from a given node. Checks through the children of a node to find the ones
    // which have the least amount of turns to terminal for win, or most for loss.
    public static ArrayList<Move> bestPlays(Node n) {
        ArrayList<Move> bestPlays = new ArrayList<>();
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        if (!Logic.gameOver(n.getNextNode(bestPlay.move).getState())) {
            bestScore = queryPlay(n.getNextNode(bestPlay.move)).score;
        }
        for (Node child : n.getChildren()) {
            Move m = child.getState().getMove();
            State state = n.getNextNode(m).getState();
            if (Logic.gameOver(state)) {
                if (Logic.getWinner(state) == m.team) bestPlays.add(m);
            } else if (queryPlay(child).score == bestScore) {
                bestPlays.add(m);
            }
        }
        return bestPlays;
    }

    public static ArrayList<Move> nonLosingPlays(Node n) {
        ArrayList<Move> nonLosingPlays = new ArrayList<>();
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        boolean won = false;
        if (!Logic.gameOver(n.getNextNode(bestPlay.move).getState())) {
            bestScore = queryPlay(n.getNextNode(bestPlay.move)).score;
        } else
            won = true;

        for (Node child : n.getChildren()) {
            Move m = child.getState().getMove();
            State state = n.getNextNode(m).getState();
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
    public static MinimaxPlay queryPlay(Node n) {
        return lookupTable.get(n);
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int turn, Node n) {
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

    public static void fillLookupTable(HashMap<Node, MinimaxPlay> lookup) {
        lookupTable = lookup;
    }

    public ArrayList<? extends FFTMove> nonLosingPlays(FFTNode n) {
        return nonLosingPlays((Node) n);
    }

    public FFTMinimaxPlay queryPlay(FFTNode n) {
        return queryPlay((Node) n);
    }


}
