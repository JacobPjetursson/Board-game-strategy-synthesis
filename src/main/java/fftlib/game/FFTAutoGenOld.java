package fftlib.game;

import fftlib.*;
import fftlib.FFT;
import misc.Config;

import java.util.*;

import static misc.Config.*;
import static misc.Globals.*;

public class FFTAutoGenOld {
    private static HashMap<? extends FFTState, ? extends StateMapping> lookupTable;

    private static PriorityQueue<FFTState> states;

    private static HashMap<FFTState, StateMapping> strategy;
    private static FFT fft;
    private static RuleGroup rg;

    private static int winner;
    private static int max_precons;

    public static FFT generateFFT(int team_) {
        AUTOGEN_TEAM = team_;
        fft = new FFT("Synthesis");
        long timeStart = System.currentTimeMillis();
        setup();
        start();
        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
        System.out.println("Final rules: \n" + fft);
        return fft;
    }

    // use already existing FFT (weakly optimal)
    public static FFT generateFFT(int team_, FFT currFFT) {
        AUTOGEN_TEAM = team_;
        fft = currFFT;
        // check if existing FFT is weakly optimal
        if (!fft.verify(team_, false)) {
            System.out.println("Existing FFT is not weakly optimal. Returning");
            return fft;
        }

        long timeStart = System.currentTimeMillis();
        setup();
        /* TODO - why doesn't it work?
        System.out.println("Removing states that apply to current rules");
        if (!GENERATE_ALL_RULES)
            states.removeIf(s -> {
                for (Rule r : fft.getRules())
                    if (r.apply(s) != null)
                        return true;
                return false;
            });
        */
        start();
        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
        System.out.println("Final rules: \n" + fft);
        return fft;
    }

    private static void setup() {
        rg = new RuleGroup("Synthesis");
        fft.addRuleGroup(rg);
        lookupTable = FFTSolution.getLookupTable();
        winner = FFTSolution.getWinner();
        max_precons = FFTManager.initialFFTState.getLiterals().size();
        fft.USE_STRATEGY = SAVE_STRAT; // Only use this feature if autogenerating, e.g. not when simply loading from file

        System.out.println("Solution size: " + lookupTable.size());

        states = new PriorityQueue<>(new StateComparator());

        if (VERIFY_SINGLE_STRATEGY) {
            strategy = new HashMap<>();
            System.out.println("Filtering for single strategy");
            filterToSingleStrat();
        } else {
            System.out.println("Filtering for all strategies");
            filterSolution();
            if (USE_FILTERING)
                deleteIrrelevantStates();
        }
        System.out.println("Amount of states after filtering: " + states.size());
    }

    private static void start() {
        System.out.println("Making rules");
        makeRules();

        System.out.println("Amount of rules before minimizing: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + fft.getAmountOfPreconditions());
        int i = fft.minimize(AUTOGEN_TEAM, Config.MINIMIZE_PRECONDITIONS);
        System.out.println("Final amount of rules after " + i + " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i + " minimize iterations: " + fft.getAmountOfPreconditions());
    }

    // TODO - this does not filter enough, e.g. it doesn't filter states that can only be reached with sub-optimal moves from player
    // TODO - instead, do an initial game tree search to identify all reachable states, which will be subset of this
    private static void filterSolution() {
        for (Map.Entry<? extends FFTState, ? extends StateMapping> entry : lookupTable.entrySet()) {
            FFTState state = entry.getKey();
            StateMapping mapping = entry.getValue();
            if (!USE_FILTERING) {
                states.add(state);
                continue;
            }
            if (AUTOGEN_TEAM == PLAYER1 && mapping.getMove().getTeam() != PLAYER1)
                continue;
            else if (AUTOGEN_TEAM == PLAYER2 && mapping.getMove().getTeam() != PLAYER2)
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
                    if (mapping.getMove().getTeam() == PLAYER1 && (mapping.getWinner() == PLAYER1 || mapping.getWinner() == PLAYER_NONE))
                        threshold = true;

                    else if (mapping.getMove().getTeam() == PLAYER2 &&
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
    private static void deleteIrrelevantStates() {
        Iterator<FFTState> itr = states.iterator();
        while (itr.hasNext()) {
            FFTState s = itr.next();
            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(s);
            if (optimalMoves.size() == s.getLegalMoves().size()) {
                itr.remove();
            }
        }
    }

    private static void makeRules() {
        while (!states.isEmpty()) {
            System.out.println("Remaining states: " + states.size() + ". Current amount of rules: " + rg.rules.size());
            FFTState state = states.iterator().next();

            if (!GENERATE_ALL_RULES && fft.verify(AUTOGEN_TEAM, true)) {
                System.out.println("FFT verified before empty statespace");
                return;
            }

            Rule r = addRule(state);
            // Run partial verification again to insert new rule mappings. Also return if no longer weakly optimal
            fft.SAFE_RUN = true;
            if (!fft.verify(AUTOGEN_TEAM, false)) {
                System.out.println("FFT is somehow no longer weakly optimal, returning");
                return;
            }

            fft.SAFE_RUN = false;

            states.remove(state);
            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();

            // Delete states that apply
            if (!GENERATE_ALL_RULES)
                states.removeIf(s -> r.apply(s) != null);

        }
    }

    private static Rule addRule(FFTState s) {
        HashSet<Literal> minSet = new HashSet<>();
        HashSet<Literal> literals = s.getLiterals();
        StateMapping mapping = lookupTable.get(s);
        Action bestAction = mapping.getMove().getAction();

        for (Literal l : literals)
            minSet.add(new Literal(l));

        if (GENERATE_ALL_RULES) {
            Rule r = new Rule(minSet, bestAction);
            rg.rules.add(r);
            return r;
        }

        Rule r;
        if (!GREEDY_AUTOGEN) {
            r = new Rule();
            r.setPreconditions(minSet);
        }
        else {
            r = new Rule(minSet, bestAction);
        }

        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.print("ORIGINAL LITERALS: ");
            for (Literal l : literals)
                System.out.print(l.getName() + " ");
            System.out.println();
            System.out.println("ORIGINAL STATE: " + s);
            System.out.println("ORIGINAL MOVE: " + mapping.getMove());
            System.out.println("ORIGINAL SCORE: " + mapping.getScore());
        }

        if (!GREEDY_AUTOGEN) {
            ArrayList<Action> actions = new ArrayList<>();
            ArrayList<Action> actionsCopy;
            for (FFTMove m : FFTSolution.optimalMoves(s))
                actions.add(m.getAction());
            actionsCopy = new ArrayList<>(actions);

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
                if (path.size() >= max_precons) // Everything has been removed
                    break;
            }
            r.setAction(chosenAction);
            if (DETAILED_DEBUG) {
                System.out.println("Best removal path (removed " + bestPath.size() + "): ");
                for (Literal l : bestPath)
                    System.out.print(l.getName() + " ");
                System.out.println();
            }
            for (Literal l : bestPath)
                r.removePrecondition(l);
            System.out.println();
        }
        else {
            for (Literal l : literals) {
                if (DETAILED_DEBUG) System.out.println("INSPECTING: " + l.getName());
                r.removePrecondition(l);

                boolean verify = fft.verify(AUTOGEN_TEAM, false);
                if (!verify) {
                    if (DETAILED_DEBUG) System.out.println("FAILED TO VERIFY RULE!");
                    r.addPrecondition(l);
                } else if (DETAILED_DEBUG)
                    System.out.println("REMOVING: " + l.getName());
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
            boolean verify = fft.verify(AUTOGEN_TEAM, false);
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

            if (bestPath.size() >= max_precons) // Everything removed, no better path exists
                break;
        }
        return bestPath;
    }

    private static class StateComparator implements Comparator<FFTState>{
        @Override
        public int compare(FFTState s1, FFTState s2) {
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
            int s1_precons_amount = s1.getLiterals().size(); // TODO - doesn't work
            int s2_precons_amount = s2.getLiterals().size();
            if (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST)
                return s1_precons_amount - s2_precons_amount;
            return s2_precons_amount - s1_precons_amount;
        }
    }

    private static void filterToSingleStrat() {
        LinkedList<FFTState> frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        Random r = new Random();
        if (RANDOM_SEED) r.setSeed(SEED);
        FFTState initialState = FFTManager.initialFFTState;
        frontier.add(initialState);
        while (!frontier.isEmpty()) {
            FFTState state = frontier.pop();
            if (FFTManager.logic.gameOver(state))
                continue;
            if (AUTOGEN_TEAM != state.getTurn()) {
                for (FFTState child : state.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                states.add(state);
                /* Taking random optimal move never works for some reason
                ArrayList<? extends FFTMove> moves = FFTSolution.optimalMoves(state);
                FFTMove move = moves.get(r.nextInt(moves.size()));
                int score = lookupTable.get(state).score;
                int depth = lookupTable.get(state).depth;
                strategy.put(state, new StateMapping(move, score, depth));
                FFTState nextState = state.getNextState(move);
                 */
                StateMapping mapping = lookupTable.get(state);
                strategy.put(state, mapping);
                FFTState nextState = state.getNextState(mapping.getMove());
                if (!closedSet.contains(nextState)) {
                    closedSet.add(nextState);
                    frontier.add(nextState);
                }
            }
        }
        fft.setSingleStrategy(strategy);
    }

}
