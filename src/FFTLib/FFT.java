package fftlib;

import fftlib.game.FFTStateMapping;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.game.FFTStateAndMove;

import java.util.*;

import static misc.Config.*;


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
    }

    public boolean verify(int team, boolean complete) {
        return verify(team, complete, null);
    }

    public boolean verify(int team, boolean complete, HashMap<FFTState, FFTStateMapping> strategy) {
        if (team == PLAYER_ANY)
            return verify(PLAYER1, complete, strategy) && verify(PLAYER2, complete, strategy);
        FFTState initialState = FFTManager.initialFFTState;
        LinkedList<FFTState> frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        frontier.add(initialState);
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        // Check if win or draw is even possible
        int score = FFTManager.db.queryState(initialState).getScore();
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
                ArrayList<? extends FFTMove> nonLosingMoves = FFTManager.db.nonLosingMoves(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {

                    if (!complete && strategy != null) { // only expand on move from strategy
                        FFTStateMapping info = strategy.get(state);
                        if (info == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        FFTState nextState = state.getNextState(info.getMove());
                        if (!closedSet.contains(nextState)) {
                            closedSet.add(nextState);
                            frontier.add(nextState);
                        }
                        continue;
                    }

                    for (FFTMove m : state.getLegalMoves()) {
                        if (nonLosingMoves.contains(m)) {
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
                } else if (!nonLosingMoves.contains(move)) {
                    failingPoint = new FFTStateAndMove(state, move, false);
                    return false;
                } else {
                    if (!complete && strategy != null) { // check that move is same as from strategy
                        FFTStateMapping info = strategy.get(state);
                        if (info == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        if (!info.getMove().equals(move)) { // DOESN'T WORK BECAUSE OF TRANSFORMATIONS!
                            return false;
                        }

                    }
                    FFTState nextState = state.getNextState(move);
                    if (!closedSet.contains(nextState)) {
                        closedSet.add(nextState);
                        frontier.add(nextState);
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



    public int getAmountOfRules() {
        int ruleSize = 0;
        for (RuleGroup rg : ruleGroups) {
            ruleSize += rg.rules.size();
        }
        return ruleSize;
    }

    public int getAmountOfPreconditions() {
        int precSize = 0;
        for (RuleGroup rg : ruleGroups) {
            precSize += rg.getAmountOfPreconditions();
        }
        return precSize;
    }

    public int minimize(int perspective) { // Returns amount of iterations
        if (!verify(perspective, true)) {
            System.out.println("FFT is not a winning strategy, so it can not be minimized");
            return 0;
        }

        int ruleSize = getAmountOfRules();
        int precSize = getAmountOfPreconditions();

        minimizeRules(perspective);
        minimizePreconditions(perspective);

        int minRuleSize = getAmountOfRules();
        int minPrecSize = getAmountOfPreconditions();

        int i = 0;
        while (ruleSize != minRuleSize || precSize != minPrecSize) {
            ruleSize = minRuleSize;
            precSize = minPrecSize;
            minimizeRules(perspective);
            minimizePreconditions(perspective);
            minRuleSize = getAmountOfRules();
            minPrecSize = getAmountOfPreconditions();
            i++;
        }
        return i;
    }

    private ArrayList<Rule> minimizeRules(int team) {
        System.out.println("Minimizing rules in FFT");
        ArrayList<Rule> redundantRules = new ArrayList<>();

        for (RuleGroup rg : ruleGroups) {
            ListIterator<Rule> itr = rg.rules.listIterator();
            while(itr.hasNext()) {
                Rule r = itr.next();
                if (r.multiRule) continue; // TODO - support multirule when minimizing?
                itr.remove();
                if (verify(team, true)) {
                    redundantRules.add(r);
                }
                else
                    itr.add(r);
            }
        }
        return redundantRules;
    }

    private void minimizePreconditions(int team) {
        System.out.println("Minimizing preconditions in rules in FFT");

        for (RuleGroup rg : ruleGroups) {
            for(Rule r : rg.rules) {
                if (r.multiRule) continue; // TODO - support multirule when minimizing?
                ArrayList<Literal> literals = new ArrayList<>();
                for (Literal l : r.preconditions.literals)
                    literals.add(new Literal(l));

                for (Literal l : literals) {
                    r.removePrecondition(l);
                    if (!verify(team, true))
                        r.addPrecondition(l);
                }
            }
        }
    }

}
