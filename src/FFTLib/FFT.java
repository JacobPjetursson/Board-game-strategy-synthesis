package fftlib;

import fftlib.game.*;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;
import static misc.Config.PLAYER_ANY;


public class FFT {
    public String name;
    public ArrayList<RuleGroup> ruleGroups;
    public FFTStateAndMove failingPoint = null;

    public FFT(String name) {
        this.name = name;
        ruleGroups = new ArrayList<>();
    }

    public FFT(FFT duplicate) {
        this.name = duplicate.name;
        ruleGroups = new ArrayList<>();
        for (RuleGroup rg : duplicate.ruleGroups) {
            ruleGroups.add(new RuleGroup(rg));
        }
        this.failingPoint = duplicate.failingPoint;
    }

    public void addRuleGroup(RuleGroup ruleGroup) {
        ruleGroups.add(ruleGroup);
        FFTManager.save();
    }

    public boolean verify(int team, boolean complete) {
        if (team == PLAYER_ANY)
            return verify(PLAYER1, complete) && verify(PLAYER2, complete);
        FFTState initialState = FFTManager.initialFFTState;
        LinkedList<FFTState> frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        frontier.add(initialState);
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        // Check if win or draw is even possible
        int score = FFTManager.db.queryPlay(initialState).getScore();
        if (team == PLAYER1 && score < -1000) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && score > 1000) {
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
                FFTMove move = apply(state);
                ArrayList<? extends FFTMove> nonLosingPlays = FFTManager.db.nonLosingMoves(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    for (FFTMove m : state.getLegalMoves()) {
                        if (nonLosingPlays.contains(m)) {
                            FFTState nextState = state.getNextState(m);
                            if (!closedSet.contains(nextState)) {
                                closedSet.add(nextState);
                                frontier.add(nextState);
                            }
                        } else if (complete) {
                            failingPoint = new FFTStateAndMove(state, m, true);
                            return false;
                        }
                    }
                } else if (!nonLosingPlays.contains(move)) {
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

    public FFTMove apply(FFTState state) {
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

    public boolean isMinimal(int team) {
        if (!verify(team, true)) {
            System.out.println("FFT is not a winning strategy, so it is not minimal");
            return false;
        }
        for (RuleGroup rg : ruleGroups) {
            for (int i = 0; i < rg.rules.size(); i++) {
                Rule r = rg.rules.get(i);
                rg.rules.remove(r);
                if (verify(team, true)) {
                    rg.rules.add(i, r);
                    return false;
                }
                rg.rules.add(i, r);
            }
        }
        return true;
    }

    public void removeRules(ArrayList<Rule> rules) {
        for (RuleGroup rg : ruleGroups) {
            rg.rules.removeAll(rules);
        }

    }

    public ArrayList<Rule> minimize(int team) {
        System.out.println("Minimizing FFT");
        ArrayList<Rule> redundantRules = new ArrayList<>();
        if (!verify(team, true)) {
            System.out.println("FFT is not a winning strategy, so it is not minimal");
            return redundantRules;
        }

        for (RuleGroup rg : ruleGroups) {
            ListIterator<Rule> itr = rg.rules.listIterator();
            while(itr.hasNext()) {
                Rule r = itr.next();
                itr.remove();
                if (verify(team, true))
                    redundantRules.add(r);
                else
                    itr.add(r);
            }
        }
        return redundantRules;
    }
}
