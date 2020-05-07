package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;
import fftlib.game.NodeMapping;
import fftlib.logic.FFT;
import fftlib.logic.Literal;
import fftlib.logic.Rule;
import fftlib.logic.RuleGroup;
import misc.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static misc.Config.*;
import static misc.Globals.*;

public class FFTAutoGen {
    // States that are reachable when playing with a partial or total strategy
    private static HashMap<FFTNode, FFTNode> reachableStates;
    // Subset of reachable states where strategy does not output a move, and player has the turn
    // This is the set of states that a new rule can potentially influence
    // Bitcode -> FFTNode
    private static Map<FFTNode, FFTNode> reachableRelevantStates;

    private static FFT fft;
    private static RuleGroup rg;

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

        int i = fft.minimize(AUTOGEN_TEAM, Config.MINIMIZE_PRECONDITIONS);
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
        System.out.println("Number of reachable relevant states: " + reachableRelevantStates.size());
    }

    private static void makeRules() {
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
        FFTMove bestMove = FFTSolution.queryNode(n).move;

        Rule r = new Rule(minSet, bestMove.convert());
        rg.rules.add(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL NODE: " + n + " , AND MOVE: " + bestMove);
            System.out.println("NODE SCORE: " + FFTSolution.queryNode(n).score);
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
        ConcurrentHashMap<FFTNode, FFTMove> appliedMap = new ConcurrentHashMap<>();

        long coveredStates = r.getNumberOfCoveredStates();
        if (DETAILED_DEBUG) {
            System.out.println("Upper bound for no. of covered states: " + coveredStates);
            System.out.println("Size of reachable relevant states: " + reachableRelevantStates.size());
        }
        if (USE_APPLYSET_OPT && coveredStates < reachableRelevantStates.size()) {
            fillFromCoveredStates(r, appliedMap);
        }
        else {
            fillByIterating(r, appliedMap);
        }

        if (DETAILED_DEBUG)
            System.out.println("appliedMap size: " + appliedMap.size());

        for (Map.Entry<FFTNode, FFTMove> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            FFTMove m = entry.getValue();
            if (!updateSets(n, m, appliedMap, safe)) {
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
        return true;
    }

    private static void fillFromCoveredStates(Rule r, ConcurrentHashMap<FFTNode, FFTMove> appliedMap) {
        HashSet<LiteralSet> states = r.getCoveredStates();
        if (DETAILED_DEBUG) System.out.println("Exact no. of covered states: " + states.size());
        if (!SINGLE_THREAD) {
            states.parallelStream().forEach(set -> {
                FFTNode n = reachableRelevantStates.get(set.getBitString());
                insert(n, r, appliedMap);
            });
        }
        for (LiteralSet state : states) {
            FFTNode n = reachableRelevantStates.get(state.getBitString());
            insert(n, r, appliedMap);
        }
    }

    private static void fillByIterating(Rule r, ConcurrentHashMap<FFTNode, FFTMove> appliedMap) {
        if (!SINGLE_THREAD) {
            reachableRelevantStates.values().parallelStream().forEach(node ->
                    insert(node, r, appliedMap));
        } else {
            for (FFTNode n : reachableRelevantStates.values())
                insert(n, r, appliedMap);
        }
    }

    private static void insert(FFTNode n, Rule r, ConcurrentHashMap<FFTNode, FFTMove> appliedMap) {
        if (n == null)
            return;
        FFTMove m = r.apply(n);
        if (m != null)
            appliedMap.put(n, m);
    }

    // Either remove states from applySet in addition to reachableSet, or check whether state is still reachable
    private static boolean updateSets(FFTNode n, FFTMove chosenMove, ConcurrentHashMap<FFTNode, FFTMove> appliedMap,
                                      boolean safe) {
        if (safe) reachableRelevantStates.remove(n);
        // s might've been set unreachable by another state in appliedSet
        if (!n.isReachable()) {
            return true;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
        if (optimalMoves.isEmpty()) // terminal state
            return true;
        if (!optimalMoves.contains(chosenMove)) { // we choose wrong move
            if (isReachable(n, appliedMap))
                return false;
            else
                chosenMove = null;
        }
        if (!safe) // only update sets in a safe run
            return true;

        for (FFTMove m : optimalMoves) { // remove pointer to all children except chosen move
            if (m.equals(chosenMove)) { // chosen move
                continue;
            }
            FFTNode existingChild = reachableStates.get(n.getNextNode(m));
            // existingChild can be null if we re-visit a state where we already deleted it from
            // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
            if (existingChild == null)
                continue;
            existingChild.removeReachableParent(n);
            if (!existingChild.isReachable())
                removePointers(existingChild);
        }
        return true;
    }

    private static void removePointers(FFTNode parent) {
        reachableStates.remove(parent);
        reachableRelevantStates.remove(parent);
        for (FFTNode child : parent.getChildren()) {
            FFTNode existingChild = reachableStates.get(child);
            if (existingChild == null)
                continue;
            existingChild.removeReachableParent(parent);
            if (!existingChild.isReachable())
                removePointers(existingChild);
        }
    }

    // walk upwards through reachable parents until either initial state is found
    private static boolean isReachable(FFTNode node, ConcurrentHashMap<FFTNode, FFTMove> appliedMap) {
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
                FFTMove chosenMove = appliedMap.get(parent);
                // either not in appliedMap or chooses correct move
                if (chosenMove == null || parent.getNextNode(chosenMove).equals(n)) {
                    if (existingParent != null && !closedSet.contains(parent)) {
                        closedSet.add(parent);
                        frontier.add(existingParent);
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
        if (USE_RULE_ORDERING)
            reachableRelevantStates = new TreeMap<>(new NodeComparator());
        else
            reachableRelevantStates = new HashMap<>();
        reachableStates.put(initialNode, initialNode);
        if (team == PLAYER1)
            reachableRelevantStates.put(initialNode, initialNode);

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
            FFTMove chosenMove = fft.apply(node);
            if (chosenMove != null) {
                addNode(frontier, node, node.getNextNode(chosenMove));
            } else {
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                reachableRelevantStates.put(node, node);
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
}

