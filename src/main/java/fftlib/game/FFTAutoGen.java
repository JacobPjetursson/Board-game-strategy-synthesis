package fftlib.game;

import fftlib.*;
import misc.Config;

import java.util.*;

import static misc.Config.*;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class FFTAutoGen {
    private static HashMap<State, ArrayList<Action>> solution;
    // States that are reachable when playing with a partial or total strategy
    private static HashMap<State, State> reachableStates; // TODO - consider doing (long, State)
    // Subset of reachable states where strategy does not output a move, and player has the turn
    // This is the set of states that a new rule can potentially influence
    // Bitcode -> State
    private static HashMap<Long, State> reachableRelevantStates;

    private static FFT fft;
    private static RuleGroup rg;

    public static FFT generateFFT(int team_) {
        // temp code
/*
        Scanner block = new Scanner(System.in);
        System.out.println("Press any button to continue");
        block.nextLine();
*/

        fft = new FFT("Synthesis");
        AUTOGEN_TEAM = team_;
        if (!fft.isValid(AUTOGEN_TEAM)) {
            System.err.println("Invalid fft");
            return null;
        }

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
        solution = FFTSolution.getSolution();
        fft.USE_STRATEGY = false; // TODO - remove somehow

        reachableStates = new HashMap<>();
        reachableRelevantStates = new HashMap<>();

        // Set reachable parents for all states
        initializeSets();

        System.out.println("Solution size: " + solution.size());
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

        System.out.println("Final amount of rules after " + i +
                " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i +
                " minimize iterations: " + fft.getAmountOfPreconditions());
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
        System.out.println("Final rules: \n" + fft);

    }

    private static void makeRules() {
        // TODO - try with iterator hasNext() and then skip states with all moves optimal
        while (!reachableRelevantStates.isEmpty()) {
            System.out.println("Remaining relevant states: " + reachableRelevantStates.size() +
                    ". Current amount of rules: " + rg.rules.size());
            State state = reachableRelevantStates.values().iterator().next();

            Rule r = addRule(state);

            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();
        }
    }

    private static Rule addRule(State s) {
        LiteralSet minSet = new LiteralSet();
        LiteralSet literals = s.getAllLiterals();
        ArrayList<Action> optimalActions = solution.get(s);
        Action rAction = optimalActions.get(0);

        for (Literal l : literals)
            minSet.add(new Literal(l));

        Rule r = new Rule(minSet, rAction);
        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL STATE: " + s + " , AND ACTION: " + rAction);
            System.out.println("ORIGINAL RULE: " + r);
        }

        for (Literal l : literals) {
            if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO REMOVE: " + l.getName());
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
        HashMap<State, Action> appliedMap = new HashMap<>();
        HashSet<State> suboptimalSet = new HashSet<>(); // contains states with sub-optimal moves
        HashMap<State, Boolean> deleteMap = new HashMap<>();
        HashMap<State, Action> undoMap = new HashMap<>();

        long coveredStates = r.getNumberOfCoveredStates();
        if (DETAILED_DEBUG) {
            System.out.println("Upper bound for no. of covered states: " + coveredStates);
            System.out.println("Size of reachable relevant states: " + reachableRelevantStates.size());
        }
        if (USE_APPLYSET_OPT && coveredStates < reachableRelevantStates.size()) {
            //System.out.println("Using covered states");
            HashSet<Long> codes = r.getCoveredStateBitCodes();
            if (DETAILED_DEBUG) System.out.println("Exact no. of covered states: " + codes.size());
            for (long code : codes) {
                State s = reachableRelevantStates.get(code);
                if (s == null) continue;
                Action a = r.apply(s);
                if (a != null)
                    appliedMap.put(s, a);
            }
        }
        else {
            //System.out.println("Using reachable relevant states");
            for (State s : reachableRelevantStates.values()) {
                Action a = r.apply(s);
                if (a != null) { // this is equivalent to checking that rule applies with legal move
                    appliedMap.put(s, a);
                }
            }
        }

        if (DETAILED_DEBUG)
            System.out.println("appliedMap size: " + appliedMap.size());

        for (Map.Entry<State, Action> entry : appliedMap.entrySet()) {
            State s = entry.getKey();
            Action a = entry.getValue();
            //System.out.println("Updating set from state: " + s + ", and action: " + a);
            updateSets(s, a, suboptimalSet, deleteMap, undoMap);
            deleteMap.putIfAbsent(s, false);
        }

        //System.out.println("deleteMap size: " + deleteMap.size());
        //System.out.println("undoMap size: " + undoMap.size());

        for (State s : suboptimalSet) {
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
    private static void updateSets(State s, Action chosenAction,
                                   HashSet<State> suboptimalSet, HashMap<State, Boolean> deleteMap,
                                   HashMap<State, Action> undoMap) {
        // s might've been set unreachable by another state in appliedSet
        if (chosenAction != null && !s.isReachable()) {
            //System.out.println("state is not reachable");
            return;
        }
        ArrayList<Action> optimalActions = solution.get(s);
        if (optimalActions == null) // terminal state
            return;
        if (chosenAction != null && !optimalActions.contains(chosenAction)) { // we choose wrong move
            //System.out.println("state chooses suboptimal move, adding to suboptimal set");
            suboptimalSet.add(s);
            return;
        }
        //System.out.println("Removing pointers to all children except chosenMove, adding state: "
        //        + s + " , and action: " + chosenAction + ", to undoMap");

        undoMap.put(s, chosenAction);
        for (Action a : optimalActions) { // remove pointer to all children except chosen move
            //System.out.println("Next state: " + s.getNextState(a) + ", from action: " + a);
            if (a.equals(chosenAction)) { // chosen move
                continue;
            }
            State existingChild = reachableStates.get(s.getNextState(a));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            // might also be the case that we already removed it once (deleteMap check)
            if (existingChild == null || deleteMap.getOrDefault(existingChild, false)) {
                //System.out.println("Existing child null or part of deleteMap");
                continue;
            }
            //System.out.println("Removing ptr to child from optimal action: " + existingChild);
            existingChild.removeReachableParent(s);
            //System.out.println("remaining pointers: " + existingChild.getReachableParents());
            if (!existingChild.isReachable()) { // TODO - use something else instead of undoMap?
                //System.out.println("Child no longer reachable, removing from set and checking recursively");
                deleteMap.put(existingChild, true);
                updateSets(existingChild, null, suboptimalSet, deleteMap, undoMap); // FIXME - tail-end?
            }
        }
    }

    private static void deleteUnreachableStates(HashMap<State, Boolean> deleteMap) {
        //System.out.println("Deleting unreachable states");
        for (Map.Entry<State, Boolean> entry : deleteMap.entrySet()) {
            State key = entry.getKey();
            if (entry.getValue()) { // del from both
                reachableStates.remove(key);
            }
            reachableRelevantStates.remove(key.getBitString());
        }
    }

    private static void undoReachableParents(HashMap<State, Action> undoMap) {
        //System.out.println("Undoing deletions of reachable parents");
        for (Map.Entry<State, Action> entry : undoMap.entrySet()) {
            State state = entry.getKey();
            Action chosenAction = entry.getValue(); // might be null (if we want to add link to all children)
            for (Action action : solution.get(state)) {
                if (action.equals(chosenAction))
                    continue;
                State child = reachableStates.get(state.getNextState(action));
                //System.out.println("For state: " + child + " , adding back parent: " + state);
                // child can be null if choiceMove is not null
                if (child != null) {
                    child.addReachableParent(state);
                }
            }
        }
    }

    // Interface between domain-specific and logic
    // TODO - fix it somehow (by using solution before converting it?)
    private static void initializeSets() {
        int team = AUTOGEN_TEAM;
        FFTNode initialNode = FFTManager.initialFFTNode;
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        LinkedList<FFTNode> frontier;
        frontier = new LinkedList<>();
        frontier.add(initialNode);
        reachableStates.clear();
        reachableRelevantStates.clear();
        reachableStates.put(initialNode.getState(), initialNode.getState());
        reachableRelevantStates.put(initialNode.getState().getBitString(), initialNode.getState());

        while (!frontier.isEmpty()) {
            FFTNode node = frontier.pop();
            // game over
            if (FFTManager.logic.gameOver(node)) {
                if (FFTManager.logic.getWinner(node) == opponent) {
                    // Should not hit this given initial check
                    System.err.println("No chance of winning vs. perfect player");
                    System.exit(1);
                }
                continue;
            }

            // Not our turn
            if (team != node.getTurn()) {
                for (FFTNode child : node.getChildren()) {
                    State stateChild = child.getState();
                    State existingChild = reachableStates.get(stateChild);
                    if (existingChild == null) {
                        reachableStates.put(stateChild, stateChild);
                        frontier.add(child);
                    }
                    // add reachableParent
                    reachableStates.get(stateChild).addReachableParent(node.getState());

                }
                continue;
            }
            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
            State state = node.getState();
            reachableRelevantStates.put(state.getBitString(), state);

            // Our turn, add all states from optimal moves
            for (FFTMove m : optimalMoves) {
                FFTNode child = node.getNextNode(m);
                State stateChild = child.getState();
                State existingChild = reachableStates.get(stateChild);
                if (existingChild == null) {
                    reachableStates.put(stateChild, stateChild);
                    frontier.add(child);
                }
                reachableStates.get(stateChild).addReachableParent(state);

            }
        }
    }
}

