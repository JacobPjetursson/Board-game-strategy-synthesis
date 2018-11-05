package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.FFTState;
import fftlib.game.FFTStateAndMove;
import misc.Config;

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

    public boolean verify(int team, boolean wholeFFT) {
        FFTNode initialNode = FFTManager.initialFFTNode;
        LinkedList<FFTNode> frontier = new LinkedList<>();
        HashSet<FFTNode> closedSet = new HashSet<>();
        frontier.add(initialNode);
        int opponent = (team == Config.PLAYER1) ? Config.PLAYER2 : Config.PLAYER1;
        // Check if win or draw is even possible
        int score = FFTManager.db.queryPlay(initialNode).getScore();
        if (team == Config.PLAYER1 && score < 0) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == Config.PLAYER2 && score > 0) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }

        while (!frontier.isEmpty()) {
            FFTNode node = frontier.pop();
            if (FFTManager.logic.gameOver(node.getState())) {
                if (FFTManager.logic.getWinner(node.getState()) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            } else if (team != node.getState().getTurn()) {
                for (FFTNode child : node.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                FFTMove move = makeMove(node);
                ArrayList<FFTMove> nonLosingPlays = FFTManager.db.nonLosingPlays(node);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    for (FFTMove m : node.getState().getLegalMoves()) {
                        if (nonLosingPlays.contains(m)) {
                            FFTNode nextNode = node.getNextNode(m);
                            if (!closedSet.contains(nextNode)) {
                                closedSet.add(nextNode);
                                frontier.add(nextNode);
                            }
                        } else if (wholeFFT) {
                            System.out.println("FFT did not apply to certain state, random move lost you the game");
                            failingPoint = new FFTStateAndMove(node.getState(), m, true);
                            return false;
                        }
                    }
                } else if (!nonLosingPlays.contains(move)) {
                    System.out.println("FFT applied, but its move lost you the game");
                    failingPoint = new FFTStateAndMove(node.getState(), move, false);
                    return false;
                } else {
                    FFTNode nextNode = node.getNextNode(move);
                    if (!closedSet.contains(nextNode)) {
                        closedSet.add(nextNode);
                        frontier.add(nextNode);
                    }
                }
            }
        }
        return true;
    }

    private FFTMove makeMove(FFTNode node) {
        FFTState state = node.getState();
        for (RuleGroup ruleGroup : ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                for (int symmetry : Config.SYMMETRIES) {
                    if (rule.applies(state, symmetry)) {
                        Action action = rule.action.applySymmetry(symmetry);
                        FFTMove move = action.getMove();
                        move.setTeam(state.getTurn());
                        if (FFTManager.logic.isLegalMove(state, move)) {
                            return move;
                        }
                    }
                }
            }
        }
        return null;
    }
}
