package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;
import fftlib.game.NodeMapping;
import fftlib.logic.FFT;
import fftlib.logic.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static misc.Config.*;
import static misc.Globals.*;

public class FFTAutoGen {
    // States that are reachable when playing with a partial or total strategy
    private static HashMap<FFTNode, FFTNode> reachableStates;
    // Subset of reachable states where strategy does not output a move, and player has the turn
    // This is the set of states that a new rule can potentially influence
    private static TreeMap<Long, FFTNode> applicableStatesOpt;
    private static Map<FFTNode, FFTNode> applicableStates;

    private static FFT fft;
    private static RuleGroup rg;

    // used for checking whether lifting a rule applies to more states
    private static int groundedAppliedMapSize;
    private static int liftedAppliedMapSize;

    public static FFT generateFFT(int team_) {
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
                generate(false);
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
            generate(false);
        }

        return fft;
    }

    public static FFT generateFFT(int team_, FFT existingFFT) {
        fft = existingFFT;
        AUTOGEN_TEAM = team_;
        if (!fft.isValid(AUTOGEN_TEAM)) {
            System.err.println("Invalid fft");
            System.exit(1);
        }
        generate(true);
        return fft;
    }

    private static void generate(boolean existingFFT) {
        long timeStart = System.currentTimeMillis();

        setup(existingFFT);
        System.out.println("Making rules");
        makeRules();
        System.out.println("Amount of rules before minimizing: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + fft.getAmountOfPreconditions());
        if (DETAILED_DEBUG) System.out.println("Rules before minimizing");
        if (DETAILED_DEBUG) System.out.println(fft);

        int i;
        if (USE_BITSTRING_SORT_OPT)
            i = minimizeOpt();
        else
            i = fft.minimize(AUTOGEN_TEAM, MINIMIZE_PRECONDITIONS);
        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;

        System.out.println("Final amount of rules after " + i +
                " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i +
                " minimize iterations: " + fft.getAmountOfPreconditions());
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
        System.out.println("Final rules: \n" + fft);
    }

    private static void setup(boolean existingFFT) {
        if (!existingFFT) {
            fft = new FFT("Synthesis");
            rg = new RuleGroup("Synthesis");
            fft.addRuleGroup(rg);
        }

        // Set reachable parents for all states
        findReachableStates();

        System.out.println("Solution size: " + FFTSolution.size());
        System.out.println("Number of reachable states: " + reachableStates.size());
        int size = USE_BITSTRING_SORT_OPT ? applicableStatesOpt.size() : applicableStates.size();
        System.out.println("Number of applicable states: " + size);
    }

    private static void makeRules() {
        long size = USE_BITSTRING_SORT_OPT ? applicableStatesOpt.size() : applicableStates.size();
        while (size != 0) {
            System.out.println("Remaining applicable states: " + size +
                    ". Current amount of rules: " + rg.rules.size());
            FFTNode node = USE_BITSTRING_SORT_OPT ? applicableStatesOpt.firstEntry().getValue() :
                    applicableStates.values().iterator().next();
            Rule r = addRule(node);
            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();
            size = USE_BITSTRING_SORT_OPT ? applicableStatesOpt.size() : applicableStates.size();
        }
    }

    private static Rule addRule(FFTNode n) {
        LiteralSet minSet = new LiteralSet(n.convert().getAll());
        FFTMove bestMove = FFTSolution.queryNode(n).move;

        Rule r = new Rule(minSet, bestMove.convert()); // rule from state-move pair

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL NODE: " + n + " , AND MOVE: " + bestMove);
            System.out.println("NODE SCORE: " + FFTSolution.queryNode(n).score);
            System.out.println("ORIGINAL RULE: " + r);
        }
        if (!USE_BITSTRING_SORT_OPT && USE_LIFTING && LIFT_BEFORE_SIMPLIFY)
            r = liftRule(r);

        simplifyRule(r);

        if (!USE_BITSTRING_SORT_OPT && USE_LIFTING && !LIFT_BEFORE_SIMPLIFY) {
            Rule pr = liftRule(r);
            // simplify again?
            if (pr != r)
                simplifyRule(pr); // TODO - benchmark
            r = pr;
        }
        rg.rules.add(r);
        verifyRule(r, true); // safe run where we know we have the final rule

        /*
        if (!fft.verify(AUTOGEN_TEAM, false)) {
            System.out.println("ERROR: Old verification failed where new did not");
            System.out.println("Failing point: " + fft.failingPoint);
            System.exit(1);
        }

         */

        return r;
    }

    private static void simplifyRule(Rule r) {
        if (DETAILED_DEBUG) System.out.println("SIMPLIFYING RULE: " + r);
        // make copy to avoid concurrentModificationException
        int prevSize;
        int i = 1;
        do {
            // sort the literals so we start with literal with lowest ID
            TreeMap<Integer, Literal> literalSet = new TreeMap<>();
            for (Literal l : r.getPreconditions()) {
                literalSet.put(l.id, l);
            }
            //LiteralSet rulePreconditions = new LiteralSet(r.getPreconditions());
            if (DETAILED_DEBUG && SIMPLIFY_ITERATIVELY)
                System.out.println("SIMPLIFICATION ITERATION: " + i++);
            prevSize = literalSet.size();
            for (Literal l : literalSet.values()) {
                if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO REMOVE: " + l.getName());
                r.removePrecondition(l);

                if (!verifyRule(r, false)) {
                    if (DETAILED_DEBUG) System.out.println("FAILED TO REMOVE: " + l.getName());
                    r.addPrecondition(l);
                } else {
                    if (DETAILED_DEBUG) System.out.println("REMOVING PRECONDITION: " + l.getName());
                }
                if (DETAILED_DEBUG) System.out.println("RULE IS NOW: " + r);
            }
        } while (SIMPLIFY_ITERATIVELY && prevSize != r.getPreconditions().size() &&
                r.getPreconditions().size() != 0);
    }

    private static boolean verifyRule(Rule r, boolean safe) {
        if (safe && DETAILED_DEBUG)
            System.out.println("DOING SAFE RUN");
        ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap = new ConcurrentHashMap<>();

        //long coveredStates = r.getNumberOfCoveredStates();
        //if (USE_APPLYSET_OPT && coveredStates < reachableRelevantStates.size()) {
        //    fillFromCoveredStates(r, appliedMap);
        //}
        if (USE_BITSTRING_SORT_OPT)
            fillByIterating_Opt(r, appliedMap);
        else
            fillByIterating(r, appliedMap);

        if (DETAILED_DEBUG)
            System.out.println("appliedMap size: " + appliedMap.size());

        for (Map.Entry<FFTNode, HashSet<FFTMove>> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            HashSet<FFTMove> moves = entry.getValue();
            if (!updateSets(n, moves, appliedMap, safe)) {
                return false;
            }
        }

        if (r instanceof PredRule)
            liftedAppliedMapSize = appliedMap.size();
        else
            groundedAppliedMapSize = appliedMap.size();

        return true;
    }

    private static Rule liftRule(Rule r) {
        if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO LIFT RULE");
        // attempt to lift propositional variables one at a time, sorted by the most occurring variable 1st
        for (int prop : r.getSortedProps()) {
            PredRule pr = r.liftAll(prop);
            if (DETAILED_DEBUG) System.out.println("VERIFYING LIFTED RULE: " + pr);
            // we do not want to lift if the lifted rule does not apply to more states
            if (verifyRule(pr, false) && groundedAppliedMapSize != liftedAppliedMapSize) {
                if (DETAILED_DEBUG) System.out.println("RULE SUCCESSFULLY LIFTED TO: " + pr);
                return pr;
            }
        }
        if (DETAILED_DEBUG) System.out.println("FAILED TO LIFT RULE");
        return r;
    }

    private static void fillFromCoveredStates(Rule r, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        HashSet<LiteralSet> states = r.getCoveredStates();
        if (DETAILED_DEBUG) System.out.println("Exact no. of covered states: " + states.size());
        if (!SINGLE_THREAD) {
            states.parallelStream().forEach(set -> {
                FFTNode n = applicableStates.get(set.getBitString());
                insert(n, r, appliedMap);
            });
        }
        for (LiteralSet state : states) {
            FFTNode n = applicableStates.get(state.getBitString());
            insert(n, r, appliedMap);
        }
    }

    private static void fillByIterating(Rule r, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        if (!SINGLE_THREAD) {
            applicableStates.values().parallelStream().forEach(node ->
                    insert(node, r, appliedMap));
        } else {
            for (FFTNode n : applicableStates.values()) {
                insert(n, r, appliedMap);
            }
        }
    }

    private static void fillByIterating_Opt(Rule r, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        long code = r.getAllPreconditions().getBitString();
        if (!SINGLE_THREAD) {
            applicableStatesOpt.values().parallelStream().forEach(node ->
                    insert(node, r, appliedMap));
        } else {
            for (Map.Entry<Long, FFTNode> entry : applicableStatesOpt.entrySet()) {
                if (entry.getKey() < code)
                    break;
                insert(entry.getValue(), r, appliedMap);
            }
        }
    }

    private static void insert(FFTNode n, Rule r, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        if (n == null)
            return;
        HashSet<FFTMove> moves = r.apply(n);
        if (!moves.isEmpty())
            appliedMap.put(n, moves);
    }

    // Either remove states from applySet in addition to reachableSet, or check whether state is still reachable
    private static boolean updateSets(FFTNode n, HashSet<FFTMove> chosenMoves, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap,
                                      boolean safe) {
        if (safe) {
            if (USE_BITSTRING_SORT_OPT) applicableStatesOpt.remove(n.convert().getBitString());
            else applicableStates.remove(n);
        }
        // s might've been set unreachable by another state in appliedSet
        if (!n.isReachable()) {
            return true;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
        if (optimalMoves.isEmpty()) // terminal state
            return true;
        for (FFTMove chosenMove : chosenMoves) {
            if (!optimalMoves.contains(chosenMove)) {
                if (isReachable(n, appliedMap))
                    return false;
                else {
                    chosenMoves.clear();
                    break;
                }
            }
        }
        if (!safe) // only update sets in a safe run
            return true;

        for (FFTMove m : optimalMoves) { // remove pointer to all children except chosen moves
            if (chosenMoves.contains(m)) { // chosen move
                continue;
            }
            FFTNode existingChild = reachableStates.get(n.getNextNode(m));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            if (existingChild == null)
                continue;
            existingChild.removeReachableParent(n);
            if (!existingChild.isReachable())
                removeFromSets(existingChild);
        }
        return true;
    }

    private static void removeFromSets(FFTNode parent) {
        reachableStates.remove(parent);
        if (USE_BITSTRING_SORT_OPT) applicableStatesOpt.remove(parent.convert().getBitString());
        else applicableStates.remove(parent);
        for (FFTNode child : parent.getChildren()) {
            FFTNode existingChild = reachableStates.get(child);
            if (existingChild == null)
                continue;
            existingChild.removeReachableParent(parent);
            if (!existingChild.isReachable())
                removeFromSets(existingChild);
        }
    }

    // walk upwards through reachable parents until either initial state is found
    private static boolean isReachable(FFTNode node, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        LinkedList<FFTNode> frontier = new LinkedList<>();
        HashSet<FFTNode> closedSet = new HashSet<>();
        frontier.add(node);
        closedSet.add(node);

        while (!frontier.isEmpty()) {
            FFTNode n = frontier.pop();
            if (n.equals(FFTNode.getInitialNode())) {
                return true;
            }
            for (FFTNode parent : n.getReachableParents()) {
                FFTNode existingParent = reachableStates.get(parent);
                if (existingParent == null || closedSet.contains(parent))
                    continue;
                HashSet<FFTMove> chosenMoves = appliedMap.get(parent);
                // not in appliedMap, add parent
                if (chosenMoves == null) {
                    closedSet.add(parent);
                    frontier.add(existingParent);
                } else{
                    // chooses correct move
                    for (FFTMove chosenMove : chosenMoves) {
                        if (parent.getNextNode(chosenMove).equals(n)) {
                            closedSet.add(parent);
                            frontier.add(existingParent);
                            break;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static void findReachableStates() {
        int team = AUTOGEN_TEAM;
        FFTNode initialNode = FFTManager.initialFFTNode;
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        LinkedList<FFTNode> frontier;
        frontier = new LinkedList<>();
        frontier.add(initialNode);
        reachableStates = new HashMap<>();
        if (USE_BITSTRING_SORT_OPT)
            applicableStatesOpt = new TreeMap<>(Comparator.reverseOrder());
        else {
            if (USE_RULE_ORDERING)
                applicableStates = new TreeMap<>(new NodeComparator());
            else
                applicableStates = new HashMap<>();

        }
        reachableStates.put(initialNode, initialNode);
        if (team == PLAYER1) {
            if (USE_BITSTRING_SORT_OPT) applicableStatesOpt.put(
                    initialNode.convert().getBitString(), initialNode);
            else
            applicableStates.put(initialNode, initialNode);
        }

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
                for (FFTNode child : node.getChildren())
                    addNode(frontier, node, child);
                continue;
            }
            HashSet<FFTMove> chosenMoves = fft.apply(node);
            if (!chosenMoves.isEmpty()) {
                for (FFTMove m : chosenMoves)
                    addNode(frontier, node, node.getNextNode(m));
            } else {
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                if (USE_BITSTRING_SORT_OPT) applicableStatesOpt.put(node.convert().getBitString(), node);
                else applicableStates.put(node, node);
                // Our turn, add all states from optimal moves, or those that an existing FFT applies to
                for (FFTMove m : optimalMoves)
                    addNode(frontier, node, node.getNextNode(m));
            }
        }
    }

    private static void addNode(LinkedList<FFTNode> frontier, FFTNode parent, FFTNode child) {
        if (!reachableStates.containsKey(child)) {
            reachableStates.put(child, child);
            frontier.add(child);
        }
        reachableStates.get(child).addReachableParent(parent);
    }

    // Allows us to sort the nodes based on custom values, such as which node is closed to a terminal node
    public static class NodeComparator implements Comparator<FFTNode> {
        @Override
        public int compare(FFTNode n1, FFTNode n2) {
            if (RULE_ORDERING == RULE_ORDERING_TERMINAL_LAST ||
                    RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST) {
                NodeMapping nm1 = FFTSolution.queryNode(n1);
                NodeMapping nm2 = FFTSolution.queryNode(n2);
                if (nm1 == null) {
                    if (nm2 == null)
                        return compareBitString(n1, n2);
                    return -1;
                } else if (nm2 == null) {
                    return 1;
                }
                int n1_score = Math.abs(nm1.getScore());
                int n2_score = Math.abs(nm2.getScore());

                if (RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST) {
                    if (n1_score > n2_score)
                        return -1;
                    else if (n2_score > n1_score)
                        return 1;
                    else {
                        return compareBitString(n1, n2);
                    }
                }
            }
            int n1_precons_amount = n1.convert().size();
            int n2_precons_amount = n2.convert().size();
            if (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST) {
                if (n1_precons_amount > n2_precons_amount)
                    return n1.hashCode() - n2.hashCode();
                return n2.hashCode() - n1.hashCode();
            }
            return 0;
        }

        private int compareBitString(FFTNode n1, FFTNode n2) {
            long bitStringDiff = n1.convert().getBitString() - n2.convert().getBitString();
            if (bitStringDiff > 0)
                return 1;
            else if (bitStringDiff < 0)
                return -1;
            return 0;
        }
    }

    // Rule 'r' has been removed, so we verify if FFT is still optimal without 'r'
    private static boolean verifyRuleRemoval(Rule r) {
        ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap = new ConcurrentHashMap<>();
        fillByIterating_Min(r, appliedMap);

        if (DETAILED_DEBUG)
            System.out.println("appliedMap size: " + appliedMap.size());

        for (Map.Entry<FFTNode, HashSet<FFTMove>> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            HashSet<FFTMove> moves = entry.getValue();
            if (!updateSets_Min(n, moves, appliedMap)) {
                return false;
            }
        }

        return true;
    }

    // iterates through reachable states (since we're looking for the states we already apply to)
    private static void fillByIterating_Min(Rule r, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        if (!SINGLE_THREAD) {
            reachableStates.values().parallelStream().forEach(node ->
                    insert_Min(node, r, appliedMap));
        } else {
            for (FFTNode n : reachableStates.values()) {
                insert_Min(n, r, appliedMap);
            }
        }
    }

    private static void insert_Min(FFTNode n, Rule r, ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        if (n == null)
            return;
        HashSet<FFTMove> oldMoves = r.apply(n);
        if (!oldMoves.isEmpty()) {
            // find new moves todo
            HashSet<FFTMove> newMoves = new HashSet<>();
            appliedMap.put(n, newMoves);
        }
    }

    // TODO
    private static boolean updateSets_Min(FFTNode n, HashSet<FFTMove> chosenMoves,
                                          ConcurrentHashMap<FFTNode, HashSet<FFTMove>> appliedMap) {
        // chosenMoves empty, add back to applicableSet
        if (chosenMoves.isEmpty()) {
            applicableStatesOpt.put(n.convert().getBitString(), n);
        }
        // s might've been set unreachable by another state in appliedSet
        if (!n.isReachable()) {
            return true;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
        if (optimalMoves.isEmpty()) // terminal state
            return true;
        for (FFTMove chosenMove : chosenMoves) {
            if (!optimalMoves.contains(chosenMove)) {
                if (isReachable(n, appliedMap))
                    return false;
                else {
                    chosenMoves.clear();
                    break;
                }
            }
        }

        // add back pointers to all children recursively
        if (chosenMoves.isEmpty()) {

        } else { // same procedure as previously

        }

        for (FFTMove m : optimalMoves) { // remove pointer to all children except chosen moves
            if (chosenMoves.contains(m)) { // chosen move, add pointer
                continue;
            }
            FFTNode existingChild = reachableStates.get(n.getNextNode(m));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            if (existingChild == null)
                continue;
            existingChild.removeReachableParent(n);
            if (!existingChild.isReachable())
                removeFromSets(existingChild);
        }
        return true;
    }

    // todo
    private static void addToSets(FFTNode parent) {
        reachableStates.remove(parent);
        if (USE_BITSTRING_SORT_OPT) applicableStatesOpt.remove(parent.convert().getBitString());
        else applicableStates.remove(parent);
        for (FFTNode child : parent.getChildren()) {
            FFTNode existingChild = reachableStates.get(child);
            if (existingChild == null)
                continue;
            existingChild.removeReachableParent(parent);
            if (!existingChild.isReachable())
                removeFromSets(existingChild);
        }
    }

    // assumes that the FFT is strongly optimal
    private static int minimizeOpt() {
        int ruleSize, precSize;
        int i = 0;
        do {
            ruleSize = fft.getAmountOfRules();
            precSize = fft.getAmountOfPreconditions();
            if (DETAILED_DEBUG)
                System.out.println("Minimizing, iteration no. " + i++);
            minimizeRules();
            if (!MINIMIZE_RULE_BY_RULE && MINIMIZE_PRECONDITIONS) {
                if (DETAILED_DEBUG) System.out.println("Minimizing preconditions");
                minimizePreconditions();
            }
        } while (ruleSize != fft.getAmountOfRules() || precSize != fft.getAmountOfPreconditions());

        return i;
    }

    private static void minimizeRules() {
        for (RuleGroup rg : fft.ruleGroups) {
            if (rg.locked) // don't minimize if rulegroup is locked
                continue;
            ListIterator<Rule> itr = rg.rules.listIterator();
            while(itr.hasNext()) {
                Rule r = itr.next();
                itr.remove();
                // todo - this bad boy is tough work
                if (!verifyRuleRemoval(r)) {
                    itr.add(r);
                    if (MINIMIZE_RULE_BY_RULE)
                        minimizePreconditions();
                }
            }
        }
    }

    private static void minimizePreconditions() {
        for (RuleGroup rg : fft.ruleGroups) {
            if (rg.locked) continue; // don't minimize if rg is locked
            for(Rule r : rg.rules) {
                // we can simplify intermediate rules optimally if symmetry is disabled
                simplifyRule(r);
            }
        }
    }
}

