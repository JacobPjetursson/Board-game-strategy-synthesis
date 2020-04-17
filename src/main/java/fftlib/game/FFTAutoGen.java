package fftlib.game;

import fftlib.FFT;
import fftlib.*;
import misc.Config;

import java.util.*;

import static misc.Config.*;
import static misc.Globals.*;

public class FFTAutoGen {
    private static HashMap<? extends FFTState, ? extends StateMapping> lookupTable;
    // States that are reachable when playing with a partial or total strategy
    private static HashMap<FFTState, FFTState> reachableStates; // TODO - consider doing (long, FFTState)
    // Subset of reachable states where strategy does not output a move, and player has the turn
    // This is the set of states that a new rule can potentially influence
    private static HashSet<FFTState> reachableRelevantStates;

    private static FFT fft;
    private static RuleGroup rg;

    public static FFT generateFFT(int perspective_) {
        // temp code
        /*
        Scanner block = new Scanner(System.in);
        System.out.println("Press any button to continue");
        block.nextLine();
         */

        AUTOGEN_PERSPECTIVE = perspective_;

        if (BENCHMARK_MODE) {
            double avgRules = 0, avgPrecons = 0, avgTime = 0;
            for (int i = 0; i < BENCHMARK_NUMBER; i++) {
                long timeStart = System.currentTimeMillis();
                generate();
                avgTime += (System.currentTimeMillis() - timeStart) / 1000.0;
                avgRules += fft.getAmountOfRules();
                avgPrecons += fft.getAmountOfPreconditions();
            }
            avgTime /= BENCHMARK_NUMBER;
            avgRules /= BENCHMARK_NUMBER;
            avgPrecons /= BENCHMARK_NUMBER;
            System.out.println("Average time: " + avgTime);
            System.out.println("Average no. of rules: " + avgRules);
            System.out.println("Average no. of preconditions: " + avgPrecons);
        }
        else {
            generate();
        }

        return fft;
    }


    private static void setup() {
        fft = new FFT("Synthesis");
        rg = new RuleGroup("Synthesis");
        fft.addRuleGroup(rg);
        lookupTable = FFTSolution.getLookupTable();
        fft.USE_STRATEGY = false; // TODO - remove somehow

        reachableStates = new HashMap<>();
        reachableRelevantStates = new HashSet<>();

        // Set reachable parents for all states
        initializeSets();

        System.out.println("Solution size: " + lookupTable.size());
        System.out.println("Number of reachable states: " + reachableStates.size());
        System.out.println("Number of reachable relevant states: " + reachableRelevantStates.size());
    }

    private static void generate() {
        long timeStart = System.currentTimeMillis();

        setup();
        System.out.println("Making rules");
        makeRules();
        System.out.println("Amount of rules before minimizing: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + fft.getAmountOfPreconditions());
        if (DETAILED_DEBUG) System.out.println("Rules before minimizing");
        if (DETAILED_DEBUG) System.out.println(fft);

        int i = fft.minimize(AUTOGEN_PERSPECTIVE, Config.MINIMIZE_PRECONDITIONS);
        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;

        System.out.println("Final amount of rules after " + i + " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i + " minimize iterations: " + fft.getAmountOfPreconditions());
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
        System.out.println("Final rules: \n" + fft);

    }

    private static void makeRules() {
        // TODO - try with iterator hasNext() and then skip states with all moves optimal
        while (!reachableRelevantStates.isEmpty()) {
            System.out.println("Remaining relevant states: " + reachableRelevantStates.size() + ". Current amount of rules: " + rg.rules.size());
            FFTState state = reachableRelevantStates.iterator().next();

            Rule r = addRule(state);

            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();
        }
    }

    private static Rule addRule(FFTState s) {
        HashSet<Literal> minSet = new HashSet<>();
        HashSet<Literal> literals = s.getAllLiterals();
        StateMapping mapping = lookupTable.get(s);
        Action bestAction = mapping.getMove().getAction();

        for (Literal l : literals)
            minSet.add(new Literal(l));

        Rule r = new Rule(minSet, bestAction);
        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL STATE: " + s + " , AND MOVE: " + mapping.getMove());
            System.out.println("ORIGINAL SCORE: " + mapping.getScore());
        }

        for (Literal l : literals) {
            if (DETAILED_DEBUG) System.out.println("ATTEMPING TO REMOVE: " + l.name);
            r.removePrecondition(l);
            boolean verified = verifyRule(r, false);

            if (!verified) {
                if (DETAILED_DEBUG) System.out.println("FAILED TO REMOVE: " + l.name);
                r.addPrecondition(l);
            } else {
                if (DETAILED_DEBUG) System.out.println("REMOVING PRECONDITION: " + l.name);
            }
            if (DETAILED_DEBUG) System.out.println("RULE IS NOW: " + r);
        }

        verifyRule(r, true); // safe run where we know we have the final rule
        return r;
    }

    private static boolean verifyRule(Rule r, boolean safe) {
        HashMap<FFTState, FFTMove> appliedMap = new HashMap<>();
        HashSet<FFTState> suboptimalSet = new HashSet<>(); // contains states with sub-optimal moves
        HashMap<FFTState, Boolean> deleteMap = new HashMap<>();
        HashMap<FFTState, FFTMove> undoMap = new HashMap<>();

        for (FFTState s : reachableRelevantStates) {
            // TODO - can we optimize this query?
            FFTMove m = r.apply(s);
            if (m != null) { // this is equivalent to checking that rule applies with legal move
                appliedMap.put(s, m);
            }
        }

        for (Map.Entry<FFTState, FFTMove> entry : appliedMap.entrySet()) {
            FFTState s = entry.getKey();
            FFTMove m = entry.getValue();
            updateSets(s, m, suboptimalSet, deleteMap, undoMap);
            deleteMap.putIfAbsent(s, false);
        }

        for (FFTState s : suboptimalSet) {
            if (s.isReachable()) {
                return false;
            }
        }

        if (safe) {
            deleteUnreachableStates(deleteMap);
        } else {
            undoReachableParents(undoMap);
        }
        return true;
    }

    // Either remove states from applySet in addition to reachableSet, or check whether state is still reachable
    private static void updateSets(FFTState s, FFTMove chosenMove,
                                   HashSet<FFTState> suboptimalSet, HashMap<FFTState, Boolean> deleteMap,
                                   HashMap<FFTState, FFTMove> undoMap) {
        // s might've been set unreachable by another state in appliedSet
        if (chosenMove != null && !s.isReachable()) {
            return;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(s);
        if (chosenMove != null && !optimalMoves.contains(chosenMove)) { // we choose wrong move
            suboptimalSet.add(s);
            return;
        }
        undoMap.put(s, chosenMove);
        for (FFTMove m : optimalMoves) { // remove pointer to all children except chosen move
            if (m.equals(chosenMove)) { // chosen move
                continue;
            }
            FFTState existingChild = reachableStates.get(s.getNextState(m));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            // might also be the case that we already removed it once (deleteMap check)
            if (existingChild == null || deleteMap.getOrDefault(existingChild, false)) {
                continue;
            }
            existingChild.removeReachableParent(s);
            if (!existingChild.isReachable()) {
                deleteMap.put(existingChild, true);
                updateSets(existingChild, null, suboptimalSet, deleteMap, undoMap); // FIXME - tail-end?
            }
        }
    }

    private static void deleteUnreachableStates(HashMap<FFTState, Boolean> deleteMap) {
        for (Map.Entry<FFTState, Boolean> entry : deleteMap.entrySet()) {
            FFTState key = entry.getKey();
            if (entry.getValue()) { // del from both
                reachableStates.remove(key);
            }
            reachableRelevantStates.remove(key);
        }
    }

    private static void undoReachableParents(HashMap<FFTState, FFTMove> undoMap) {
        for (Map.Entry<FFTState, FFTMove> entry : undoMap.entrySet()) {
            FFTState state = entry.getKey();
            FFTMove chosenMove = entry.getValue(); // may be null (if we want to add link to all children)
            for (FFTMove move : FFTSolution.optimalMoves(state)) {
                if (move.equals(chosenMove))
                    continue;
                FFTState child = reachableStates.get(state.getNextState(move));
                // child can be null if choiceMove is not null
                if (child != null) {
                    child.addReachableParent(state);

                }
            }
        }
    }

    private static void initializeSets() {
        int team = AUTOGEN_PERSPECTIVE;
        FFTState initialState = FFTManager.initialFFTState;
        LinkedList<FFTState> frontier;
        frontier = new LinkedList<>();
        frontier.add(initialState);
        reachableStates.put(initialState, initialState);
        reachableRelevantStates.add(initialState);

        while (!frontier.isEmpty()) {
            FFTState state = frontier.pop();
            // game over
            if (FFTManager.logic.gameOver(state))
                continue;
            // Not our turn
            if (team != state.getTurn()) {
                for (FFTState child : state.getChildren()) {
                    FFTState existingChild = reachableStates.get(child);
                    if (existingChild == null) {
                        reachableStates.put(child, child);
                        frontier.add(child);
                    }
                    // add reachableParent
                    reachableStates.get(child).addReachableParent(state);

                }
                continue;
            }
            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(state);
            reachableRelevantStates.add(state);

            // Our turn, add all states from optimal moves
            for (FFTMove m : optimalMoves) {
                FFTState child = state.getNextState(m);
                FFTState existingChild = reachableStates.get(child);
                if (existingChild == null) {
                    reachableStates.put(child, child);
                    frontier.add(child);
                }
                reachableStates.get(child).addReachableParent(state);

            }
        }
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
            int s1_precons_amount = s1.getLiterals().size();
            int s2_precons_amount = s2.getLiterals().size();
            if (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST)
                return s1_precons_amount - s2_precons_amount;
            return s2_precons_amount - s1_precons_amount;
        }
    }

}

