package fftlib.game;

import fftlib.*;
import fftlib.FFT;
import misc.Config;
import misc.Globals;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.*;

import static misc.Config.*;
import static misc.Globals.*;

public class FFTAutoGen {
    private static HashMap<? extends FFTState, ? extends FFTStateMapping> lookupTable;

    private static PriorityQueue<FFTState> states;

    private static HashMap<FFTState, FFTStateMapping> strategy;
    private static FFT fft;
    private static RuleGroup rg;

    // CONFIGURATION
    private static int winner;

    public static FFT generateFFT(int perspective_, int winner_) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        AUTOGEN_PERSPECTIVE = perspective_;
        winner = winner_;
        setup();
        return fft;
    }

    private static void setup() throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        long timeStart = System.currentTimeMillis();
        fft = new FFT("Autogen");
        rg = new RuleGroup("Autogen");
        fft.addRuleGroup(rg);
        lookupTable = FFTManager.db.getSolution();

        System.out.println("Solution size: " + lookupTable.size());

        states = new PriorityQueue<>(new StateComparator());

        System.out.println("Filtering...");
        if (VERIFY_SINGLE_STRATEGY) {
            strategy = new HashMap<>();
            System.out.println("Filtering for single strategy");
            filterToSingleStrat();
        } else {
            System.out.println("Filtering for all strategies");
            filterSolution();
            deleteIrrelevantStates();
        }
        System.out.println("Amount of states after filtering: " + states.size());

        System.out.println("Making rules");
        makeRules();

        System.out.println("Amount of rules before minimizing: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + fft.getAmountOfPreconditions());
        int i = fft.minimize(AUTOGEN_PERSPECTIVE, Config.MINIMIZE_PRECONDITIONS);
        System.out.println("Amount of rules after " + i + " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions after " + i + " minimize iterations: " + fft.getAmountOfPreconditions());

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
    }


    private static void filterSolution() {
        for (Map.Entry<? extends FFTState, ? extends FFTStateMapping> entry : lookupTable.entrySet()) {
            FFTState state = entry.getKey();
            FFTStateMapping play = entry.getValue();
            if (AUTOGEN_PERSPECTIVE == PLAYER1 && play.getMove().getTeam() != PLAYER1)
                continue;
            else if (AUTOGEN_PERSPECTIVE == PLAYER2 && play.getMove().getTeam() != PLAYER2)
                continue;

            boolean threshold = false;
            switch(AUTOGEN_PERSPECTIVE) {
                case PLAYER1:
                    threshold = play.getWinner() == PLAYER1 || winner == play.getWinner();
                    break;
                case PLAYER2:
                    threshold = play.getWinner() == PLAYER2 || winner == play.getWinner();
                    break;
                case PLAYER_ANY:
                    if (play.getMove().getTeam() == PLAYER1 && (play.getWinner() == PLAYER1 || play.getWinner() == PLAYER_NONE))
                        threshold = true;

                    else if (play.getMove().getTeam() == PLAYER2 &&
                            (play.getWinner() == PLAYER2 || play.getWinner() == PLAYER_NONE))
                        threshold = true;
                    break;
            }
            if (threshold) {
                states.add(state);
            }
        }
    }

    // States where all moves are correct should not be part of FFT
    private static void deleteIrrelevantStates() {
        Iterator<FFTState> itr = states.iterator();
        while (itr.hasNext()) {
            FFTState s = itr.next();
            ArrayList<? extends FFTMove> nonLosingMoves = FFTManager.db.nonLosingMoves(s);
            if (nonLosingMoves.size() == s.getLegalMoves().size()) {
                itr.remove();
            }
        }
    }

    private static void filterToSingleStrat() {
        LinkedList<FFTState> frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        FFTState initialState = FFTManager.initialFFTState;
        frontier.add(initialState);
        while (!frontier.isEmpty()) {
            FFTState state = frontier.pop();
            if (FFTManager.logic.gameOver(state))
                continue;
            if (AUTOGEN_PERSPECTIVE != state.getTurn()) {
                for (FFTState child : state.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                states.add(state);
                FFTStateMapping info = lookupTable.get(state);
                strategy.put(state, info);
                FFTState nextState = state.getNextState(info.getMove());
                if (!closedSet.contains(nextState)) {
                    closedSet.add(nextState);
                    frontier.add(nextState);
                }
            }
        }
    }

    private static void makeRules() throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        while (!states.isEmpty()) {
            System.out.println("Remaining states: " + states.size() + ". Current amount of rules: " + rg.rules.size());
            FFTState state = states.iterator().next();

            Rule r = addRule(state);
            states.remove(state);
            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();


            if (fft.verify(AUTOGEN_PERSPECTIVE, true)) {
                System.out.println("FFT verified before empty statespace");
                return;
            }

            // Delete states that apply
            states.removeIf(s -> r.apply(s) != null);

        }

    }

    private static Rule addRule(FFTState s) {
        HashSet<Literal> minSet = new HashSet<>();
        HashSet<Literal> literals = s.getAllLiterals();
        ArrayList<Action> actions = new ArrayList<>();
        ArrayList<Action> actionsCopy;
        FFTStateMapping mapping = lookupTable.get(s);
        Action bestAction = mapping.getMove().getAction();

        for (Literal l : literals)
            minSet.add(new Literal(l));

        for (FFTMove m : FFTManager.db.nonLosingMoves(s))
            actions.add(m.getAction());
        actionsCopy = new ArrayList<>(actions);

        if (FULL_RULES)
            return new Rule(minSet, actions.get(0));

        Rule r;
        if (!GREEDY_AUTOGEN) {
            r = new Rule();
            r.setPreconditions(new Clause(minSet));
        } else {
            r = new Rule(minSet, bestAction);
        }

        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.print("ORIGINAL LITERALS: ");
            for (Literal l : literals)
                System.out.print(l.name + " ");
            System.out.println();
            System.out.println("ORIGINAL STATE: " + s);
            System.out.println("ORIGINAL MOVE: " + mapping.getMove());
            System.out.println("ORIGINAL SCORE: " + mapping.getScore());
        }

        if (!GREEDY_AUTOGEN) {
            LinkedList<Literal> copy = new LinkedList<>(literals);
            LinkedList<Literal> bestPath = new LinkedList<>();
            Action chosenAction = null;
            for (Action a : actionsCopy) {
                r.setAction(a);
                LinkedList<Literal> path = getBestRemovalPath(copy, r, new LinkedList<>());
                if (path.size() > bestPath.size()) {
                    chosenAction = a;
                    bestPath = path;
                }
                if (path.size() >= FFTManager.MAX_PRECONS) // Everything has been removed
                    break;
            }
            r.setAction(chosenAction);
            if (DETAILED_DEBUG) {
                System.out.println("Best removal path (removed " + bestPath.size() + "): ");
                for (Literal l : bestPath)
                    System.out.print(l.name + " ");
                System.out.println();
            }
            for (Literal l : bestPath)
                r.removePrecondition(l);
            System.out.println();
        }
        else {
            for (Literal l : literals) {
                if (DETAILED_DEBUG) System.out.println("INSPECTING: " + l.name);
                r.removePrecondition(l);

                boolean verify = fft.verify(AUTOGEN_PERSPECTIVE, false, null); // strategy is null if VERIFY_SINGLE_STRAT is false
                if (!verify) {
                    if (DETAILED_DEBUG) System.out.println("FAILED TO VERIFY RULE!");
                    r.addPrecondition(l);
                } else if (DETAILED_DEBUG)
                    System.out.println("REMOVING: " + l.name);
            }
        }

        return r;
    }

    private static LinkedList<Literal> getBestRemovalPath(LinkedList<Literal> literals, Rule r, LinkedList<Literal> path) {
        ListIterator<Literal> it = literals.listIterator();
        LinkedList<Literal> bestPath = path;

        while(it.hasNext()) {
            Literal l = it.next();
            r.removePrecondition(l);
            boolean verify = fft.verify(AUTOGEN_PERSPECTIVE, false, null);
            if (!verify) {
                r.addPrecondition(l);
                continue;
            }
            it.remove();

            LinkedList<Literal> copy = new LinkedList<>(path);
            copy.add(l);
            LinkedList<Literal> p = getBestRemovalPath(new LinkedList<>(literals), r, copy);

            if (p.size() > bestPath.size())
                bestPath = p;

            it.add(l);
            r.addPrecondition(l);

            if (bestPath.size() >= FFTManager.MAX_PRECONS) // Everything removed, no better path exists
                break;
        }
        return bestPath;
    }

    private static class StateComparator implements Comparator<FFTState>{
        @Override
        public int compare(FFTState s1, FFTState s2) {
            if (RANDOM_RULE_ORDERING)
                return 0;
            int s1_score = lookupTable.get(s1).getScore();
            int s2_score = lookupTable.get(s2).getScore();

            if (Math.abs(s1_score) > 1000)
                s1_score = Math.abs(Math.abs(s1_score) - 2000);
            if (Math.abs(s2_score) > 1000)
                s2_score = Math.abs(Math.abs(s2_score) - 2000);

            return s1_score - s2_score; // s1 - s2 means states closer to terminal first
        }
    }

}