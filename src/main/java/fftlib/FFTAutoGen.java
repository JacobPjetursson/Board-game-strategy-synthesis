package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;
import fftlib.logic.FFT;
import fftlib.logic.Literal;
import fftlib.logic.Rule;
import fftlib.logic.RuleGroup;
import misc.Config;

import java.util.*;

import static misc.Config.*;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class FFTAutoGen {
    // States that are reachable when playing with a partial or total strategy
    private static HashMap<FFTNode, FFTNode> reachableStates; // TODO - consider doing (long, State)
    // Subset of reachable states where strategy does not output a move, and player has the turn
    // This is the set of states that a new rule can potentially influence
    // Bitcode -> FFTNode
    private static HashMap<Long, FFTNode> reachableRelevantStates;

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
            System.exit(1);
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

        reachableStates = new HashMap<>();
        reachableRelevantStates = new HashMap<>();

        // Set reachable parents for all states
        initializeSets();

        System.out.println("Solution size: " + FFTSolution.size());
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
            FFTNode node = reachableRelevantStates.values().iterator().next();

            Rule r = addRule(node);

            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();
        }
    }

    private static Rule addRule(FFTNode n) {
        LiteralSet minSet = new LiteralSet(n.convert().getAll());
        FFTMove bestMove = FFTSolution.queryState(n).move;

        Rule r = new Rule(minSet, bestMove.convert());
        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL NODE: " + n + " , AND MOVE: " + bestMove);
            System.out.println("ORIGINAL RULE: " + r);
        }
        // make copy to avoid concurrentModificationException
        LiteralSet rulePreconditions = new LiteralSet(r.getPreconditions());
        for (Literal l : rulePreconditions) {
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
        HashMap<FFTNode, FFTMove> appliedMap = new HashMap<>();
        HashSet<FFTNode> suboptimalSet = new HashSet<>(); // contains states with sub-optimal moves
        HashMap<FFTNode, Boolean> deleteMap = new HashMap<>();
        HashMap<FFTNode, FFTMove> undoMap = new HashMap<>();

        long coveredStates = r.getNumberOfCoveredStates();
        if (DETAILED_DEBUG) {
            System.out.println("Upper bound for no. of covered states: " + coveredStates);
            System.out.println("Size of reachable relevant states: " + reachableRelevantStates.size());
        }
        if (USE_APPLYSET_OPT && coveredStates < reachableRelevantStates.size()) {
            HashSet<Long> codes = r.getCoveredStateBitCodes();
            if (DETAILED_DEBUG) System.out.println("Exact no. of covered states: " + codes.size());
            for (long code : codes) {
                FFTNode n = reachableRelevantStates.get(code);
                if (n == null) continue;
                FFTMove m = r.apply(n);
                if (m != null)
                    appliedMap.put(n, m);
            }
        }
        else {
            for (FFTNode n : reachableRelevantStates.values()) {
                FFTMove m = r.apply(n);
                if (m != null) { // this is equivalent to checking that rule applies with legal move
                    appliedMap.put(n, m);
                }
            }
        }

        if (DETAILED_DEBUG)
            System.out.println("appliedMap size: " + appliedMap.size());

        for (Map.Entry<FFTNode, FFTMove> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            FFTMove m = entry.getValue();
            //System.out.println("Updating set from node: " + n + ", and move: " + m);
            updateSets(n, m, suboptimalSet, deleteMap, undoMap);
        }

        //System.out.println("deleteMap size: " + deleteMap.size());
        //System.out.println("undoMap size: " + undoMap.size());

        for (FFTNode n : suboptimalSet) {
            if (n.isReachable()) {
                //System.out.println("suboptimal node is reachable: " + n);
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
            // When simplifying rules, they might apply to previous states with a different symmetry
            // (and thus a different action), which means we need to undo the changes made
            undoReachableParents(undoMap);
        }
        return true;
    }

    // Either remove states from applySet in addition to reachableSet, or check whether state is still reachable
    private static void updateSets(FFTNode n, FFTMove chosenMove,
                                   HashSet<FFTNode> suboptimalSet, HashMap<FFTNode, Boolean> deleteMap,
                                   HashMap<FFTNode, FFTMove> undoMap) {
        deleteMap.putIfAbsent(n, false);
        // s might've been set unreachable by another state in appliedSet
        if (!n.isReachable()) {
            //System.out.println("state is not reachable");
            return;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
        if (optimalMoves.isEmpty()) // terminal state
            return;
        if (!optimalMoves.contains(chosenMove)) { // we choose wrong move
            //System.out.println("state chooses suboptimal move, adding to suboptimal set");
            suboptimalSet.add(n);
            return;
        }
        //System.out.println("Removing pointers to all children except chosenMove, adding state: "
        //        + n + " , and move: " + chosenMove + ", to undoMap");

        undoMap.put(n, chosenMove);
        for (FFTMove m : optimalMoves) { // remove pointer to all children except chosen move
            //System.out.println("Next node: " + n.getNextNode(m) + ", from move: " + m);
            if (m.equals(chosenMove)) { // chosen move
                continue;
            }
            FFTNode existingChild = reachableStates.get(n.getNextNode(m));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            // Might also be the case that we already removed it once (deleteMap check)
            if (existingChild == null || deleteMap.getOrDefault(existingChild, false)) {
                //System.out.println("Existing child null or part of deleteMap");
                continue;
            }
            //System.out.println("Removing ptr to child" + existingChild + " , from node: " + n);
            existingChild.removeReachableParent(n);
            //System.out.println("remaining pointers: " + existingChild.getReachableParents());
            if (!existingChild.isReachable()) {
                //System.out.println("Child no longer reachable, removing from set and checking recursively");
                removePointers(existingChild, deleteMap, undoMap);
            }
        }
    }

    private static void removePointers(FFTNode parent, HashMap<FFTNode, Boolean> deleteMap,
                                       HashMap<FFTNode, FFTMove> undoMap) {
        deleteMap.put(parent, true);
        undoMap.put(parent, null);
        for (FFTNode child : parent.getChildren()) {
            FFTNode existingChild = reachableStates.get(child);
            if (existingChild == null || deleteMap.getOrDefault(existingChild, false)) {
                //System.out.println("Existing child null or part of deleteMap");
                continue;
            }

            //System.out.println("Removing ptr to child" + existingChild + " , from node: " + parent);
            existingChild.removeReachableParent(parent);
            //System.out.println("remaining pointers: " + existingChild.getReachableParents());
            if (!existingChild.isReachable()) { // TODO - use something else instead of undoMap?
                //System.out.println("Child no longer reachable, removing from set and checking recursively");
                removePointers(existingChild, deleteMap, undoMap);
            }
        }
        // FIXME - tail-end?
    }

    private static void deleteUnreachableStates(HashMap<FFTNode, Boolean> deleteMap) {
        //System.out.println("Deleting unreachable states");
        for (Map.Entry<FFTNode, Boolean> entry : deleteMap.entrySet()) {
            FFTNode key = entry.getKey();
            if (entry.getValue()) { // del from both
                reachableStates.remove(key);
            }
            reachableRelevantStates.remove(key.convert().getBitString());
        }
    }

    private static void undoReachableParents(HashMap<FFTNode, FFTMove> undoMap) {
        //System.out.println("Undoing deletions of reachable parents");
        for (Map.Entry<FFTNode, FFTMove> entry : undoMap.entrySet()) {
            FFTNode state = entry.getKey();
            FFTMove chosenMove = entry.getValue(); // might be null (if we want to add link to all children)
            for (FFTMove m : state.getLegalMoves()) {
                if (m.equals(chosenMove))
                    continue;
                FFTNode child = reachableStates.get(state.getNextNode(m));
                // child can be null if choiceMove is not null
                if (child != null)
                    child.addReachableParent(state);
            }
        }
    }

    private static void initializeSets() {
        int team = AUTOGEN_TEAM;
        FFTNode initialNode = FFTManager.initialFFTNode;
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        LinkedList<FFTNode> frontier;
        frontier = new LinkedList<>();
        frontier.add(initialNode);
        reachableStates.clear();
        reachableRelevantStates.clear();
        reachableStates.put(initialNode, initialNode);
        if (team == PLAYER1)
            reachableRelevantStates.put(initialNode.convert().getBitString(), initialNode);

        while (!frontier.isEmpty()) {
            FFTNode node = frontier.pop();
            // game over
            if (node.isTerminal()) {
                if (node.getWinner() == opponent) {
                    // Should not hit this given initial check
                    System.err.println("No chance of winning vs. perfect player");
                    System.exit(1);
                }
                continue;
            }

            // Not our turn
            if (team != node.getTurn()) {
                for (FFTNode child : node.getChildren()) {
                    if (!reachableStates.containsKey(child)) {
                        reachableStates.put(child, child);
                        frontier.add(child);
                    }
                    // add reachableParent
                    reachableStates.get(child).addReachableParent(node);
                }
                continue;
            }
            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
            reachableRelevantStates.put(node.convert().getBitString(), node);

            // Our turn, add all states from optimal moves
            for (FFTMove m : optimalMoves) {
                FFTNode child = node.getNextNode(m);
                if (!reachableStates.containsKey(child)) {
                    reachableStates.put(child, child);
                    frontier.add(child);
                }
                reachableStates.get(child).addReachableParent(node);

            }
        }
    }
}

