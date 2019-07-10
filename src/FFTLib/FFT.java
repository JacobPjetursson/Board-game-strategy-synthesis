package fftlib;

import fftlib.GGPAutogen.Database;
import fftlib.GGPAutogen.GGPMapping;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.game.FFTStateAndMove;
import fftlib.game.FFTStateMapping;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;


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

    public boolean verify(int team, boolean complete) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        return verify(team, complete, null);
    }
/*
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
*/

    public boolean verify(int team, boolean complete, HashMap<MachineState, GGPMapping> strategy) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        if (team == PLAYER_ANY)
            return verify(PLAYER1, complete, strategy) && verify(PLAYER2, complete, strategy);
        MachineState initialState = FFTManager.sm.getInitialState();
        LinkedList<MachineState> frontier = new LinkedList<>();
        HashSet<MachineState> closedSet = new HashSet<>();
        frontier.add(initialState);
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        // Check if win or draw is even possible
        int score = Database.queryState(initialState).getScore();
        if (team == PLAYER1 && score < -1000) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && score > 1000) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }

        while (!frontier.isEmpty()) {
            MachineState state = frontier.pop();
            Role currRole = FFTManager.getStateRole(state);
            int currTurn = FFTManager.roleToPlayer(currRole);
            if (FFTManager.sm.isTerminal(state)) {
                List<Integer> winners = FFTManager.getPlayerWinners(state);
                if (winners == null) {// should not happen
                    return false;
                }

                if (winners.size() == 1 && winners.get(0) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            } else if (team != currTurn) {
                for (MachineState child : FFTManager.sm.getNextStates(state))
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                Move move = apply(state);
                Set<Move> nonLosingMoves = Database.nonLosingMoves(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {

                    if (!complete && strategy != null) { // only expand on move from strategy
                        GGPMapping mapping = strategy.get(state);
                        if (mapping == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        MachineState nextState = FFTManager.getNextState(state, mapping.getMove());
                        if (!closedSet.contains(nextState)) {
                            closedSet.add(nextState);
                            frontier.add(nextState);
                        }
                        continue;
                    }
                    for (Move m : FFTManager.sm.getLegalMoves(state, currRole)) {
                        if (nonLosingMoves.contains(m)) {
                            MachineState nextState = FFTManager.getNextState(state, m);
                            if (!closedSet.contains(nextState)) {
                                closedSet.add(nextState);
                                frontier.add(nextState);
                            }
                        } else if (complete) {
                            //failingPoint = new FFTStateAndMove(state, m, true);
                            return false;
                        }
                    }
                } else if (!nonLosingMoves.contains(move)) {
                    //failingPoint = new FFTStateAndMove(state, move, false);
                    return false;
                } else {
                    if (!complete && strategy != null) { // check that move is same as from strategy
                        GGPMapping mapping = strategy.get(state);
                        if (mapping == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        if (!mapping.getMove().equals(move)) { // DOESN'T WORK BECAUSE OF TRANSFORMATIONS!
                            return false;
                        }

                    }
                    MachineState nextState = FFTManager.getNextState(state, move);
                    if (!closedSet.contains(nextState)) {
                        closedSet.add(nextState);
                        frontier.add(nextState);
                    }
                }
            }
        }
        return true;
    }


    public Move apply(MachineState state) throws MoveDefinitionException {
        for (RuleGroup ruleGroup : ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                    Move move = rule.apply(state);
                    if (move != null) {
                        return move;
                    }
            }
        }
        return null;
    }
/*
    public FFTMove apply(FFTState state) {
        for (RuleGroup rg : ruleGroups) {
            for (Rule r : rg.rules) {
                FFTMove m = r.apply(state);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }
    */
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

    public int getAmountOfSentences() {
        int precSize = 0;
        for (RuleGroup rg : ruleGroups) {
            precSize += rg.getAmountOfPreconditions();
        }
        return precSize;
    }

    public int minimize(int perspective, boolean minimize_precons) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException { // Returns amount of iterations
        if (!verify(perspective, true)) {
            System.out.println("FFT is not a winning strategy, so it can not be minimized");
            return -1;
        }

        int ruleSize = getAmountOfRules();
        int precSize = getAmountOfPreconditions();

        minimizeRules(perspective);
        if (minimize_precons)
            minimizePreconditions(perspective);


        int minRuleSize = getAmountOfRules();
        int minPrecSize = getAmountOfPreconditions();

        int i = 0;
        while (ruleSize != minRuleSize || precSize != minPrecSize) {
            ruleSize = minRuleSize;
            precSize = minPrecSize;

            minimizeRules(perspective);
            if (minimize_precons)
                minimizePreconditions(perspective);

            minRuleSize = getAmountOfRules();
            minPrecSize = getAmountOfPreconditions();
            i++;
        }
        return i;
    }

    private ArrayList<Rule> minimizeRules(int team) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
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
/*
    private void minimizePreconditions(int team) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
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
*/
    private void minimizePreconditions(int team) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        for (RuleGroup rg : ruleGroups) {
            for(Rule r : rg.rules) {
                Set<GdlSentence> sentences = new HashSet<>();
                for (GdlSentence s : r.sentences)
                    sentences.add(s.clone());

                for (GdlSentence s : sentences) {
                    r.removePrecondition(s);
                    if (!verify(team, true))
                        r.addPrecondition(s);
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RuleGroup rg : ruleGroups) {
            sb.append(rg.name).append(":\n");
            for (Rule r : rg.rules)
                sb.append(r).append("\n");
        }
        return sb.toString();
    }
}
