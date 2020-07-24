package fftlib;

import fftlib.auxiliary.NodeMap;
import fftlib.game.*;
import fftlib.logic.FFT;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.PredRule;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.Rule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static fftlib.FFTManager.*;
import static misc.Config.*;
import static misc.Globals.PLAYER1;

public class FFTAutoGen {
    // States that are reachable when playing with a partial or total strategy
    private static Map<FFTNode, FFTNode> reachableStates;
    // Subset of reachable states where strategy does not output a move, and player has the turn
    // This is the set of states that a new rule can potentially influence (subset of reachable)
    private static NodeMap applicableStates;
    // This is the set of states that the rules currently apply to (subset of reachable)
    private static NodeMap appliedStates;

    private static FFT copy;

    private static boolean minimizing;

    // used for checking whether lifting a rule applies to more states
    private static int groundedAppliedMapSize;
    private static int liftedAppliedMapSize;

    public static void synthesize() {
        if (!FFTSolver.solved)
            FFTSolver.solveGame();
        if (currFFT == null)
            addNewFFT("Synthesis");

        if (!currFFT.isValid(AUTOGEN_TEAM)) {
            System.err.println("Invalid fft");
            System.exit(1);
        }

        if (BENCHMARK_MODE) {
            double avgRules = 0, avgPrecons = 0, avgTime = 0;
            for (int i = 0; i < BENCHMARK_NUMBER; i++) {
                long timeStart = System.currentTimeMillis();
                generate();
                avgTime += (System.currentTimeMillis() - timeStart) / 1000.0;
                avgRules += currFFT.getAmountOfRules();
                avgPrecons += currFFT.getAmountOfPreconditions();
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
        if (SAVE_FFT)
            FFTManager.save();
    }

    private static void generate() {
        long timeStart = System.currentTimeMillis();

        setup();
        System.out.println("Making rules");
        if (NAIVE_RULE_GENERATION)
            makeNaiveRules();
        else
            makeRules();

        System.out.println("Amount of rules before minimizing: " + currFFT.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + currFFT.getAmountOfPreconditions());
        double autoGenTimeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;

        /*
        System.out.println("Set sizes before minimize:");
        System.out.println("reachable: " + reachableStates.size());
        System.out.println("applicable: " + applicableStates.size());
        System.out.println("applied: " + appliedStates.size());

         */

        if (DETAILED_DEBUG) System.out.println("Rules before minimizing");
        if (DETAILED_DEBUG) System.out.println(currFFT);


        int i = -1;
        //copy = new FFT(fft);
        timeStart = System.currentTimeMillis();
        if (MINIMIZE) {
            i = minimize();

        }
        double timeSpentMinimize = (System.currentTimeMillis() - timeStart) / 1000.0;

/*
        System.out.println("RULES WITH OLD MINIMIZE:");
        System.out.println(copy);
        System.out.println("RULES WITH NEW MINIMIZE:");
        System.out.println(fft);
        System.out.println("COMPARING FFT's AFTER MINIMIZING");
        for (int x = 0; x < copy.getRules().size(); x++) {
            Rule r = copy.getRules().get(x);
            if (!fft.getRules().get(x).equals(r)) {
                System.out.println("Rule discrepancy!");
                System.out.println("Rule with old minimization: " + r);
                System.out.println("Rule with new minimization: " + fft.getRules().get(x));
                System.exit(1);
            }
        }

 */

        System.out.println("Final amount of rules after " + i +
                " minimize iterations: " + currFFT.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i +
                " minimize iterations: " + currFFT.getAmountOfPreconditions());
        System.out.println("Time spent on Autogenerating: " + autoGenTimeSpent + " seconds");
        System.out.println("Time spent on minimizing: " + timeSpentMinimize + " seconds");
        System.out.println("Time spent in total: " + (timeSpentMinimize + autoGenTimeSpent) + " seconds");
        System.out.println("Final rules: \n" + currFFT);

        /*
        System.out.println("Set sizes after minimize:");
        System.out.println("reachable: " + reachableStates.size());
        System.out.println("applicable: " + applicableStates.size());
        System.out.println("applied: " + appliedStates.size());

         */
    }

    private static void setup() {
        // Set reachable parents for all states
        if (!NAIVE_RULE_GENERATION)
            findReachableStates();

        System.out.println("Solution size: " + FFTSolution.size());
        if (!NAIVE_RULE_GENERATION) {
            System.out.println("Number of reachable states: " + reachableStates.size());
            System.out.println("Number of applicable states: " + applicableStates.size());
            System.out.println("Number of applied states: " + appliedStates.size());
        }
    }

    private static void makeRules() {
        Set<FFTNode> skippedNodes = new HashSet<>();
        while (applicableStates.size() > 0) {
            System.out.println("Remaining applicable states: " + applicableStates.size() +
                    ". Current amount of rules: " + currFFT.getAmountOfRules());
            Iterator<FFTNode> it = applicableStates.values().iterator();
            FFTNode node = it.next();

            /*  //Skipping rules probably bad
            boolean onlySkips = false;
            while (skippedNodes.contains(node)) {
                if (!it.hasNext()) {
                    onlySkips = true;
                    break;
                }
                node = it.next();
            }
            if (onlySkips)
                break;

            if (FFTSolution.optimalMoves(node).size() == node.getLegalMoves().size()) {
                // don't need a rule for this state
                System.out.println("Skipping rule");
                skippedNodes.add(node);
                continue;
            }
            */

            Rule r = addRule(node);
            if (DETAILED_DEBUG) System.out.println("FINAL RULE: " + r);
            System.out.println();
        }
    }

    private static void makeNaiveRules() {
        // make search through state space playing with the minimax strategy, adding a rule for each move chosen,
        // even the moves where all moves are optimal
        int team = AUTOGEN_TEAM;
        FFTNode initialNode = FFTManager.initialFFTNode;
        LinkedList<FFTNode> frontier;
        frontier = new LinkedList<>();
        frontier.add(initialNode);

        reachableStates = new HashMap<>();
        appliedStates = new NodeMap(NodeMap.NO_SORT);
        applicableStates = new NodeMap(NodeMap.NO_SORT);
        reachableStates.put(initialNode, initialNode);
        while (!frontier.isEmpty()) {
            FFTNode node = frontier.pop();

            // Not our turn
            if (team != node.getTurn()) {
                for (FFTNode child : node.getChildren())
                    addNode(frontier, node, child);
                continue;
            }
            appliedStates.put(node);
            FFTMove chosenMove = FFTSolution.queryNode(node).move;

            // if symmetry, only add rule if node is not covered
            // expand on all chosen moves, since symmetric rule can have several chosen moves
            if (SYMMETRY_DETECTION) {
                Set<FFTMove> appliedMoves = currFFT.apply(node);
                if (appliedMoves.isEmpty()) {
                    PropRule r = PropRule.createRule(node, chosenMove);
                    //System.out.println("adding rule: " + r);
                    currFFT.append(r);
                    appliedMoves = r.apply(node);
                }
                //System.out.println("appliedMoves: " + appliedMoves);
                for (FFTMove move : appliedMoves)
                    addNode(frontier, node, node.getNextNode(move));
            } else {
                currFFT.append(PropRule.createRule(node, chosenMove));
                addNode(frontier, node, node.getNextNode(chosenMove));
            }
        }

        System.out.println("Number of reachable states: " + reachableStates.size());
        System.out.println("Number of applied states: " + appliedStates.size());
    }

    private static Rule addRule(FFTNode n) {
        LiteralSet stSet = new LiteralSet(n.convert());
        FFTMove bestMove = FFTSolution.queryNode(n).move;

        Rule r = new PropRule(stSet, bestMove.convert()); // rule from state-move pair
        currFFT.append(r);
        // set of rules affected by deleing states when minimizing
        // if set to null then no rules will be added to set
        Set<Rule> affectedRules = null;
        if (SIMPLIFY_AFTER_DEL)
            affectedRules = new HashSet<>();

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL NODE: " + n + " , AND MOVE: " + bestMove);
            System.out.println("NODE SCORE: " + FFTSolution.queryNode(n).score);
            System.out.println("ORIGINAL RULE: " + r);
        }
        if (USE_LIFTING && LIFT_BEFORE_SIMPLIFY)
            r = liftRule((PropRule) r, true);

        simplifyRule(r, true);

        if (USE_LIFTING && !LIFT_BEFORE_SIMPLIFY) {
            r = liftRule((PropRule)r, true);
        }
        // symmetry and use_lifting can introduce new moves for an applied state, so we do a safe run at last
        safeRun(r, affectedRules,true); // safe run where we know we have the final rule

/*
        if (!fft.verify(AUTOGEN_TEAM, false)) {
            System.out.println("ERROR: Old verification failed where new did not");
            System.out.println("Failing point: " + fft.failingPoint);
            System.exit(1);
        }

 */
        System.out.println("added rule: " + r);
        if (SIMPLIFY_AFTER_DEL)
            simplifyAffectedRules(affectedRules);

        return r;
    }

    private static void safeRun(Rule r, Set<Rule> affectedRules, boolean lastRule) {
        verifyRule(r, affectedRules, lastRule, true);
    }

    private static void safeRun(Rule r, boolean lastRule) {
        verifyRule(r, null, lastRule, true);
    }

    private static void simplifyAffectedRules(Set<Rule> affectedRules) {
        System.out.println("No. of affected rules: " + affectedRules.size());
        Set<Rule> simplified = new HashSet<>(); // keep track of rules already simplified in the recursion
        for (Rule r : affectedRules) {
            System.out.println("Simplifying: " + r);
            if (simplified.contains(r)) {
                System.out.println("Rule already simplified");
                continue;
            }
            boolean lastRule = (currFFT.getLastRule() == r);
            Set<Rule> newAffectedRules = new HashSet<>();
            simplifyRule(r, lastRule);
            safeRun(r, newAffectedRules, lastRule);
            System.out.println("simplified to: " + r);
            System.out.println("newAffectedRules size: " + newAffectedRules.size());
            System.out.println("simplifying affected rules recursively");
            simplifyAffectedRules(newAffectedRules);
            simplified.addAll(newAffectedRules);
        }
    }

    // if simplifying last rule, simplifying can not effect other rules, so it's simpler
    private static void simplifyRule(Rule r, boolean lastRule) {
        if (DETAILED_DEBUG) System.out.println("SIMPLIFYING RULE: " + r);
        // make copy to avoid concurrentModificationException
        Collection<Literal> precons;
        if (USE_BITSTRING_SORT_OPT && lastRule) { // outcomment lastRule if you want to compare to original minimization
            // sort the literals so we start with literal with lowest ID
            TreeMap<Integer, Literal> literalSet = new TreeMap<>();
            for (Literal l : r.getPreconditions()) {
                literalSet.put(l.id, l);
            }
            precons = literalSet.values();
        } else {
            precons = new LiteralSet(r.getPreconditions());
        }
        boolean simplified = false;
        for (Literal l : precons) {
            if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO REMOVE: " + l.getName());
            currFFT.removePrecondition(r, l);
            if (!verifyRule(r, lastRule)) {
                if (DETAILED_DEBUG) System.out.println("FAILED TO REMOVE: " + l.getName());
                currFFT.addPrecondition(r, l);
            } else {
                simplified = true;
                if (DETAILED_DEBUG) System.out.println("REMOVING PRECONDITION: " + l.getName());
            }
            if (DETAILED_DEBUG) System.out.println("RULE IS NOW: " + r);
        }
        if (!lastRule && simplified)
            currFFT.removeDeadRules(r);
    }

    private static void fillAppliedMap(
            Rule r, Map<FFTNode, Set<FFTMove>> appliedMap, boolean lastRule, boolean safe) {

        if (!lastRule)
            appliedStates.findNodes(r, appliedMap, safe);

        applicableStates.findNodes(r, appliedMap, safe);

        if (!lastRule) { // replace value of all keys
            if (!SINGLE_THREAD) {
                appliedMap.keySet().parallelStream().forEach(key ->
                        appliedMap.replace(key, currFFT.apply(key, safe)));

            } else {
                appliedMap.replaceAll((k, v) -> currFFT.apply(k, safe));
            }
        }

    }

    private static boolean verifyRule(Rule r, boolean lastRule) {
        return verifyRule(r, null, lastRule, false);
    }

    private static boolean verifyRule(Rule r, Set<Rule> affectedRules, boolean lastRule, boolean safe) {
        //System.out.println("Verifying rule");
        if (minimizing && !USE_OPTIMIZED_MINIMIZE)
            return currFFT.verify(AUTOGEN_TEAM, true);
        if (safe && DETAILED_DEBUG)
            System.out.println("DOING SAFE RUN");
        Map<FFTNode, Set<FFTMove>> appliedMap = new ConcurrentHashMap<>();

        fillAppliedMap(r, appliedMap, lastRule, safe);

        //System.out.println("appliedMap:");
        for (Map.Entry<FFTNode, Set<FFTMove>> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            Set<FFTMove> moves = entry.getValue();
            //System.out.println(n + " , moves: " + moves);
            //System.out.println("Checking if node is valid");
            if (!updateSets(n, moves, appliedMap, affectedRules, lastRule, safe)) {
                //System.out.println("Node is invalid!");
                return false;
            }
            //System.out.println("Node is valid!");
        }
        if (r instanceof PredRule)
            liftedAppliedMapSize = appliedMap.size();
        else
            groundedAppliedMapSize = appliedMap.size();

        return true;
    }

    private static boolean updateSets(FFTNode n, Set<FFTMove> chosenMoves,
                                          Map<FFTNode, Set<FFTMove>> appliedMap, Set<Rule> affectedRules,
                                      boolean lastRule, boolean safe) {
        boolean reachable = true;
        // s might've been set unreachable by another state in appliedSet
        if (!n.isReachable()) {
            return true;
        }
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
        //System.out.println("Optimal moves: " + optimalMoves);
        //System.out.println("reachable parents: " + n.getReachableParents());
        if (optimalMoves.isEmpty()) // terminal state
            return true;

        // if chosenMoves empty, require all moves to be optimal (if state isReachable())
        if (chosenMoves.isEmpty() && optimalMoves.size() != n.getLegalMoves().size()) {
            if (isReachable(n, appliedMap)) {
                //System.out.println("Chosen moves empty and not all moves optimal");
                return false;
            }
            else {
                //System.out.println("This node is not reachable");
                reachable = false;
            }
        }

        // if chosenMoves is non-empty, require them to be optimal
        for (FFTMove chosenMove : chosenMoves) {
            if (!optimalMoves.contains(chosenMove)) {
                if (isReachable(n, appliedMap)) {
                    //System.out.println("Move: " + chosenMove + " , is not optimal");
                    return false;
                }
                else {
                    //System.out.println("This node is not reachable");
                    reachable = false;
                    break;
                }
            }
        }
        // only check if we can add sets back if node is reachable. We may not have checked that earlier
        if (!lastRule && reachable)
            reachable = isReachable(n, appliedMap);
        // add back transitions (if not last rule)
        if (!lastRule && reachable) {
            //System.out.println("Checking if adding back transitions is verified");
            if (chosenMoves.isEmpty()) {
                if (safe) {
                    applicableStates.put(n);
                }
                //System.out.println("Chosen moves empty, checking if we can add back all transitions");
                for (FFTMove m : optimalMoves) {
                    if (!addToSets(n, m, safe)) {
                       //System.out.println("Failed to add the transition from node: " + n + ", with move: " + m);
                        return false;
                    }
                }
            } else {
                //System.out.println("Chosen moves non-empty, checking if we can add back chosen moves");
                for (FFTMove m : chosenMoves)
                    if (!addToSets(n, m, safe)) {
                        //System.out.println("Failed to add the transition from node: " + n + ", with move: " + m);
                        return false;
                    }
            }
        }
        if (!safe)
            return true;
        //System.out.println("Removing transitions");
        if (!reachable) {
            for (FFTMove m : optimalMoves) // remove all
                removeFromSets(n, m, affectedRules);
        } else if (!chosenMoves.isEmpty()) { // remove all but to chosenMoves
            for (FFTMove m : optimalMoves)
                if (!chosenMoves.contains(m))
                    removeFromSets(n, m, affectedRules);
        }

        // Adjusting sets
        if (reachable) {
            // when deleting a rule, chosenMoves might be empty and state is no longer applied
            if (chosenMoves.isEmpty()) {
                //System.out.println("removing node " + n + " , from appliedStates");
                appliedStates.remove(n);
                applicableStates.put(n);
            } else {
                // simplifying an intermediate rule might affect both an applied state and applicable state
                if (!appliedStates.contains(n)) {
                    //System.out.println("Adding node: " + n + " , to appliedStates");
                    appliedStates.put(n);
                }
                applicableStates.remove(n);
            }
        }

        return true;
    }

    // you might remove all children from a state and end up removing the state itself cause of recursion
    // should use a closedSet to prevent looping back, like we do with addToSets
    private static void removeFromSets(FFTNode n, FFTMove m, Set<Rule> affectedRules) {
        FFTNode existingChild = reachableStates.get(n.getNextNode(m));
        // existingChild can be null if we re-visit a state where we already deleted it from
        // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
        // getReachableParents may not contain 'n', e.g. if we simplify an intermediate rule
        if (existingChild == null)
            return;
        existingChild.removeReachableParent(n);
        if (existingChild.isReachable())
            return;
        remove(existingChild, affectedRules);
        for (FFTMove move : existingChild.getLegalMoves()) {
            removeFromSets(existingChild, move, affectedRules);
        }
    }

    private static void remove(FFTNode n, Set<Rule> affectedRules) {
        reachableStates.remove(n);
        applicableStates.remove(n);
        appliedStates.remove(n);
        if (affectedRules != null) {// it's null when minimizing
            Rule appliedRule = n.getAppliedRule();
            if (appliedRule != null)
                affectedRules.add(appliedRule);
        }


    }

    private static boolean addToSets(FFTNode node, FFTMove move, boolean safe) {
        LinkedList<FFTNodeAndMove> frontier = new LinkedList<>();
        Set<FFTNode> closedSet = new HashSet<>(); // prevent looping when not safe
        frontier.add(new FFTNodeAndMove(node, move));
        while (!frontier.isEmpty()) {
            FFTNodeAndMove nm = frontier.pop();
            FFTNode n = nm.getNode();
            FFTMove m = nm.getMove();
            FFTNode child = n.getNextNode(m);
            FFTNode existingChild = reachableStates.get(child);
            //System.out.println("adding transition from node: " + n + " , with move: " + m);
            //System.out.println("existing child exists: " + (existingChild != null));
            if (existingChild != null) {
                if (safe) existingChild.addReachableParent(n);
                continue;
            }
            if (closedSet.contains(child))
                continue;
            closedSet.add(child);

            if (child.isTerminal())
                continue;

            if (safe) {
                //System.out.println("inserting child: " + child + " , in reachable states");
                reachableStates.put(child, child);
                child.addReachableParent(n);
            }

            if (child.getTurn() != AUTOGEN_TEAM) {
                for (FFTMove legalMove : child.getLegalMoves())
                    frontier.add(new FFTNodeAndMove(child, legalMove));

                continue;
            }

            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(child);
            //System.out.println("Optimal moves for child: " + child + " : " + optimalMoves);
            // find new move (if any)
            Set<FFTMove> newMoves = currFFT.apply(child);
            //System.out.println("newMoves: " + newMoves);
            if (newMoves.isEmpty()) {
                if (safe) {
                    // this state is now applicable
                    applicableStates.put(child);
                }
                // check that every move is legal
                if (optimalMoves.size() != child.getLegalMoves().size()) {
                    //System.out.println("Chosen moves empty and not every move is optimal");
                    return false;
                }
                // we already assume it is valid, meaning optimalMoves = legalMoves
                for (FFTMove optMove : optimalMoves)
                    frontier.add(new FFTNodeAndMove(child, optMove));
            } else {
                if (safe) {
                    // this state is now applied
                    appliedStates.put(child);
                }
                for (FFTMove newMove : newMoves) {
                    if (!optimalMoves.contains(newMove)) {
                        //System.out.println("Chosen move was not optimal");
                        return false;
                    }
                    frontier.add(new FFTNodeAndMove(child, newMove));

                }
            }
        }
        return true;
    }

    // walk upwards through reachable parents until initial state is found
    private static boolean isReachable(FFTNode node,
                                       Map<FFTNode, Set<FFTMove>> appliedMap) {
        //System.out.println("Checking if node" + node + " , is reachable");
        LinkedList<FFTNode> frontier = new LinkedList<>();
        Set<FFTNode> closedSet = new HashSet<>();
        frontier.add(node);
        closedSet.add(node);

        while (!frontier.isEmpty()) {
            FFTNode n = frontier.pop();
            if (n.equals(FFTNode.getInitialNode())) {
                return true;
            }
            for (FFTNode parent : n.getReachableParents()) {
                FFTNode existingParent = reachableStates.get(parent);
                //System.out.println("Parent: " + parent + " , exists: " + (existingParent != null));
                if (existingParent == null || closedSet.contains(parent))
                    continue;
                Set<FFTMove> chosenMoves = appliedMap.get(parent);
                //System.out.println("chosenMoves: " + chosenMoves);
                // not in appliedMap or all moves optimal, add parent
                if (chosenMoves == null || chosenMoves.isEmpty()) {
                    //System.out.println("No chosen moves, adding parent: " + parent + " , to frontier");
                    closedSet.add(parent);
                    frontier.add(existingParent);
                } else{
                    // chooses correct move
                    for (FFTMove chosenMove : chosenMoves) {
                        if (parent.getNextNode(chosenMove).equals(n)) {
                            //System.out.println("Correct move, adding parent: " + parent + " , to frontier");
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

    // assumes that the FFT is strongly optimal
    public static int minimize() {
        minimizing = true;
        int ruleSize, precSize;
        int i = 0;
        do {
            ruleSize = currFFT.getAmountOfRules();
            precSize = currFFT.getAmountOfPreconditions();
            i++;
            System.out.println("Minimizing, iteration no. " + i);
            minimizeRules();
        } while (ruleSize != currFFT.getAmountOfRules() || precSize != currFFT.getAmountOfPreconditions());
        minimizing = false;
        return i;
    }

    private static void minimizeRules() {
        ArrayList<Rule> rulesCopy = new ArrayList<>(currFFT.getRules());
        int removed = 0;
        for (int i = 0; i < rulesCopy.size(); i++) {
            Rule r = rulesCopy.get(i);
            if (r.isLocked())
                continue;
            if ((i - removed) >= currFFT.getRules().size()) // remaining rules are removed
                break;
            if (DETAILED_DEBUG || NAIVE_RULE_GENERATION)
                System.out.println("Remaining amount of rules: " + currFFT.getAmountOfRules());
            if (!currFFT.getRules().get(i - removed).equals(r)) { // some rules deleted
                removed++;
                continue;
            }

            currFFT.removeRule(i - removed);

            removed++;
            boolean verify = (USE_OPTIMIZED_MINIMIZE) ? verifyRule(r, false) : currFFT.verify(AUTOGEN_TEAM, true);
            if (!verify) {
                removed--;
                currFFT.addRule(r, i - removed);
                if (MINIMIZE_PRECONDITIONS)
                    simplifyRule(r, false);
                if (LIFT_WHEN_MINIMIZING && !(r instanceof PredRule)) {
                    PropRule propRule = (PropRule) r;
                    liftRule(propRule, false);
                }
                //System.out.println("Doing safe run");
                if (USE_OPTIMIZED_MINIMIZE)
                    safeRun(r, false);
            }
        }
    }

    public static void findReachableStates() {
        int team = AUTOGEN_TEAM;
        FFTNode initialNode = FFTManager.initialFFTNode;
        LinkedList<FFTNode> frontier;
        frontier = new LinkedList<>();
        frontier.add(initialNode);
        reachableStates = new HashMap<>();
        appliedStates = new NodeMap(NodeMap.NO_SORT);
        int sort = NodeMap.NO_SORT;
        if (USE_BITSTRING_SORT_OPT)
            sort = NodeMap.BITSTRING_SORT;
        else if (USE_RULE_ORDERING)
            sort = NodeMap.RULE_SORT;
        applicableStates = new NodeMap(sort);


        reachableStates.put(initialNode, initialNode);
        if (team == PLAYER1 && currFFT.apply(initialNode).isEmpty()) {
            applicableStates.put(initialNode);
        }

        while (!frontier.isEmpty()) {
            FFTNode node = frontier.pop();

            // Not our turn
            if (team != node.getTurn()) {
                for (FFTNode child : node.getChildren())
                    addNode(frontier, node, child);
                continue;
            }
            Set<FFTMove> chosenMoves = currFFT.apply(node);
            if (!chosenMoves.isEmpty()) {
                appliedStates.put(node);
                for (FFTMove m : chosenMoves)
                    addNode(frontier, node, node.getNextNode(m));
            } else {
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                applicableStates.put(node);
                // add all states from optimal moves
                for (FFTMove m : optimalMoves)
                    addNode(frontier, node, node.getNextNode(m));
            }
        }
    }

    private static void addNode(LinkedList<FFTNode> frontier, FFTNode parent, FFTNode child) {
        if (child.isTerminal())
            return;
        if (!reachableStates.containsKey(child)) {
            reachableStates.put(child, child);
            frontier.add(child);
        }
        reachableStates.get(child).addReachableParent(parent);
    }

    private static Rule liftRule(PropRule r, boolean lastRule) {
        if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO LIFT RULE");
        // attempt to lift propositional variables one at a time, sorted by the most occurring variable 1st
        for (int prop : r.getSortedProps()) {
            PredRule pr = r.liftAll(prop);
            if (pr == null) // inconsistent
                continue;
            currFFT.replaceRule(r, pr);
            if (DETAILED_DEBUG) System.out.println("VERIFYING LIFTED RULE: " + pr);
            // we do not want to lift if the lifted rule does not apply to more states
            if (verifyRule(pr, lastRule) && groundedAppliedMapSize != liftedAppliedMapSize) {
                if (DETAILED_DEBUG) System.out.println("RULE SUCCESSFULLY LIFTED TO: " + pr);
                return pr;
            } else {
                currFFT.replaceRule(pr, r);
            }
        }
        if (DETAILED_DEBUG) System.out.println("FAILED TO LIFT RULE");
        return r;
    }
}

