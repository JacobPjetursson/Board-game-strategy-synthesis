package fftlib.GGPAutogen;

import fftlib.FFT;
import fftlib.Rule;
import fftlib.RuleGroup;
import misc.Config;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.*;

import static misc.Config.*;
import static misc.Globals.*;

public class Runner {
    private static HashMap<MachineState, GGPMapping> lookupTable;

    private static PriorityQueue<MachineState> states;

    private static HashMap<MachineState, GGPMapping> strategy;
    private static FFT fft;
    private static RuleGroup rg;
    private static Role p1Role, p2Role;
    private static Move noop;

    private static int winner;
    private static int max_precons;

    public static void main(String[] args) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {

        String base_path = "src/main/java/fftlib/GGPAutogen/games/";
        String game_path = base_path + Config.GGP_GAME;
        // Set default properties
        Config.ENABLE_GGP = true;

        GGPManager.loadGDL(game_path);
        p1Role = GGPManager.p1role;
        p2Role = GGPManager.p2role;
        noop = GGPManager.noop;
        System.out.println("Gdl rules validated. Autogenerating FFT");
        
        setup();
    }


    private static void setup() throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        long timeStart = System.currentTimeMillis();
        fft = new FFT("Synthesis");
        rg = new RuleGroup("Synthesis");
        fft.addRuleGroup(rg);
        MachineState initialState = GGPManager.getInitialState();
        Solver solver = new Solver();
        lookupTable = solver.solve();

        GGPMapping result = lookupTable.get(initialState);
        winner = result.getWinner();
        max_precons = initialState.getContents().size();
        String winnerStr = winner == PLAYER1 ? "Player 1" : winner == PLAYER2 ? "Player 2" : "Draw";
        System.out.println("Winner of game: " + winnerStr);
        Database.initialize(lookupTable);
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
        // todo
        //int it = fft.minimize(AUTOGEN_TEAM, Config.MINIMIZE_PRECONDITIONS);
        int it = -1;
        System.out.println("Amount of rules after " + it + " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions after " + it + " minimize iterations: " + fft.getAmountOfPreconditions());

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
        System.out.println("Final rules: \n" + fft);


    }


    private static void filterSolution() {
        for (Map.Entry<MachineState, GGPMapping> entry : lookupTable.entrySet()) {
            MachineState state = entry.getKey();
            GGPMapping mapping = entry.getValue();
            if (AUTOGEN_TEAM == PLAYER1 && !mapping.getRole().equals(p1Role))
                continue;
            else if (AUTOGEN_TEAM == PLAYER2 && !mapping.getRole().equals(p2Role))
                continue;

            boolean threshold = false;
            switch(AUTOGEN_TEAM) {
                case PLAYER1:
                    threshold = mapping.getWinner() == PLAYER1 || winner == mapping.getWinner();
                    break;
                case PLAYER2:
                    threshold = mapping.getWinner() == PLAYER2 || winner == mapping.getWinner();
                    break;
                case PLAYER_ANY:
                    if (mapping.getRole().equals(p1Role) && (mapping.getWinner() == PLAYER1 || mapping.getWinner() == PLAYER_NONE))
                        threshold = true;

                    else if (mapping.getRole().equals(p2Role) &&
                            (mapping.getWinner() == PLAYER2 || mapping.getWinner() == PLAYER_NONE))
                        threshold = true;
                    break;
            }
            if (threshold) {
                states.add(state);
            }
        }
    }

    // States where all moves are correct should not be part of FFT
    private static void deleteIrrelevantStates() throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
        Iterator<MachineState> itr = states.iterator();
        while (itr.hasNext()) {
            MachineState ms = itr.next();
            Set<Move> optimalMoves = Database.optimalMoves(ms);
            Role r = GGPManager.getRole(ms);
            if (optimalMoves.size() == GGPManager.getLegalMoves(ms, r).size()) {
                itr.remove();
            }
        }
    }

    private static void filterToSingleStrat() throws MoveDefinitionException, TransitionDefinitionException {
        LinkedList<MachineState> frontier = new LinkedList<>();
        HashSet<MachineState> closedSet = new HashSet<>();
        MachineState initialState = (MachineState) GGPManager.getInitialState();
        frontier.add(initialState);
        while (!frontier.isEmpty()) {
            MachineState ms = frontier.pop();
            Role r = GGPManager.getRole(ms);
            if (GGPManager.isTerminal(ms))
                continue;
            if (AUTOGEN_TEAM != GGPManager.roleToPlayer(r)) {
                for (MachineState child : GGPManager.getNextStates(ms))
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                states.add(ms);
                GGPMapping mapping = lookupTable.get(ms);
                strategy.put(ms, mapping);
                Move m = mapping.getMove();
                MachineState nextState = GGPManager.getNextState(ms, m);
                if (!closedSet.contains(nextState)) {
                    closedSet.add(nextState);
                    frontier.add(nextState);
                }
            }
        }
    }

    private static void makeRules() throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        while (!states.isEmpty()) {

            System.out.println("Remaining states: " + states.size() + ". Current amount of rules: " + rg.rules.size());
            MachineState state = states.iterator().next();
            Rule r = addRule(state);
            states.remove(state);
            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();

            // todo
            if (!GENERATE_ALL_RULES && false) {//fft.verify(AUTOGEN_TEAM, true)) {
                System.out.println("FFT verified before empty statespace");
                return;
            }

            // Delete states that apply
            if (!GENERATE_ALL_RULES) {
                states.removeIf(s -> {
                    try {
                        return r.apply(s) != null;
                    } catch (MoveDefinitionException e) {
                        e.printStackTrace();
                        return false;
                    }
                });
            }
        }
    }

    private static Rule addRule(MachineState ms) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        Set<GdlSentence> sentences = ms.getContents();
        Set<GdlSentence> minSet = new HashSet<>();
        for (GdlSentence s : sentences) {
            minSet.add(s.clone());
        }
        GGPMapping mapping = lookupTable.get(ms);
        Move bestMove = mapping.getMove();


        ArrayList<Move> moves = new ArrayList<>(Database.optimalMoves(ms));
        ArrayList<Move> movesCopy = new ArrayList<>(moves);

        if (GENERATE_ALL_RULES) {
            Rule r = new Rule(minSet, bestMove);
            rg.rules.add(r);
            return r;
        }

        Rule r;
        if (!GREEDY_AUTOGEN) {
            r = new Rule(minSet);
        } else {
            r = new Rule(minSet, bestMove);
        }

        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL STATE: " + ms);
            System.out.println("ORIGINAL MOVE: " + mapping.getMove());
            System.out.println("ORIGINAL SCORE: " + mapping.getScore());
        }

        if (!GREEDY_AUTOGEN) {
            LinkedList<GdlSentence> copy = new LinkedList<>(sentences);
            LinkedList<GdlSentence> bestPath = new LinkedList<>();
            Move chosenMove = null;
            for (Move m : movesCopy) {
                r.setMove(m);
                LinkedList<GdlSentence> path = getBestRemovalPath(copy, r, new LinkedList<>());
                if (path.size() > bestPath.size()) {
                    chosenMove = m;
                    bestPath = path;
                }
                if (path.size() >= max_precons) // Everything has been removed
                    break;
            }
            r.setMove(chosenMove);
            if (DETAILED_DEBUG) {
                System.out.println("Best removal path (removed " + bestPath.size() + "): ");
                for (GdlSentence s : bestPath)
                    System.out.print(s + " ");
                System.out.println();
            }
            for (GdlSentence s : bestPath)
                r.removePrecondition(s);
        }
        else {
            for (GdlSentence s : sentences) {
                if (DETAILED_DEBUG) System.out.println("INSPECTING: " + s);
                r.removePrecondition(s);

                // todo
                boolean verify = false; // fft.verify(AUTOGEN_TEAM, false); // strategy is null if VERIFY_SINGLE_STRAT is false
                if (!verify) {
                    if (DETAILED_DEBUG) System.out.println("FAILED TO VERIFY RULE!");
                    r.addPrecondition(s);
                } else if (DETAILED_DEBUG)
                    System.out.println("REMOVING: " + s);
            }
        }

        return r;
    }

    private static LinkedList<GdlSentence> getBestRemovalPath(LinkedList<GdlSentence> sentences, Rule r, LinkedList<GdlSentence> path) {
        ListIterator<GdlSentence> it = sentences.listIterator();
        LinkedList<GdlSentence> bestPath = path;

        while(it.hasNext()) {
            GdlSentence s = it.next();
            r.removePrecondition(s);
            //todo
            boolean verify = false; //fft.verify(AUTOGEN_TEAM, false);
            if (!verify) {
                r.addPrecondition(s);
                continue;
            }
            it.remove();

            LinkedList<GdlSentence> copy = new LinkedList<>(path);
            copy.add(s);
            LinkedList<GdlSentence> p = getBestRemovalPath(new LinkedList<>(sentences), r, copy);

            if (p.size() > bestPath.size())
                bestPath = p;

            it.add(s);
            r.addPrecondition(s);

            if (bestPath.size() >= max_precons) { // Everything removed, no better path exists
                break;
            }
        }
        return bestPath;
    }

    private static class StateComparator implements Comparator<MachineState>{
        @Override
        public int compare(MachineState s1, MachineState s2) {
            if (RULE_ORDERING == RULE_ORDERING_RANDOM)
                return 0;

            if (RULE_ORDERING == RULE_ORDERING_TERMINAL_LAST ||
                    RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST) {
                int s1_score = lookupTable.get(s1).getScore();
                int s2_score = lookupTable.get(s2).getScore();

                if (Math.abs(s1_score) > 1000)
                    s1_score = Math.abs(Math.abs(s1_score) - 2000);
                if (Math.abs(s2_score) > 1000)
                    s2_score = Math.abs(Math.abs(s2_score) - 2000);

                if (RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST)
                    return s1_score - s2_score; // s1 - s2 means states closer to terminal first
                else if (RULE_ORDERING == RULE_ORDERING_TERMINAL_LAST)
                    return s2_score - s1_score;
            }
            int s1_precons_amount = s1.getContents().size();
            int s2_precons_amount = s2.getContents().size();
            if (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST)
                return s1_precons_amount - s2_precons_amount;
            return s2_precons_amount - s1_precons_amount;
        }
    }


}
