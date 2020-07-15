package fftlib;

import fftlib.auxiliary.NodeMap;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.FFTNodeAndMove;
import fftlib.game.FFTSolution;
import fftlib.logic.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private static FFT fft;

    private static FFT copy;

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
        if (NAIVE_RULE_GENERATION)
            makeNaiveRules();
        else
            makeRules();

        System.out.println("Amount of rules before minimizing: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + fft.getAmountOfPreconditions());
        double autoGenTimeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;

        /*
        System.out.println("Set sizes before minimize:");
        System.out.println("reachable: " + reachableStates.size());
        System.out.println("applicable: " + applicableStates.size());
        System.out.println("applied: " + appliedStates.size());

         */

        if (DETAILED_DEBUG) System.out.println("Rules before minimizing");
        if (DETAILED_DEBUG) System.out.println(fft);


        int i = -1;
        //copy = new FFT(fft);
        timeStart = System.currentTimeMillis();
        if (MINIMIZE) {
            if (USE_MINIMIZE_OPT)
                i = minimizeOpt();
            else
                i = fft.minimize(AUTOGEN_TEAM, MINIMIZE_PRECONDITIONS);

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
                " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i +
                " minimize iterations: " + fft.getAmountOfPreconditions());
        System.out.println("Time spent on Autogenerating: " + autoGenTimeSpent + " seconds");
        System.out.println("Time spent on minimizing: " + timeSpentMinimize + " seconds");
        System.out.println("Time spent in total: " + (timeSpentMinimize + autoGenTimeSpent) + " seconds");
        System.out.println("Final rules: \n" + fft);

        /*
        System.out.println("Set sizes after minimize:");
        System.out.println("reachable: " + reachableStates.size());
        System.out.println("applicable: " + applicableStates.size());
        System.out.println("applied: " + appliedStates.size());

         */
    }

    private static void setup(boolean existingFFT) {
        if (!existingFFT) {
            fft = new FFT("Synthesis");
            fft.addRuleGroup(new RuleGroup("Synthesis"));
        }

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
                    ". Current amount of rules: " + fft.size());
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
        // initialize rule list
        if (USE_APPLY_OPT)
            fft.initializeRuleList();
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
                Set<FFTMove> appliedMoves = fft.apply(node);
                if (appliedMoves.isEmpty()) {
                    Rule r = Rule.createRule(node, chosenMove);
                    //System.out.println("adding rule: " + r);
                    fft.append(r);
                    appliedMoves = r.apply(node);
                }
                //System.out.println("appliedMoves: " + appliedMoves);
                for (FFTMove move : appliedMoves)
                    addNode(frontier, node, node.getNextNode(move));
            } else {
                fft.append(Rule.createRule(node, chosenMove));
                addNode(frontier, node, node.getNextNode(chosenMove));
            }
        }

        // initialize rule list
        if (USE_APPLY_OPT)
            fft.initializeRuleList();

        System.out.println("Number of reachable states: " + reachableStates.size());
        System.out.println("Number of applied states: " + appliedStates.size());
    }

    private static Rule addRule(FFTNode n) {
        LiteralSet stSet = new LiteralSet(n.convert());
        FFTMove bestMove = FFTSolution.queryNode(n).move;

        Rule r = new Rule(stSet, bestMove.convert()); // rule from state-move pair
        fft.append(r);

        // DEBUG
        if (DETAILED_DEBUG) {
            System.out.println("ORIGINAL NODE: " + n + " , AND MOVE: " + bestMove);
            System.out.println("NODE SCORE: " + FFTSolution.queryNode(n).score);
            System.out.println("ORIGINAL RULE: " + r);
        }
        if (USE_LIFTING && LIFT_BEFORE_SIMPLIFY)
            r = liftRule(r, true);

        simplifyRule(r, true);

        if (USE_LIFTING && !LIFT_BEFORE_SIMPLIFY) {
            Rule pr = liftRule(r, true);
            // simplify again?
            if (pr != r)
                simplifyRule(pr, true); // TODO - benchmark
            r = pr;
        }
        if (SYMMETRY_DETECTION || USE_LIFTING) // symmetry and use_lifting can introduce new moves for an applied state, so we do a safe run at last
            verifyRule(r, true,true); // safe run where we know we have the final rule

/*
        if (!fft.verify(AUTOGEN_TEAM, false)) {
            System.out.println("ERROR: Old verification failed where new did not");
            System.out.println("Failing point: " + fft.failingPoint);
            System.exit(1);
        }

 */

        return r;
    }

    // if simplifying last rule, simplifying can not effect other rules, so it's simpler
    private static void simplifyRule(Rule r, boolean lastRule) {
        if (DETAILED_DEBUG) System.out.println("SIMPLIFYING RULE: " + r);
        // make copy to avoid concurrentModificationException
        int prevSize;
        int i = 1;
        do {
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
            if (DETAILED_DEBUG && SIMPLIFY_ITERATIVELY)
                System.out.println("SIMPLIFICATION ITERATION: " + i++);
            prevSize = precons.size();
            boolean simplified = false;
            for (Literal l : precons) {
                if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO REMOVE: " + l.getName());
                fft.removePrecondition(r, l);
                if (!verifyRule(r, lastRule, false)) {
                    if (DETAILED_DEBUG) System.out.println("FAILED TO REMOVE: " + l.getName());
                    fft.addPrecondition(r, l);
                } else {
                    simplified = true;
                    if (DETAILED_DEBUG) System.out.println("REMOVING PRECONDITION: " + l.getName());
                }
                if (DETAILED_DEBUG) System.out.println("RULE IS NOW: " + r);
            }
            if (!lastRule && simplified)
                fft.removeDeadRules(r);
        } while (SIMPLIFY_ITERATIVELY && prevSize != r.getPreconditions().size() &&
                r.getPreconditions().size() != 0);
    }

    private static Rule liftRule(Rule r, boolean lastRule) {
        if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO LIFT RULE");
        // attempt to lift propositional variables one at a time, sorted by the most occurring variable 1st
        for (int prop : r.getSortedProps()) {
            PredRule pr = r.liftAll(prop);
            if (pr == null) // inconsistent
                continue;
            fft.replaceRule(r, pr);
            if (DETAILED_DEBUG) System.out.println("VERIFYING LIFTED RULE: " + pr);
            // we do not want to lift if the lifted rule does not apply to more states
            if (verifyRule(pr, lastRule,false) && groundedAppliedMapSize != liftedAppliedMapSize) {
                if (DETAILED_DEBUG) System.out.println("RULE SUCCESSFULLY LIFTED TO: " + pr);
                return pr;
            } else {
                fft.replaceRule(pr, r);
            }
        }
        if (DETAILED_DEBUG) System.out.println("FAILED TO LIFT RULE");
        return r;
    }

    private static void fillAppliedMap(
            Rule r, Map<FFTNode, Set<FFTMove>> appliedMap, boolean lastRule, boolean safe) {

        if (!lastRule)
            appliedStates.findNodes(r, appliedMap, safe);

        applicableStates.findNodes(r, appliedMap, safe);

        if (!lastRule) { // replace value of all keys
            if (!SINGLE_THREAD) {
                appliedMap.keySet().parallelStream().forEach(key ->
                        appliedMap.replace(key, fft.apply(key)));

            } else {
                appliedMap.replaceAll((k, v) -> fft.apply(k));
            }
        }

    }

    private static boolean verifyRule(Rule r, boolean lastRule, boolean safe) {
        //System.out.println("Verifying rule");
        if (safe && DETAILED_DEBUG && (SYMMETRY_DETECTION || USE_LIFTING))
            System.out.println("DOING SAFE RUN");
        Map<FFTNode, Set<FFTMove>> appliedMap = new ConcurrentHashMap<>();

        fillAppliedMap(r, appliedMap, lastRule, safe);

        //System.out.println("appliedMap:");
        for (Map.Entry<FFTNode, Set<FFTMove>> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            Set<FFTMove> moves = entry.getValue();
            //System.out.println(n + " , moves: " + moves);
            //System.out.println("Checking if node is valid");
            if (!updateSets(n, moves, appliedMap, lastRule, safe)) {
                //System.out.println("Node is invalid!");
                return false;
            }
            //System.out.println("Node is valid!");
        }
        if (r instanceof PredRule)
            liftedAppliedMapSize = appliedMap.size();
        else
            groundedAppliedMapSize = appliedMap.size();

        // If g(s) = f(s), which is the case with no symmetry and lifting,
        // we can here do another updateSets() for all applied states
        if (!SYMMETRY_DETECTION && !USE_LIFTING)
            for (Map.Entry<FFTNode, Set<FFTMove>> entry : appliedMap.entrySet()) {
                //System.out.println("Doing safe updateSets for node: " + entry.getKey());
                //System.out.println("moves: " + entry.getValue());
                updateSets(entry.getKey(), entry.getValue(), appliedMap, lastRule, true);
            }
        return true;
    }

    private static boolean updateSets(FFTNode n, Set<FFTMove> chosenMoves,
                                          Map<FFTNode, Set<FFTMove>> appliedMap,
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
                removeFromSets(n, m);
        } else if (!chosenMoves.isEmpty()) { // remove all but to chosenMoves
            for (FFTMove m : optimalMoves)
                if (!chosenMoves.contains(m))
                    removeFromSets(n, m);
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
    private static void removeFromSets(FFTNode n, FFTMove m) {
        FFTNode existingChild = reachableStates.get(n.getNextNode(m));
        // existingChild can be null if we re-visit a state where we already deleted it from
        // i.e. we deleted all but chosen move from this state, so it's still reachable, but children isn't
        // getReachableParents may not contain 'n', e.g. if we simplify an intermediate rule
        if (existingChild == null)
            return;
        existingChild.removeReachableParent(n);
        if (existingChild.isReachable())
            return;
        remove(existingChild);
        for (FFTMove move : existingChild.getLegalMoves()) {
            removeFromSets(existingChild, move);
        }
    }

    private static void remove(FFTNode n) {
        //System.out.println("removing child: " + existingChild);
        reachableStates.remove(n);
        applicableStates.remove(n);
        appliedStates.remove(n);

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
            Set<FFTMove> newMoves = fft.apply(child);
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
    private static int minimizeOpt() {
        int ruleSize, precSize;
        int i = 0;
        do {
            ruleSize = fft.getAmountOfRules();
            precSize = fft.getAmountOfPreconditions();
            i++;
            System.out.println("Minimizing, iteration no. " + i);
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
            ArrayList<Rule> rulesCopy = new ArrayList<>(rg.rules);
            int removed = 0;
            for (int i = 0; i < rulesCopy.size(); i++) {
                if ((i - removed) >= rg.rules.size()) // remaining rules are removed
                    break;

                if (DETAILED_DEBUG || NAIVE_RULE_GENERATION)
                    System.out.println("Remaining amount of rules: " + fft.size());
                Rule r = rulesCopy.get(i);
                if (!rg.rules.get(i - removed).equals(r)) {// some later rules deleted
                    removed++;
                    continue;
                }
                //System.out.println("Attempting to remove rule: " + r);
                fft.removeRule(r, i - removed);
                removed++;
                if (!verifyRule(r, false, false)) {
                    //System.out.println("failed to remove: " + r);
                    //System.out.println();
                    removed--;
                    fft.addRule(r, i - removed);
                    if (MINIMIZE_RULE_BY_RULE && MINIMIZE_PRECONDITIONS) {
                        //System.out.println("Attempting to simplify rule: " + r);
                        simplifyRule(r, false);
                        //System.out.println();
                        if (LIFT_WHEN_MINIMIZING && !(r instanceof PredRule))
                            liftRule(r, false);
                    }
                }
                //System.out.println("Doing safe run");
                if (SYMMETRY_DETECTION || USE_LIFTING)
                    verifyRule(r, false,true);

/*
                if (!fft.verify(AUTOGEN_TEAM, true)) {
                    System.out.println("Failed to verify the FFT!");
                    System.out.println("failing point: " + fft.failingPoint);
                    System.out.println("Exiting");
                    System.exit(1);
                }
                System.out.println();
 */
            }
        }
    }

    private static void minimizePreconditions() {
        for (RuleGroup rg : fft.ruleGroups) {
            if (rg.locked) continue; // don't minimize if rg is locked
            for(Rule r : rg.rules) {
                //System.out.println("Simplifying rule");
                simplifyRule(r, false);
                if (SYMMETRY_DETECTION || USE_LIFTING)
                    verifyRule(r, false,true);
            }
        }
    }

    private static void findReachableStates() {
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
        if (team == PLAYER1 && fft.apply(initialNode).isEmpty()) {
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
            Set<FFTMove> chosenMoves = fft.apply(node);
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

    public static Map<FFTNode, FFTNode> getReachableStates() {
        return reachableStates;
    }

    public static NodeMap getAppliedStates() {
        return appliedStates;
    }



}

