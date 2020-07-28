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

    private static FFT fft;
    private static boolean minimizing;

    // used for checking whether lifting a rule applies to more states
    private static int groundedAppliedMapSize;
    private static int liftedAppliedMapSize;

    // determines from what set we will do add/delete
    private static int REACHABLE = 0;
    private static int APPLICABLE = 1;
    private static int APPLIED = 2;

    public static void synthesize() {
        synthesize(null);
    }

    public static void synthesize(FFT existingFFT) {
        if (!FFTSolver.solved)
            FFTSolver.solveGame();

        if (BENCHMARK_MODE) {
            double avgRules = 0, avgPrecons = 0, avgTime = 0;
            for (int i = 0; i < BENCHMARK_NUMBER; i++) {
                long timeStart = System.currentTimeMillis();
                generate(existingFFT);
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
            generate(existingFFT);
        }
        addNewFFT(fft);
        if (SAVE_FFT)
            FFTManager.save();
    }

    private static void generate(FFT existingFFT) {
        if (existingFFT == null)
            fft = new FFT("Synthesis");
        else
            fft = existingFFT.clone();

        if (!fft.isValid(AUTOGEN_TEAM)) {
            System.err.println("Invalid fft");
            System.exit(1);
        }
        long timeStart = System.currentTimeMillis();

        setup();
        System.out.println("Making rules");
        if (NAIVE_RULE_GENERATION)
            makeNaiveRules();
        else
            makeRules();

        System.out.println("Amount of rules before minimizing: " + fft.getAmountOfRules());
        System.out.println("Amount of preconditions before minimizing: " + fft.getAmountOfPreconditions());
        double autoGenTimeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;

        System.out.println("Set sizes before minimize:");
        System.out.println("reachable: " + reachableStates.size());
        System.out.println("applicable: " + applicableStates.size());
        System.out.println("applied: " + appliedStates.size());


        if (DETAILED_DEBUG) System.out.println("Rules before minimizing");
        if (DETAILED_DEBUG) System.out.println(fft);


        int i = -1;
        timeStart = System.currentTimeMillis();
        if (MINIMIZE) {
            if (TESTING)
                checkMinimize();
            else if (USE_OPTIMIZED_MINIMIZE)
                i = minimize(fft);
            else
                i = fft.minimize();
        }
        double timeSpentMinimize = (System.currentTimeMillis() - timeStart) / 1000.0;

        System.out.println("Set sizes after minimize:");
        System.out.println("reachable: " + reachableStates.size());
        System.out.println("applicable: " + applicableStates.size());
        System.out.println("applied: " + appliedStates.size());

        System.out.println("Final amount of rules after " + i +
                " minimize iterations: " + fft.getAmountOfRules());
        System.out.println("Final amount of preconditions after " + i +
                " minimize iterations: " + fft.getAmountOfPreconditions());
        System.out.println("Time spent on Autogenerating: " + autoGenTimeSpent + " seconds");
        System.out.println("Time spent on minimizing: " + timeSpentMinimize + " seconds");
        System.out.println("Time spent in total: " + (timeSpentMinimize + autoGenTimeSpent) + " seconds");
        System.out.println("Final rules: \n" + fft);

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
                    ". Current amount of rules: " + fft.getAmountOfRules());
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
                Set<FFTMove> appliedMoves = fft.apply(node).getMoves();
                if (appliedMoves.isEmpty()) {
                    PropRule r = PropRule.createRule(node, chosenMove);
                    fft.append(r);
                    appliedMoves = r.apply(node);
                }
                for (FFTMove move : appliedMoves)
                    addNode(frontier, node, node.getNextNode(move));
            } else {
                fft.append(PropRule.createRule(node, chosenMove));
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
        fft.append(r);
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

        Set<FFTNode> appliedSet = new HashSet<>();
        simplifyRule(r, appliedSet, true);

        if (USE_LIFTING && !LIFT_BEFORE_SIMPLIFY) {
            r = liftRule((PropRule)r, true);
        }
        // symmetry and use_lifting can introduce new moves for an applied state, so we do a safe run at last
        safeRun(r, appliedSet, true); // safe run where we know we have the final rule

        System.out.println("added rule: " + r);
        if (SIMPLIFY_AFTER_DEL)
            simplifyAffectedRules(affectedRules);

        return r;
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
            boolean lastRule = (fft.getLastRule() == r);
            Set<Rule> newAffectedRules = new HashSet<>();
            Set<FFTNode> appliedSet = new HashSet<>();
            simplifyRule(r, appliedSet, lastRule);
            safeRun(r, appliedSet, lastRule);
            System.out.println("simplified to: " + r);
            System.out.println("newAffectedRules size: " + newAffectedRules.size());
            System.out.println("simplifying affected rules recursively");
            simplifyAffectedRules(newAffectedRules);
            simplified.addAll(newAffectedRules);
        }
    }

    private static void simplifyRule(Rule r, boolean lastRule) {
        simplifyRule(r, null, lastRule);
    }

    // if simplifying last rule, simplifying can not effect other rules, so it's simpler
    private static void simplifyRule(Rule r, Set<FFTNode> appliedSet, boolean lastRule) {
        if (DETAILED_DEBUG) System.out.println("SIMPLIFYING RULE: " + r);
        // make copy to avoid concurrentModificationException
        Collection<Literal> precons;
        if (USE_BITSTRING_SORT_OPT && lastRule) {
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
            fft.removePrecondition(r, l);
            if (!verifyRule(r, appliedSet, lastRule)) {
                if (DETAILED_DEBUG) System.out.println("FAILED TO REMOVE: " + l.getName());

                if (TESTING) {
                    System.out.println("testing if removing precondition: " + l + " , is valid");
                    checkVerification(r, minimizing, false);
                }

                fft.addPrecondition(r, l);
            } else {
                simplified = true;
                if (DETAILED_DEBUG) System.out.println("REMOVING PRECONDITION: " + l.getName());
                if (TESTING) {
                    System.out.println("Testing if removing precondition: " + l + " , is valid");
                    checkVerification(r, minimizing, true);
                }
            }
            if (DETAILED_DEBUG) System.out.println("SUCCESS, RULE IS NOW: " + r);
        }
        if (!lastRule && simplified)
            fft.removeDeadRules(r);
    }

    private static void fillAppliedMap(
            Rule r, Map<FFTNode, RuleMapping> appliedMap, boolean lastRule) {
        System.out.println("Filling applied map");
        if (!lastRule)
            appliedStates.findNodes(r, appliedMap);

        applicableStates.findNodes(r, appliedMap);

        if (!lastRule) { // replace value of all keys
            if (!SINGLE_THREAD) {
                appliedMap.keySet().parallelStream().forEach(key ->
                        appliedMap.replace(key, fft.apply(key)));

            } else {
                appliedMap.replaceAll((k, v) -> fft.apply(k));
            }
        }

    }

    private static void safeRun(Rule r, Set<FFTNode> appliedSet, boolean lastRule) {
        verifyRule(r, appliedSet, lastRule, true);
        if (TESTING)
            checkSetCorrectness();
    }

    private static void safeRun(Rule r, boolean lastRule) {
        verifyRule(r, null, lastRule, true);
        if (TESTING)
            checkSetCorrectness();
    }

    private static boolean verifyRule(Rule r, boolean lastRule) {
        return verifyRule(r, null, lastRule, false);
    }

    private static boolean verifyRule(Rule r, Set<FFTNode> appliedSet, boolean lastRule) {
        return verifyRule(r, appliedSet, lastRule, false);
    }

    // affectedRules is optimization, prevAppliedMap is used when symmetry is involved
    private static boolean verifyRule(Rule r, Set<FFTNode> prevApplied,
                                      boolean lastRule, boolean safe) {
        System.out.println("Verifying rule");
        if (safe && DETAILED_DEBUG)
            System.out.println("DOING SAFE RUN");
        Map<FFTNode, RuleMapping> appliedMap = new ConcurrentHashMap<>();

        fillAppliedMap(r, appliedMap, lastRule);

        // include applied states from previous verification
        /*
        if (prevApplied != null && lastRule) {
            for (FFTNode prev : prevApplied) {
                if (!appliedMap.containsKey(prev) && prev.isReachable())
                    appliedMap.put(prev, new RuleMapping(r, prev));
            }
        }

         */

        // suboptimalnodes contains all nodes that has a sub-optimal move or indirectly causes it through a chosenmove
        // addedNodes contains all nodes that has been added. deletedNodes does the opposite
        // addedParentNodes contains all the newly added reachable parents of a given state
        Set<FFTNode> suboptimalNodes = new HashSet<>();
        List<Set<FFTNode>> addedNodes = new ArrayList<>();
        List<Set<FFTNode>> deletedNodes = new ArrayList<>();
        Map<FFTNode, Set<FFTNode>> addedParentNodes = new HashMap<>();
        Map<FFTNode, Set<FFTNode>> deletedParentNodes = new HashMap<>();
        // fill add/delete sets with the 3 kind
        for (int i = 0; i < 3; i++) {
            addedNodes.add(new HashSet<>());
            deletedNodes.add(new HashSet<>());
        }
        Set<FFTNode> newAppliedNodes = new HashSet<>();
        updateSets(appliedMap, suboptimalNodes, addedNodes, deletedNodes,
                addedParentNodes, deletedParentNodes, lastRule, safe);

        System.out.println("Verification complete, doing checks");

        // check if any of the sub-optimal states are reachable
        // they will all be contained in current reachableStates(), so we need to check the new sets
        for (FFTNode subNode : suboptimalNodes) {
            if (reachableStates.containsKey(subNode)) {
                System.out.println("node: " + subNode + " is sub-optimal and still reachable!");
                undoChanges(addedNodes, deletedNodes, addedParentNodes, deletedParentNodes);
                return false;
            }
        }

        // todo - find a way to change this
        //if ((SYMMETRY_DETECTION || USE_LIFTING) && !safe)
          //  undoChanges(addedNodes, deletedNodes, addedParentNodes, deletedParentNodes);

        //if (TESTING)
        //    checkSetCorrectness();

        // used for lifting
        if (r instanceof PredRule)
            liftedAppliedMapSize = appliedMap.size();
        else
            groundedAppliedMapSize = appliedMap.size();

        // prevAppliedMap accumulates all the applied states for successful simplifications
        // the point is that we need to re-use them if symmetry is enabled
        // for instance if a state is made unreachable and later reachable within same rule simplification,
        // that state should be included in appliedMap
        // todo - look through addedNodes.get(applied) and add all nodes to prevAppliedMap where
        //        the rule is equal to the this rule. That should be all we need
        if (prevApplied != null)
            for (FFTNode n : newAppliedNodes)
                if (n.isReachable())
                    prevApplied.add(n);


        return true;
    }

    private static void undoChanges(List<Set<FFTNode>> addedNodes,
                                    List<Set<FFTNode>> deletedNodes,
                                    Map<FFTNode, Set<FFTNode>> addedParentNodes,
                                    Map<FFTNode, Set<FFTNode>> deletedParentNodes) {

        // todo - consider optimizing by looking at set diff between the add/remove sets
        // start with the major sets
        System.out.println("adding back states to reachable:");
        for (FFTNode deleted : deletedNodes.get(REACHABLE)) {
            reachableStates.put(deleted, deleted);
        }
        applicableStates.putAll(deletedNodes.get(APPLICABLE));
        appliedStates.putAll(deletedNodes.get(APPLIED));

        System.out.println("removing states from reachable:");
        for (FFTNode addedNode : addedNodes.get(REACHABLE)) {
            System.out.println("deleting from reachable: " + addedNode);
            reachableStates.remove(addedNode);
        }
        for (FFTNode addedNode : addedNodes.get(APPLICABLE)) {
            System.out.println("deleting from applicable: " + addedNode);
            applicableStates.remove(addedNode);
        }
        for (FFTNode addedNode : addedNodes.get(APPLIED)) {
            System.out.println("deleting from applied: " + addedNode);
            appliedStates.remove(addedNode);
        }

        // add/remove all the reachable parents
        for (Map.Entry<FFTNode, Set<FFTNode>> entry : deletedParentNodes.entrySet()) {
            System.out.println("adding to node: " + entry.getKey() + " , reachable parents: " + entry.getValue());
            entry.getKey().addReachableParents(entry.getValue());
        }
        for (Map.Entry<FFTNode, Set<FFTNode>> entry : addedParentNodes.entrySet()) {
            System.out.println("removing from node: " + entry.getKey() + " , reachable parents: " + entry.getValue());
            entry.getKey().removeReachableParents(entry.getValue());
        }
    }

    private static void updateSets(Map<FFTNode, RuleMapping> appliedMap, Set<FFTNode> suboptimalNodes,
                                   List<Set<FFTNode>> addedNodes,
                                   List<Set<FFTNode>> deletedNodes,
                                   Map<FFTNode, Set<FFTNode>> addedParentNodes,
                                   Map<FFTNode, Set<FFTNode>> deletedParentNodes,
                                   boolean lastRule, boolean safe) {
        // check if states are sub-optimal, and add all relevant transitions
        System.out.println("appliedMap:");
        for (Map.Entry<FFTNode, RuleMapping> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            System.out.println("Checking if node: " + n + " , is valid");
            // this might happen if we use prevAppliedSet
            if (!n.isReachable()) {
                System.out.println("node: " + n + " , is not reachable");
                continue;
            }
            Set<FFTMove> chosenMoves = entry.getValue().getMoves();
            System.out.println("chosenMoves: " + chosenMoves);

            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
            System.out.println("optimal moves: " + optimalMoves);
            boolean suboptimal = false;
            // start by determining if this state is suboptimal, and if so, just return immediately after adding to sets
            // no chosen moves, not all moves are optimal, thus outcome is sub-optimal (if minimizing)
            if (minimizing && chosenMoves.isEmpty() && optimalMoves.size() != n.getLegalMoves().size()) {
                System.out.println("adding node: " + n + " , to suboptimalNodes");
                suboptimalNodes.add(n);
                suboptimal = true;
            }
            // if chosenMoves is non-empty, require them to be optimal
            for (FFTMove chosenMove : chosenMoves) {
                if (!optimalMoves.contains(chosenMove)) {
                    System.out.println("adding node: " + n + " , to suboptimalNodes");
                    suboptimalNodes.add(n);
                    suboptimal = true;
                    break;
                }
            }
            if (suboptimal)
                continue;
            System.out.println("optimal move is chosen for node: " + n);
            // check if adding back transitions results in sub-optimal moves from child states
            // if symmetry detection enabled, a simplification might give a node more chosenMoves
            if (!lastRule || SYMMETRY_DETECTION || USE_LIFTING) {
                System.out.println("Checking if adding back transitions for node: " + n + " , is verified");
                Collection<? extends FFTMove> moves;
                if (chosenMoves.isEmpty()) {
                    System.out.println("Chosen moves empty");
                    moves = optimalMoves;
                    if (!applicableStates.contains(n)) {
                        System.out.println("adding node: " + n + " , to applicableStates");
                        applicableStates.put(n);
                        addedNodes.get(APPLICABLE).add(n);

                    }
                } else {
                    moves = chosenMoves;
                }
                for (FFTMove m : moves) {
                    if (!addToSets(n, m, addedNodes, addedParentNodes, safe)) {
                        System.out.println("Failed to add the transition from node: " + n + ", with move: " + m);
                        suboptimalNodes.add(n);
                        break;
                    }
                }
            }
            System.out.println("node: " + n + " , is verified");
        }
        System.out.println("removeSets for all nodes");
        // now we remove from sets for all nodes. We use isReachable() to check for unreachable loops
        for (Map.Entry<FFTNode, RuleMapping> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            System.out.println("starting removal from node: " + n);
            Set<FFTMove> chosenMoves = entry.getValue().getMoves();
            System.out.println("chosenMoves: " + chosenMoves);

            // s might've been set unreachable by another state in appliedSet
            if (!n.isReachable() || suboptimalNodes.contains(n)) {
                System.out.println("node: " + n + " , is either not reachable or suboptimal");
                continue;
            }

            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
            System.out.println("optimal moves: " + optimalMoves);
            // if chosenMoves is empty, don't remove anything
            // can happen when removing rules and no rules cover
            if (chosenMoves.isEmpty())
                continue;

            // remove all except for chosenMoves
            for (FFTMove m : optimalMoves) {
                if (!chosenMoves.contains(m))
                    removeFromSets(n, m, deletedNodes, deletedParentNodes, appliedMap);
            }
        }

        // Adjusting sets for all nodes still reachable
        System.out.println("Adjusting sets for all nodes!");
        for (Map.Entry<FFTNode, RuleMapping> entry : appliedMap.entrySet()) {
            FFTNode n = entry.getKey();
            System.out.println("adjusting sets for node: " + n);
            adjustSets(n, entry.getValue(), addedNodes, deletedNodes, safe);
        }
    }

    // removes all children from the sets recursively. Stops if the state is still reachable after removing the link
    // takes the addNodes/addParentNodes as argument to prevent deleting if it is made reachable in addToSets
    private static void removeFromSets(FFTNode n, FFTMove m,
                                       List<Set<FFTNode>> deletedNodes,
                                       Map<FFTNode, Set<FFTNode>> deletedParentNodes,
                                       Map<FFTNode, RuleMapping> appliedMap) {
        FFTNode child = n.getNextNode(m);
        System.out.println("child: " + child);

        FFTNode existingChild = reachableStates.get(child);
        if (existingChild == null) {
            System.out.println("existing child null, returning");
            return;
        }

        System.out.println("existingChild: " + existingChild);
        System.out.println("reachable parents: " + existingChild.getReachableParents());
        System.out.println("deleting node: " + n + " , from reachable parents");
        if (existingChild.getReachableParents().contains(n)) {
            existingChild.removeReachableParent(n);
            Set<FFTNode> deletedParents = deletedParentNodes.getOrDefault(existingChild, new HashSet<>());
            deletedParents.add(n);
            deletedParentNodes.put(existingChild, deletedParents);
        }
        if (existingChild.isReachable() && isReachable(existingChild, appliedMap)) {
            System.out.println("existing child: " + existingChild + " , still reachable");
            return;
        }
        remove(existingChild, deletedNodes, deletedParentNodes);
        // todo - consider optimizing by other calling function for relevant children
        for (FFTMove move : existingChild.getLegalMoves())
            removeFromSets(existingChild, move, deletedNodes, deletedParentNodes, appliedMap);
    }

    private static void remove(FFTNode n, List<Set<FFTNode>> deletedNodes,
                               Map<FFTNode, Set<FFTNode>> deletedParentNodes) {
        System.out.println("removing node: " + n + " , from everything");
        if (reachableStates.containsKey(n)) {
            System.out.println("removing node: " + n + " , from reachable");
            reachableStates.remove(n);
            deletedNodes.get(REACHABLE).add(n);
        }
        if (applicableStates.contains(n)) {
            System.out.println("removing node: " + n + " , from applicable");
            applicableStates.remove(n);
            deletedNodes.get(APPLICABLE).add(n);
        }
        if (appliedStates.contains(n)) {
            System.out.println("removing node: " + n + " , from applied");
            appliedStates.remove(n);
            deletedNodes.get(APPLIED).add(n);
        }
        Set<FFTNode> deletedParents = deletedParentNodes.getOrDefault(n, new HashSet<>());
        deletedParents.addAll(n.getReachableParents());
        deletedParentNodes.put(n, deletedParents);
        n.clearReachableParents();
        System.out.println("deleted node:" + n + " , from everything");
        System.out.println("reachable parents: " + n.getReachableParents() + " , reachable: " + n.isReachable());
    }

    private static boolean addToSets(FFTNode node, FFTMove move, List<Set<FFTNode>> addedNodes,
                                  Map<FFTNode, Set<FFTNode>> addedParentNodes,
                                     boolean safe) {
        LinkedList<FFTNodeAndMove> frontier = new LinkedList<>();
        Set<FFTNode> closedSet = new HashSet<>(); // prevent looping when not safe
        frontier.add(new FFTNodeAndMove(node, move));
        while (!frontier.isEmpty()) {
            FFTNodeAndMove nm = frontier.pop();
            FFTNode n = nm.getNode();
            FFTMove m = nm.getMove();
            FFTNode child = n.getNextNode(m);
            FFTNode existingChild = reachableStates.get(child);
            System.out.println("adding transition from node: " + n + " , with move: " + m);
            System.out.println("existing child: " + existingChild);
            if (existingChild != null) {
                if (!existingChild.getReachableParents().contains(n)) {
                    System.out.println("for node:" + existingChild + " , adding reachable parent: " + n);
                    existingChild.addReachableParent(n);
                    Set<FFTNode> addParents = addedParentNodes.getOrDefault(existingChild, new HashSet<>());
                    addParents.add(n);
                    addedParentNodes.put(existingChild, addParents);
                }
                System.out.println("continuing");
                continue;
            }

            if (closedSet.contains(child)) {
                System.out.println("closedSet contains: " + child);
                continue;
            }
            closedSet.add(child);

            if (child.isTerminal()) {
                System.out.println("child: " + child + " , is terminal");
                continue;
            }

            System.out.println("putting node: " + child + " , in reachablestates");
            System.out.println("adding reachable parent: " + n);
            child.addReachableParent(n);
            System.out.println("new reachable parents: " + child.getReachableParents());
            addedNodes.get(REACHABLE).add(child);
            Set<FFTNode> addParents = addedParentNodes.getOrDefault(child, new HashSet<>());
            addParents.add(n);
            addedParentNodes.put(child, addParents);
            reachableStates.put(child, child);

            if (child.getTurn() != AUTOGEN_TEAM) {
                for (FFTMove legalMove : child.getLegalMoves())
                    frontier.add(new FFTNodeAndMove(child, legalMove));
                continue;
            }

            ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(child);
            System.out.println("Optimal moves for child: " + child + " : " + optimalMoves);
            // find new move (if any)
            RuleMapping rm = fft.apply(child);
            Set<FFTMove> newMoves = rm.getMoves();
            System.out.println("newMoves: " + newMoves);
            adjustSets(child, rm, addedNodes, new ArrayList<>(), safe);

            if (newMoves.isEmpty()) {
                // check that every move is legal if minimizing
                if (minimizing && optimalMoves.size() != child.getLegalMoves().size()) {
                    System.out.println("Chosen moves empty and not every move is optimal");
                    return false;
                }
                // add all optimal moves
                for (FFTMove optMove : optimalMoves)
                    frontier.add(new FFTNodeAndMove(child, optMove));
            } else {
                for (FFTMove newMove : newMoves) {
                    if (!optimalMoves.contains(newMove)) {
                        System.out.println("Chosen moves were not all optimal");
                        return false;
                    }
                    frontier.add(new FFTNodeAndMove(child, newMove));
                }
            }
        }
        return true;
    }

    public static void adjustSets(FFTNode n, RuleMapping rm, List<Set<FFTNode>> addedNodes,
                                  List<Set<FFTNode>> deletedNodes, boolean safe) {
        System.out.println("adjusting sets for node:" + n);
        System.out.println("chosenMoves:" + rm.getMoves());
        if (!n.isReachable()) {
            System.out.println("node: " + n + " , is not reachable");
            return;
        }
        Set<FFTMove> chosenMoves = rm.getMoves();
        // when deleting a rule, chosenMoves might be empty and state is no longer applied
        if (chosenMoves.isEmpty() || simplifyingLastRule(rm, safe)) {
            if (appliedStates.contains(n)) {
                System.out.println("removing node " + n + " , from appliedStates");
                appliedStates.remove(n);
                deletedNodes.get(APPLIED).add(n);

            }
            if (!applicableStates.contains(n)) {
                System.out.println("adding node: " + n + " , to applicablestates");
                applicableStates.put(n);
                addedNodes.get(APPLICABLE).add(n);
            }
        } else {
            if (!appliedStates.contains(n)) {
                System.out.println("adding node " + n + " , to appliedStates");
                addedNodes.get(APPLIED).add(n);
                appliedStates.put(n);
            }

            if (applicableStates.contains(n)) {
                System.out.println("deleting node " + n + " , from applicablestates");
                applicableStates.remove(n);
                deletedNodes.get(APPLICABLE).add(n);
            }
        }
    }

    private static boolean simplifyingLastRule(RuleMapping rm, boolean safe) {
        boolean lastRule = (SYMMETRY_DETECTION || USE_LIFTING) &&
                rm.getRule().equals(fft.getLastRule()) && !safe;
        if (lastRule)
            System.out.println("we are simplifying last rule!");
        return lastRule;
    }

    // walk upwards through reachable parents until initial state is found
    private static boolean isReachable(FFTNode node,
                                       Map<FFTNode, RuleMapping> appliedMap) {
        System.out.println("checking if node: " + node + " , isReachable()");
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
                if (existingParent == null || closedSet.contains(parent))
                    continue;
                RuleMapping rm = appliedMap.get(parent);
                // not in appliedMap or all moves optimal, add parent
                if (rm == null || rm.getMoves().isEmpty()) {
                    closedSet.add(existingParent);
                    frontier.add(existingParent);
                } else{
                    // chooses correct move
                    for (FFTMove chosenMove : rm.getMoves()) {
                        if (parent.getNextNode(chosenMove).equals(n)) {
                            closedSet.add(existingParent);
                            frontier.add(existingParent);
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("node: " + node + " , is not reachable");
        return false;
    }

    // assumes that the FFT is strongly optimal
    public static int minimize(FFT f) {
        minimizing = true;
        fft = f;
        int ruleSize, precSize;
        int i = 0;
        do {
            ruleSize = fft.getAmountOfRules();
            precSize = fft.getAmountOfPreconditions();
            i++;
            System.out.println("Minimizing, iteration no. " + i);
            minimizeRules();
        } while (MINIMIZE_ITERATIVELY &&
                (ruleSize != fft.getAmountOfRules() || precSize != fft.getAmountOfPreconditions()));
        minimizing = false;
        return i;
    }

    private static void minimizeRules() {
        ArrayList<Rule> rulesCopy = new ArrayList<>(fft.getRules());
        int removed = 0;
        for (int i = 0; i < rulesCopy.size(); i++) {
            Rule r = rulesCopy.get(i);
            if (r.isLocked())
                continue;
            if ((i - removed) >= fft.size()) // remaining rules are removed
                break;
            if (DETAILED_DEBUG || NAIVE_RULE_GENERATION)
                System.out.println("Remaining amount of rules: " + fft.getAmountOfRules());
            if (!fft.getRules().get(i - removed).equals(r)) {
                System.out.println("This rule has already been deleted");
                removed++;
                continue;
            }
            System.out.println("Attempting to remove rule: " + r);
            fft.removeRule(i - removed);
            removed++;
            if (!verifyRule(r, false)) {
                if (TESTING) {
                    System.out.println("Testing verification when removing rule");
                    checkVerification(r, true, false);
                }

                removed--;
                fft.addRule(r, i - removed);
                if (MINIMIZE_PRECONDITIONS)
                    simplifyRule(r,false);
                if (LIFT_WHEN_MINIMIZING && !(r instanceof PredRule)) {
                    PropRule propRule = (PropRule) r;
                    liftRule(propRule, false);
                }
            } else {
                System.out.println("removed rule: " + r);
                if (TESTING) {
                    System.out.println("Testing verification when removing rule");
                    checkVerification(r, true, true);
                }
            }
            safeRun(r, false);
        }
    }

    // todo - consider doing a check for sub-optimal here
    // it is cost-free and would be nice for e.g. the minimizing function
    public static void findReachableStates() {
        int team = AUTOGEN_TEAM;
        FFTNode initialNode = FFTManager.initialFFTNode.clone();
        initialNode.setReachable(true);
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
        if (team == PLAYER1) {
            Set<FFTMove> moves = fft.apply(initialNode).getMoves();
            if (moves.isEmpty())
                applicableStates.put(initialNode);
            else
                appliedStates.put(initialNode);
        }

        while (!frontier.isEmpty()) {
            FFTNode node = frontier.pop();

            // Not our turn
            if (team != node.getTurn()) {
                for (FFTMove legalMove : node.getLegalMoves())
                    addNode(frontier, node, node.getNextNode(legalMove));
                continue;
            }
            Set<FFTMove> chosenMoves = fft.apply(node).getMoves();
            if (!chosenMoves.isEmpty()) {
                appliedStates.put(node);
                for (FFTMove chosenMove : chosenMoves)
                    addNode(frontier, node, node.getNextNode(chosenMove));
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
            fft.replaceRule(r, pr);
            if (DETAILED_DEBUG) System.out.println("VERIFYING LIFTED RULE: " + pr);
            // we do not want to lift if the lifted rule does not apply to more states
            if (verifyRule(pr, null, lastRule) && groundedAppliedMapSize != liftedAppliedMapSize) {
                if (DETAILED_DEBUG) System.out.println("RULE SUCCESSFULLY LIFTED TO: " + pr);
                return pr;
            } else {
                fft.replaceRule(pr, r);
            }
        }
        if (DETAILED_DEBUG) System.out.println("FAILED TO LIFT RULE");
        return r;
    }

    // Remaining functions are for testing purposes
    // The generated set seems to be correct since the function passes, but it results in the program
    // failing elsewhere for some reason. What could it be?
    private static void checkSetCorrectness() {
        for (FFTNode n : reachableStates.keySet()) {
            if (!n.isReachable()) {
                System.out.println("node: " + n + " , is not reachable even though its part of reachable sets??");
                System.exit(1);
            }
        }
        // make copy of sets
        HashMap<FFTNode, FFTNode> reachableCopy = new HashMap<>();
        NodeMap applicableCopy = new NodeMap(applicableStates.getSort());
        NodeMap appliedCopy = new NodeMap(appliedStates.getSort());
        for (FFTNode n : reachableStates.keySet()) {
            FFTNode cloned = n.clone();
            HashSet<FFTNode> reachable = new HashSet<>(n.getReachableParents());
            cloned.addReachableParents(reachable);
            reachableCopy.put(cloned, cloned);

            if (applicableStates.contains(cloned))
                applicableCopy.put(cloned);
            if (appliedStates.contains(cloned))
                appliedCopy.put(cloned);
        }

        findReachableStates();

        // check if sizes match
        if (reachableCopy.size() != reachableStates.size()) {
            System.out.println("reachable state size diff");
            for (FFTNode n : reachableStates.keySet()) {
                FFTNode existingNode = reachableCopy.get(n);
                if (existingNode == null) {
                    System.out.println("node: " + n + " , is in correct map but not in copy!");
                }
            }
            System.exit(1);
        }

        if (applicableCopy.size() != applicableStates.size()) {
            System.out.println("applicable states size diff");
            for (FFTNode n : applicableStates.getMap().keySet()) {
                FFTNode existingNode = applicableCopy.get(n);
                if (existingNode == null) {
                    System.out.println("node: " + n + " , is in correct map but not in copy!");
                }
            }
            System.exit(1);
        }

        if (appliedCopy.size() != appliedStates.size()) {
            System.out.println("applied states size diff");
            for (FFTNode n : appliedStates.getMap().keySet()) {
                FFTNode existingNode = appliedCopy.get(n);
                if (existingNode == null) {
                    System.out.println("node: " + n + " , is in correct map but not in copy!");
                }
            }
            System.exit(1);
        }

        // check node differences in detail
        for (FFTNode n : reachableCopy.keySet()) {
            FFTNode existingNode = reachableStates.get(n);
            if (existingNode == null) {
                System.out.println("node: " + n + " , was in reachable copy but not in static!");
                LinkedList<FFTNode> frontier = new LinkedList<>();
                HashSet<FFTNode> closedSet = new HashSet<>();
                frontier.add(n);
                System.out.println("doing upwards search with reachable parents");
                while(!frontier.isEmpty()) {
                    FFTNode node = frontier.pop();
                    System.out.println("node: " + node + " , reachable parents: " + node.getReachableParents());
                    System.out.println("exists in copy: " + reachableCopy.containsKey(node));
                    if (node.equals(initialFFTNode)) {
                        System.out.println("initial node found");
                        break;
                    }
                    for (FFTNode parent : node.getReachableParents()) {
                        if (!closedSet.contains(parent)) {
                            frontier.add(parent);
                            closedSet.add(parent);
                        } else {
                            System.out.println("parent in closed set: " + parent);
                        }
                    }
                }
                System.exit(1);
            }

            if (!existingNode.getReachableParents().equals(n.getReachableParents())) {
                System.out.println("reachable parent discrepancy!");
                System.out.println("node: " + n);
                System.out.println("copy parents: " + n.getReachableParents());
                System.out.println("correct parents: " + existingNode.getReachableParents());
                System.exit(1);
            }

            if (applicableCopy.contains(n) && !applicableStates.contains(n)) {
                System.out.println("applicable states discrepancy!");
                System.out.println("node: " + n + " , was in copy but not in correct map!");
                System.exit(1);
            }

            if (applicableStates.contains(n) && !applicableCopy.contains(n)) {
                System.out.println("applicable states discrepancy!");
                System.out.println("node: " + n + " , was in correct map but not in copy!");
                System.exit(1);
            }

            if (appliedCopy.contains(n) && !appliedStates.contains(n)) {
                System.out.println("Applied states discrepancy!");
                System.out.println("node: " + n + " , was in copy but not in correct map!");
                System.exit(1);
            }

            if (appliedStates.contains(n) && !appliedCopy.contains(n)) {
                System.out.println("Applied states discrepancy!");
                System.out.println("node: " + n + " , was in correct map but not in copy!");
                System.exit(1);
            }

        }
    }

    public static void checkMinimize() {
        FFT copy = new FFT(fft);

        // start with optimized one in case of verification errors
        minimize(fft);
        copy.minimize();
        System.out.println("RULES WITH OLD MINIMIZE:");
        System.out.println(copy);
        System.out.println("RULES WITH NEW MINIMIZE:");
        System.out.println(fft);
        System.out.println("COMPARING FFT's AFTER MINIMIZING");
        System.out.println("SIZE OF OLD:" + copy.size());
        System.out.println("SIZE OF NEW:" + fft.size());
        for (int x = 0; x < copy.getRules().size(); x++) {
            Rule r = copy.getRules().get(x);
            if (!fft.getRules().get(x).equals(r)) {
                System.out.println("Rule discrepancy!");
                System.out.println("Rule with old minimization: " + r);
                System.out.println("Rule with new minimization: " + fft.getRules().get(x));
                System.exit(1);
            }
        }
    }

    public static void checkVerification(Rule r, boolean complete, boolean expectedResult) {
        if (fft.verify(AUTOGEN_TEAM, complete) != expectedResult) {
            System.out.println("verification discrepancy!");
            System.out.println("expected verification to be: " + expectedResult);
            System.out.println("discrepancy from rule: " + r);
            System.out.println("failing point: " + fft.getFailingPoint());
            System.out.println("current rules: " + fft);
            System.exit(1);
        }
    }
}

