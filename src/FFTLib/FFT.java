package fftlib;

import fftlib.game.*;
import misc.Config;
import tictactoe.misc.Database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;


public class FFT {
    public String name;
    public ArrayList<RuleGroup> ruleGroups;
    public FFTStateAndMove failingPoint = null;

    public FFT(String name) {
        this.name = name;
        ruleGroups = new ArrayList<>();
    }

    public void addRuleGroup(RuleGroup ruleGroup) {
        ruleGroups.add(ruleGroup);
        FFTManager.save();
    }

    // Overload for simply verification
    public boolean verify(int team, boolean wholeFFT) {
        return verify(team, wholeFFT, null);
    }

    public boolean verify(int team, boolean wholeFFT, FFTMinimaxPlay prevBestPlay) {
        FFTState initialState = FFTManager.initialFFTState;
        LinkedList<FFTState> frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        frontier.add(initialState);
        int opponent = (team == Config.PLAYER1) ? Config.PLAYER2 : Config.PLAYER1;
        // Check if win or draw is even possible
        int score = FFTManager.db.queryPlay(initialState).getScore();
        if (team == Config.PLAYER1 && score < -1000) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == Config.PLAYER2 && score > 1000) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }

        while (!frontier.isEmpty()) {
            FFTState state = frontier.pop();
            if (FFTManager.logic.gameOver(state)) {
                if (FFTManager.logic.getWinner(state) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            } else if (team != state.getTurn()) {
                for (FFTState child : state.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                FFTMove move = makeMove(state);
                ArrayList<? extends FFTMove> nonLosingPlays = FFTManager.db.nonLosingPlays(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    for (FFTMove m : state.getLegalMoves()) {
                        if (nonLosingPlays.contains(m)) {
                            FFTState nextState = state.getNextState(m);
                            if (!closedSet.contains(nextState)) {
                                closedSet.add(nextState);
                                frontier.add(nextState);
                            }
                        } else if (wholeFFT) {
                            System.out.println("FFT did not apply to certain state, random move lost you the game");
                            failingPoint = new FFTStateAndMove(state, m, true);
                            return false;
                        }
                    }
                } else if (prevBestPlay != null) { // requires chosen move to be as good as prev best move
                    FFTMinimaxPlay bestPlay = FFTManager.db.queryPlay(state);
                    System.out.println(state.print());
                    System.out.println(prevBestPlay.getScore() + "  " + bestPlay.getScore());
                    System.out.println("PREV MOVE: " + prevBestPlay.getMove().print());
                    System.out.println("NEW MOVE: " + bestPlay.getMove().print());
                    System.out.println();
                    if (prevBestPlay.getScore() > 1000 && bestPlay.getScore() < prevBestPlay.getScore()) {
                        //System.out.println("FFT applied, but its move was not among the best moves");
                        failingPoint = new FFTStateAndMove(state, move, false);
                        return false;
                    }
                } else if (!nonLosingPlays.contains(move)) {
                    System.out.println("FFT applied, but its move lost you the game");
                    failingPoint = new FFTStateAndMove(state, move, false);
                    return false;
                } else {
                    FFTState nextNode = state.getNextState(move);
                    if (!closedSet.contains(nextNode)) {
                        closedSet.add(nextNode);
                        frontier.add(nextNode);
                    }
                }
            }
        }
        return true;
    }

    private FFTMove makeMove(FFTState state) {
        for (RuleGroup ruleGroup : ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                    FFTMove move = rule.apply(state);
                    if (move != null) {
                        return move;
                    }
            }
        }
        return null;
    }
}
