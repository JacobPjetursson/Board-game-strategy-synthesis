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

    public static FFT generateFFT(int team_) {
        // temp code
        /*
        Scanner block = new Scanner(System.in);
        System.out.println("Press any button to continue");
        block.nextLine();
         */

        AUTOGEN_TEAM = team_;

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

        int i = fft.minimize(AUTOGEN_TEAM, Config.MINIMIZE_PRECONDITIONS);
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

            int prevSize = reachableRelevantStates.size();
            Rule r = addRule(state);
            if (prevSize == reachableRelevantStates.size()) {
                System.out.println("SIZE UNCHANGED AFTER ADDING RULE!");
                System.exit(1);
            }

            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();
        }
    }

    private static Rule addRule(FFTState s) {
        HashSet<Literal> minSet = new HashSet<>();
        HashSet<Literal> literals = s.getLiterals();
        StateMapping mapping = lookupTable.get(s);
        Action bestAction = mapping.getMove().getAction();

        for (Literal l : literals)
            minSet.add(new Literal(l));

        Rule r = new Rule(minSet, bestAction);
        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL STATE: " + s + " , AND MOVE: " + mapping.getMove());
            System.out.println("ORIGINAL PRECONDITIONS: " + s.getLiterals());
            System.out.println("ORIGINAL SCORE: " + mapping.getScore());
        }

        for (Literal l : literals) {
            if (DETAILED_DEBUG) System.out.println("ATTEMPING TO REMOVE: " + l.getName());
            r.removePrecondition(l);
            boolean verified = verifyRule(r, false);

            if (!verified) {
                if (DETAILED_DEBUG) System.out.println("FAILED TO REMOVE: " + l.getName());
                r.addPrecondition(l);
            } else {
                if (DETAILED_DEBUG) System.out.println("REMOVING PRECONDITION: " + l.getName());
            }
            if (DETAILED_DEBUG) System.out.println("RULE IS NOW: " + r);
        }
        if (DETAILED_DEBUG) System.out.println("DOING SAFE RUN");
        verifyRule(r, true); // safe run where we know we have the final rule
        return r;
    }

    private static boolean verifyRule(Rule r, boolean safe) {
        HashMap<FFTState, FFTMove> appliedMap = new HashMap<>();
        HashSet<FFTState> suboptimalSet = new HashSet<>(); // contains states with sub-optimal moves
        HashMap<FFTState, Boolean> deleteMap = new HashMap<>();
        HashMap<FFTState, FFTMove> undoMap = new HashMap<>();

        for (FFTState s : reachableRelevantStates) {
            // TODO - This takes most of the time, can we optimize it?
            FFTMove m = r.apply(s);
            if (m != null) { // this is equivalent to checking that rule applies with legal move
                appliedMap.put(s, m);
            }
        }
        System.out.println("AppliedMap size: " + appliedMap.size());

        for (Map.Entry<FFTState, FFTMove> entry : appliedMap.entrySet()) {
            FFTState s = entry.getKey();
            FFTMove m = entry.getValue();
            //System.out.println("Updating set from state: " + s + ", and move: " + m);
            updateSets(s, m, suboptimalSet, deleteMap, undoMap);
            deleteMap.putIfAbsent(s, false);
        }

        System.out.println("deleteMap size: " + deleteMap.size());
        System.out.println("undoMap size: " + undoMap.size());

        for (FFTState s : suboptimalSet) {
            if (s.isReachable()) {
                undoReachableParents(undoMap);
                return false;
            }
        }

        /*
        if (!fft.verify(AUTOGEN_TEAM, false)) {
            System.out.println("ERROR: Old verification failed where new did not");
            System.out.println("Failing point: " + fft.failingPoint);
            System.exit(1);
            return false;
        }
        */

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
            //System.out.println("state is not reachable");
            return;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(s);
        if (chosenMove != null && !optimalMoves.contains(chosenMove)) { // we choose wrong move
            //System.out.println("state chooses suboptimal move, adding to suboptimal set");
            suboptimalSet.add(s);
            return;
        }
        //System.out.println("Removing pointers to all children except chosenMove, adding state: " + s + " , and move: " + chosenMove + ", to undoMap");

        undoMap.put(s, chosenMove);
        for (FFTMove m : optimalMoves) { // remove pointer to all children except chosen move
            //System.out.println("Next state: " + s.getNextState(m) + ", from move: " + m);
            if (m.equals(chosenMove)) { // chosen move
                continue;
            }
            FFTState existingChild = reachableStates.get(s.getNextState(m));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            // might also be the case that we already removed it once (deleteMap check)
            if (existingChild == null || deleteMap.getOrDefault(existingChild, false)) {
                //System.out.println("Existing child null or part of deleteMap");
                continue;
            }
            //System.out.println("Removing ptr to child from optimal move: " + existingChild);
            existingChild.removeReachableParent(s);
            //System.out.println("remaining pointers: " + existingChild.getReachableParents());
            if (!existingChild.isReachable()) {
                //System.out.println("Child no longer reachable, removing from set and checking recursively");
                deleteMap.put(existingChild, true);
                updateSets(existingChild, null, suboptimalSet, deleteMap, undoMap); // FIXME - tail-end?
            }
        }
    }

    private static void deleteUnreachableStates(HashMap<FFTState, Boolean> deleteMap) {
        //System.out.println("Deleting unreachable states");
        for (Map.Entry<FFTState, Boolean> entry : deleteMap.entrySet()) {
            FFTState key = entry.getKey();
            if (entry.getValue()) { // del from both
                reachableStates.remove(key);
            }
            reachableRelevantStates.remove(key);
        }
    }

    private static void undoReachableParents(HashMap<FFTState, FFTMove> undoMap) {
        //System.out.println("Undoing deletions of reachable parents");
        for (Map.Entry<FFTState, FFTMove> entry : undoMap.entrySet()) {
            FFTState state = entry.getKey();
            FFTMove chosenMove = entry.getValue(); // may be null (if we want to add link to all children)
            for (FFTMove move : FFTSolution.optimalMoves(state)) {
                if (move.equals(chosenMove))
                    continue;
                FFTState child = reachableStates.get(state.getNextState(move));
            //    System.out.println("For state: " + child + " , adding back parent: " + state);
                // child can be null if choiceMove is not null
                if (child != null) {
                    child.addReachableParent(state);

                }
            }
        }
    }

    private static void initializeSets() {
        int team = AUTOGEN_TEAM;
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
            int s1_precons_amount = s1.getLiterals().size(); // TODO - doesnt
            int s2_precons_amount = s2.getLiterals().size();
            if (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST)
                return s1_precons_amount - s2_precons_amount;
            return s2_precons_amount - s1_precons_amount;
        }
    }

}

