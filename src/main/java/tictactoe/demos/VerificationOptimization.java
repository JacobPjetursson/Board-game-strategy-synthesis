package tictactoe.demos;

import fftlib.FFT;
import fftlib.*;
import fftlib.game.*;
import misc.Config;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.State;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import static misc.Config.*;
import static misc.Globals.*;

public class VerificationOptimization {
    private static HashMap<? extends FFTState, ? extends StateMapping> lookupTable;
    // States that are reachable when playing with a partial or total strategy
    private static HashMap<FFTState, FFTState> reachableStates; // TODO - consider doing (long, FFTState)
    // Subset of reachable states where strategy does not output a move, and player has the turn
    // This is the set of states that a new rule can potentially influence
    private static HashSet<FFTState> reachableRelevantStates;

    private static FFT fft;
    private static RuleGroup rg;

    public static void main(String[] args) {
        GameSpecifics specs = new GameSpecifics();
        new FFTManager(specs);
        FFTSolver.solveGame(new State());
        PrintStream debugFile = null;
        try {
            debugFile = new PrintStream("debug.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(debugFile);

        generateFFT(PLAYER1);
    }

    public static FFT generateFFT(int perspective_) {
        AUTOGEN_PERSPECTIVE = perspective_;
        fft = new FFT("Synthesis");
        long timeStart = System.currentTimeMillis();
        setup();
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
        fft.USE_STRATEGY = false; // TODO - remove somehow

        System.out.println("Solution size: " + lookupTable.size());

        reachableStates = new HashMap<>();
        reachableRelevantStates = new HashSet<>();

        // Set reachable parents for all states
        initializeSets();
        System.out.println("Number of reachable states: " + reachableStates.size());
        System.out.println("Number of reachable relevant states: " + reachableRelevantStates.size());
        System.out.println("Reachable states:");
        for (FFTState s : reachableStates.keySet())
            System.out.println(s + " , rParents: " + s.getReachableParents());
        System.out.println();

        System.out.println("Reachable relevant states: ");
        for (FFTState s : reachableRelevantStates) {
            System.out.println(s + " , rParents: " + s.getReachableParents());
        }
        System.out.println();
    }

    // Labels each unique state with an occurrence, i.e. how many times said state could potentially be visited with optimal play from initial state
    private static void initializeSets() {
        int team = AUTOGEN_PERSPECTIVE;
        FFTState initialState = FFTManager.initialFFTState;
        LinkedList<FFTState> frontier;
        frontier = new LinkedList<>();
        frontier.add(initialState);
        reachableStates.put(initialState, initialState);
        reachableRelevantStates.add(initialState);
        //if (FFTSolution.optimalMoves(initialState).size() != initialState.getLegalMoves().size())
        //    relevantRules.add(initialState);

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
            //if (state.getLegalMoves().size() != optimalMoves.size()) // not only optimal moves, so relevant
            //    relevantRules.add(state);
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

    private static void start() {
        System.out.println("Making rules");
        makeRules();
        System.out.println("Amount of rules before minimizing: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + fft.getAmountOfPreconditions());
        if (DETAILED_DEBUG) System.out.println("Rules before minimizing");
        if (DETAILED_DEBUG) System.out.println(fft);
        int i = fft.minimize(AUTOGEN_PERSPECTIVE, Config.MINIMIZE_PRECONDITIONS);
        System.out.println("Final amount of rules after " + i + " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i + " minimize iterations: " + fft.getAmountOfPreconditions());
    }

    private static void makeRules() {
        // TODO - try with iterator hasNext() and then skip states with all moves optimal
        while (!reachableRelevantStates.isEmpty()) {
        //while (!relevantRules.isEmpty()) {
            System.out.println("Remaining relevant rules: " + reachableRelevantStates.size() + ". Current amount of rules: " + rg.rules.size());
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
            System.out.print("ORIGINAL LITERALS: ");
            for (Literal l : literals)
                System.out.print(l.name + " ");
            System.out.println();
            System.out.println("ORIGINAL STATE: " + s);
            System.out.println("ORIGINAL MOVE: " + mapping.getMove());
            System.out.println("ORIGINAL SCORE: " + mapping.getScore());
        }

        for (Literal l : literals) {
            if (DETAILED_DEBUG) System.out.println("ATTEMPING TO REMOVE: " + l.name);
            r.removePrecondition(l);

            if (!verifyRule(r)) {
                if (DETAILED_DEBUG) System.out.println("FAILED TO VERIFY RULE!");
                r.addPrecondition(l);
            } else if (DETAILED_DEBUG) {
                System.out.println("REMOVING: " + l.name);
            }
            System.out.println();
        }

        return r;
    }

    private static boolean verifyRule(Rule r) {
        FFTMove move = r.action.getMove(AUTOGEN_PERSPECTIVE);
        HashSet<FFTState> appliedSet = new HashSet<>();
        HashSet<FFTState> suboptimalSet = new HashSet<>(); // contains states with sub-optimal moves
        // true if delete from both, false if only from relevantReachable
        HashMap<FFTState, Boolean> deleteMap = new HashMap<>();
        // Undo the changes to reachable parents if simplification invalid
        HashMap<FFTState, FFTMove> undoMap = new HashMap<>();

        for (FFTState s : reachableRelevantStates) {
            if (r.apply(s) != null) { // this is equivalent to checking that rule applies with legal move
                appliedSet.add(s);
            }
        }

        /*
            System.out.println("Applied set:");
            for (FFTState s : appliedSet)
                System.out.println(s + "reachable parents: " + s.getReachableParents() + " , isReachable: " + s.isReachable());
            System.out.println();
        */

        for (FFTState s : appliedSet) {
            System.out.println("Updating set from state: " + s + " , and move: " + move);
            updateSets(s, move, suboptimalSet, deleteMap, undoMap);
            deleteMap.putIfAbsent(s, false);
        }


        System.out.println("Checking if states in suboptimalSet is reachable");
        for (FFTState s : suboptimalSet) {
            if (s.isReachable()) {
                System.out.println("State: " + s + " , is reachable with suboptimal move");
                System.out.println("Simplification invalid, rule falsified");
                undoReachableParents(undoMap);
                return false;
            }
        }

        System.out.println("Reachable states size: " + reachableStates.size());
        System.out.println("Reachable relevant states size: " + reachableRelevantStates.size());

        if (!fft.verify(AUTOGEN_PERSPECTIVE, false)) {
            System.out.println("ERROR: Old verification failed where new did not");
            System.out.println("Failing point: " + fft.failingPoint);
            System.exit(1);
            return false;
        }
        System.out.println("Simplification succeeded, applying deletions");
        for (Map.Entry<FFTState, Boolean> entry : deleteMap.entrySet()) {
            FFTState key = entry.getKey();
            if (entry.getValue()) { // del from both
                reachableStates.remove(key);
            }
            reachableRelevantStates.remove(key);
        }
        /*
        System.out.println("Printing reachable relevant states:");
        for (FFTState s : reachableRelevantStates)
            System.out.println(s + " , reachable parents: " + s.getReachableParents() + " , isReachable: " + s.isReachable());
        System.out.println();
        */

        return true;
    }

    // Either remove states from applySet in addition to reachableSet, or check whether state is still reachable
    private static void updateSets(FFTState s, FFTMove chosenMove,
                                   HashSet<FFTState> suboptimalSet, HashMap<FFTState, Boolean> deleteMap,
                                   HashMap<FFTState, FFTMove> undoMap) {
        if (chosenMove != null && !s.isReachable()) {// s might've been set unreachable by another state in appliedSet
            System.out.println("state is not reachable");
            return;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(s);
        if (chosenMove != null && !optimalMoves.contains(chosenMove)) { // we choose wrong move
            System.out.println("state chooses suboptimal move, adding to suboptimal set");
            suboptimalSet.add(s);
            return;
        }
        System.out.println("Removing pointers to all children except chosenMove, adding state: " + s + " , and move: " + chosenMove + ", to undoMap");
        undoMap.put(s, chosenMove);
        for (FFTMove m : optimalMoves) { // remove pointer to all children except chosen move
            System.out.println("Next state: " + s.getNextState(m) + ", from move: " + m);
            if (m.equals(chosenMove)) { // chosen move
                System.out.println("We have chosen move");
                continue;
            }
            FFTState existingChild = reachableStates.get(s.getNextState(m));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            // might also be the case that we already removed it once (deleteMap check)
            if (existingChild == null || deleteMap.getOrDefault(existingChild, false)) {
                System.out.println("Existing child null or part of deleteMap");
                continue;
            }
            System.out.println("Removing ptr to child from optimal move: " + existingChild);
            existingChild.removeReachableParent(s);
            System.out.println("remaining pointers: " + existingChild.getReachableParents());
            if (!existingChild.isReachable()) {
                System.out.println("Child no longer reachable, removing from set and checking recursively");
                deleteMap.put(existingChild, true);
                updateSets(existingChild, null, suboptimalSet, deleteMap, undoMap); // FIXME - tail-end?
            }
        }
    }

    private static void undoReachableParents(HashMap<FFTState, FFTMove> undoMap) {
        System.out.println("Undoing deletions of reachable parents");
        for (Map.Entry<FFTState, FFTMove> entry : undoMap.entrySet()) {
            FFTState state = entry.getKey();
            FFTMove chosenMove = entry.getValue(); // may be null (if we want to add link to all children)
            if (chosenMove == null) System.out.println("chosenMove null");
            for (FFTMove move : FFTSolution.optimalMoves(state)) {
                if (move.equals(chosenMove))
                    continue;
                FFTState child = reachableStates.get(state.getNextState(move));
                System.out.println("For state: " + child + " , adding back parent: " + state);
                if (child != null)
                    child.addReachableParent(state);
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

