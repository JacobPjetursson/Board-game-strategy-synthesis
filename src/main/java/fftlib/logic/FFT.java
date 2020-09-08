package fftlib.logic;

import fftlib.FFTManager;
import fftlib.GGPAutogen.Database;
import fftlib.GGPAutogen.GGPManager;
import fftlib.auxiliary.algo.InvertedList;
import fftlib.auxiliary.algo.RuleList;
import fftlib.game.*;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.PredRule;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.MetaRule;
import misc.Config;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static misc.Config.*;
import static misc.Globals.*;


public class FFT {
    private String name;
    // used for a simple representation of the rules
    private ArrayList<Rule> rules;
    private ArrayList<MetaRule> metaRules;
    // used for a simple list of rules in correct order ,which is handy when minimizing
    // used for the efficient computation of looking up rules that apply
    private RuleList ruleList;
    private InvertedList invertedList;


    private FFTNode failingPoint = null;

    // For concurrency purposes
    private static ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    private ConcurrentHashMap<FFTNode, Boolean> closedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<MachineState, Boolean> closedMapGGP = new ConcurrentHashMap<>();
    private boolean failed; // failed is set to true if verification failed

    public FFT(String name) {
        this.name = name;
        rules = new ArrayList<>();
        metaRules = new ArrayList<>();
        ruleList = new RuleList();
        invertedList = new InvertedList(false);
    }

    public FFT(FFT duplicate) {
        this.name = duplicate.name;
        if (duplicate.failingPoint != null)
            this.failingPoint = duplicate.failingPoint.clone();

        this.rules = new ArrayList<>();
        for (Rule r : duplicate.rules)
            this.rules.add(r.clone());

        this.metaRules = new ArrayList<>();
        for (MetaRule mr : duplicate.metaRules)
            this.metaRules.add(new MetaRule(this, mr.name, mr.startIdx, mr.endIdx));

        ruleList = new RuleList();
        if (USE_RULELIST) {
            for (Rule r : this.rules)
                ruleList.sortedAdd(r);
        }
        invertedList = new InvertedList(false);
        if (USE_INVERTED_LIST_RULES_OPT) {
            for (Rule r : this.rules) {
                invertedList.add((PropRule)r);
            }
        }
    }

    public FFT clone() {
        return new FFT(this);
    }

    public int getAmountOfRules() {
        return rules.size();
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }

    public void addMetaRule(MetaRule metaRule) {
        metaRules.add(metaRule);
    }

    public void removeMetaRule(MetaRule mr, boolean deep) {
        // if shallow remove, only remove metarule (name and interval)
        // if deep remove, remove all rules in metarule
        if (deep) {
            ArrayList<Rule> rulesCopy = new ArrayList<>(rules);
            rules.removeAll(rulesCopy.subList(mr.startIdx, mr.endIdx));
        }
        metaRules.remove(mr);
    }

    public void append(Rule r) {
        r.setRuleIndex(size());
        rules.add(r);
        // add to sorted list of rules
        if (USE_RULELIST)
            ruleList.sortedAdd(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            // only works when no predicate rules and no symmetry
            PropRule propRule = (PropRule) r;
            invertedList.add(propRule);
        }
    }

    public void removePrecondition(Rule r, Literal l) {
        if (USE_RULELIST) {
            ruleList.sortedRemove(r);
            r.removePrecondition(l);
            ruleList.sortedAdd(r);
        } else if (USE_INVERTED_LIST_RULES_OPT) {
            // no predicate rules or symmetry allowed (yet)
            PropRule propRule = (PropRule) r;
            invertedList.remove(propRule);
            propRule.removePrecondition(l);
            invertedList.add(propRule);
        } else {
            r.removePrecondition(l);
        }
    }

    public void addPrecondition(Rule r, Literal l) {
        if (USE_RULELIST) {
            ruleList.sortedRemove(r);
            r.addPrecondition(l);
            ruleList.sortedAdd(r);
        } else if (USE_INVERTED_LIST_RULES_OPT) {
            PropRule propRule = (PropRule) r;
            invertedList.remove(propRule);
            r.addPrecondition(l);
            invertedList.add(propRule);
        } else {
            r.addPrecondition(l);
        }
    }

    // faster since we don't have to search for rule
    public void removeRule(int idx) {
        Rule r = rules.get(idx);
        rules.remove(idx);

        if (USE_RULELIST)
            ruleList.sortedRemove(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            PropRule propRule = (PropRule) r;
            invertedList.remove(propRule);
        }
        updateMetaRules(true, idx);
    }


    public void addRule(Rule r, int idx) {
        rules.add(idx, r);

        if (USE_RULELIST)
            ruleList.sortedAdd(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            PropRule propRule = (PropRule) r;
            invertedList.add(propRule);
        }
        updateMetaRules(false, idx);
    }

    public void moveRule(int oldIdx, int newIdx) {
        Rule r = rules.get(oldIdx);
        // remove
        rules.remove(oldIdx);
        if (USE_RULELIST)
            ruleList.sortedRemove(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            PropRule propRule = (PropRule) r;
            invertedList.remove(propRule);
        }
        // add
        rules.add(newIdx, r);
        if (USE_RULELIST)
            ruleList.sortedAdd(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            PropRule propRule = (PropRule) r;
            invertedList.add(propRule);
        }
        updateMetaRules(oldIdx, newIdx);
    }

    public boolean isValid(int team) {
        int winner = FFTManager.winner;
        if (team == PLAYER1 && winner == PLAYER2) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && winner == PLAYER1) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }
        return true;
    }

    public int getAmountOfPreconditions() {
        int precSize = 0;
        for (Rule r : rules)
            precSize += r.getAmountOfPreconditions();
        return precSize;
    }

    public int size() {
        return getAmountOfRules();
    }

    public ArrayList<MetaRule> getMetaRules() {
        return metaRules;
    }

    public FFTNode getFailingPoint() {
        return failingPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public Rule getLastRule() {
        return rules.get(rules.size()-1);
    }

    // This function updates the indices at every metarule (relevant when minimizing or otherwise changing the FFT)
    private void updateMetaRules(boolean removing, int idx) {
        for (MetaRule mr : metaRules) {
            mr.updateIntervals(removing, idx);
        }
    }

    private void updateMetaRules(int oldIdx, int newIdx) {
        for (MetaRule mr : metaRules) {
            mr.updateIntervals(oldIdx, newIdx);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Rule r : rules)
            sb.append(r).append("\n");
        return sb.toString();
    }

    public RuleMapping apply_slow(FFTNode node) {
        HashSet<FFTMove> moves;
        for (Rule r : getRules()) {
            moves = r.apply(node);
            if (!moves.isEmpty()) {
                return new RuleMapping(r, moves);
            }
        }
        return RuleMapping.NOMATCH;
    }

    public RuleMapping apply(FFTNode node) {
        if (USE_RULELIST)
            return ruleList.apply(node);
        if (USE_INVERTED_LIST_RULES_OPT)
            return invertedList.apply(node);
        return apply_slow(node);
    }

    // for all rules r' with a higher index than r.index, check if r is subset of r'
    // if yes, then r' is dead. Maybe we can use containsAll
    // Alternative: We can use r.apply(r') to determine the same thing.
    // Perhaps we can even use the invertedList/ruleList stuff
    public void removeDeadRules(Rule rule) {
        ArrayList<Rule> rulesCopy = new ArrayList<>(rules);
        int removed = 0;
        for (int i = 0; i < rulesCopy.size(); i++) {
            Rule r = rulesCopy.get(i);
            if (isDead(rule, r)) {
                removeRule(i - removed);
                removed++;
            }

        }
    }

    private boolean isDead(Rule first, Rule second) {
        boolean dead = second.getRuleIndex() > first.getRuleIndex() &&
                !first.apply(second.getAllPreconditions()).isEmpty();
        if (dead && DETAILED_DEBUG)
            System.out.println("Dead rule at index " + second.getRuleIndex() + ": " + second);
        return dead;
    }

    public void replaceRule(Rule oldRule, Rule newRule) {
        ArrayList<Rule> rulesCopy = new ArrayList<>(rules);
        for (int i = 0; i < rulesCopy.size(); i++) {
            Rule r = rulesCopy.get(i);
            if (oldRule == r) {
                removeRule(i);
                addRule(newRule, i);
                return;
            }
        }
    }

    public boolean verify(int team, boolean complete) {
        if (!Config.ENABLE_GGP && SINGLE_THREAD) {
            return verifySingleThread(team, complete);
        }

        return (Config.ENABLE_GGP) ? verifyGGP(team, complete) : verify_(team, complete);
    }

    public boolean verifySingleThread(int team, boolean complete) {
        if (team == PLAYER_ANY)
            return verifySingleThread(PLAYER1, complete) && verifySingleThread(PLAYER2, complete);
        FFTNode initialNode = FFTManager.getInitialNode();
        LinkedList<FFTNode> frontier;
        frontier = new LinkedList<>();
        HashSet<FFTNode> closedSet = new HashSet<>();
        frontier.add(initialNode);
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        if (!isValid(team))
            return false;
        while (!frontier.isEmpty()) {
            FFTNode node = frontier.pop();
            if (node.isTerminal()) {
                if (node.getWinner() == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            } else if (team != node.getTurn()) {
                for (FFTNode child : node.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                Set<FFTMove> moves = apply(node).getMoves();
                List<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                // If move is null, check that all possible (random) moves are ok
                if (moves.isEmpty()) {
                    for (FFTMove m : node.getLegalMoves()) {
                        if (optimalMoves.contains(m)) {
                            FFTNode nextNode = node.getNextNode(m);
                            if (!closedSet.contains(nextNode)) {
                                closedSet.add(nextNode);
                                frontier.add(nextNode);
                            }
                        } else if (complete) {
                            failingPoint = node.clone();
                            return false;
                        }
                    }
                } else {
                    for (FFTMove m : moves) {
                        if (!optimalMoves.contains(m)) {
                            failingPoint = node.clone();
                            return false;
                        }
                        FFTNode nextNode = node.getNextNode(m);
                        if (!closedSet.contains(nextNode)) {
                            closedSet.add(nextNode);
                            frontier.add(nextNode);
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean verify_(int team, boolean complete) {
        if (team == PLAYER_ANY)
            return verify_(PLAYER1, complete) && verify_(PLAYER2, complete);
        FFTNode initialNode = FFTManager.getInitialNode();
        closedMap.clear();
        failed = false;
        // Check if win or draw is even possible
        if (!isValid(team))
            return false;

        return forkJoinPool.invoke(new VerificationTask(initialNode, team, complete));
    }


    private boolean verifyGGP(int team, boolean complete) {
        if (team == PLAYER_ANY)
            return verifyGGP(PLAYER1, complete) && verifyGGP(PLAYER2, complete);
        MachineState initial_ms = GGPManager.getInitialState();
        closedMapGGP.clear();
        failed = false;
        if (!isValid(team))
            return false;

        return forkJoinPool.invoke(new GGPVerificationTask(initial_ms, team, complete));
    }

    public Move apply(MachineState ms) throws MoveDefinitionException {
        return null;
    }

    // assumes that the FFT is strongly optimal
    public int minimize() {
        int ruleSize, precSize;
        int i = 0;
        do {
            ruleSize = getAmountOfRules();
            precSize = getAmountOfPreconditions();
            i++;
            System.out.println("Minimizing, iteration no. " + i);
            minimizeRules();
        } while (MINIMIZE_ITERATIVELY &&
                (ruleSize != getAmountOfRules() || precSize != getAmountOfPreconditions()));
        return i;
    }

    private void minimizeRules() {
        ArrayList<Rule> rulesCopy = new ArrayList<>(getRules());
        int removed = 0;
        for (int i = 0; i < rulesCopy.size(); i++) {
            Rule r = rulesCopy.get(i);
            if (r.isLocked())
                continue;
            if ((i - removed) >= size()) // remaining rules are removed
                break;
            if (DETAILED_DEBUG || NAIVE_RULE_GENERATION)
                System.out.println("Remaining amount of rules: " + getAmountOfRules());
            if (!getRules().get(i - removed).equals(r)) {
                System.out.println("This rule has already been deleted");
                removed++;
                continue;
            }

            removeRule(i - removed);
            removed++;

            if (!verify(AUTOGEN_TEAM, true)) {
                removed--;
                addRule(r, i - removed);
                if (MINIMIZE_PRECONDITIONS)
                    simplifyRule(r);
                if (LIFT_WHEN_MINIMIZING && !(r instanceof PredRule)) {
                    PropRule propRule = (PropRule) r;
                    liftRule(propRule);
                }
            }
        }
    }

    // if simplifying last rule, simplifying can not effect other rules, so it's simpler
    private void simplifyRule(Rule r) {
        if (DETAILED_DEBUG) System.out.println("SIMPLIFYING RULE: " + r);
        LiteralSet precons = new LiteralSet(r.getPreconditions());

        boolean simplified = false;
        for (Literal l : precons) {
            if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO REMOVE: " + l.getName());
            removePrecondition(r, l);

            if (!verify(AUTOGEN_TEAM, true)) {
                if (DETAILED_DEBUG) System.out.println("FAILED TO REMOVE: " + l.getName());
                addPrecondition(r, l);
            } else {
                simplified = true;
                if (DETAILED_DEBUG) System.out.println("REMOVING PRECONDITION: " + l.getName());
            }
            if (DETAILED_DEBUG) System.out.println("SUCCESS, RULE IS NOW: " + r);
        }
        if (simplified)
            removeDeadRules(r);
    }

    private Rule liftRule(PropRule r) {
        if (DETAILED_DEBUG) System.out.println("ATTEMPTING TO LIFT RULE");
        // attempt to lift propositional variables one at a time, sorted by the most occurring variable 1st
        for (int prop : r.getSortedProps()) {
            PredRule pr = r.liftAll(prop);
            if (pr == null) // inconsistent
                continue;
            replaceRule(r, pr);
            if (DETAILED_DEBUG) System.out.println("VERIFYING LIFTED RULE: " + pr);
            // we do not want to lift if the lifted rule does not apply to more states
            if (verify(AUTOGEN_TEAM, true)) {
                if (DETAILED_DEBUG) System.out.println("RULE SUCCESSFULLY LIFTED TO: " + pr);
                return pr;
            } else {
                replaceRule(pr, r);
            }
        }
        if (DETAILED_DEBUG) System.out.println("FAILED TO LIFT RULE");
        return r;
    }

    public class VerificationTask extends RecursiveTask<Boolean> {
        int team, opponent;
        FFTNode node;
        boolean complete;
        LinkedList<VerificationTask> forks;

        VerificationTask(FFTNode node, int team, boolean complete) {
            this.node = node;
            this.team = team;
            this.complete = complete;
            this.opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
            forks = new LinkedList<>();

        }

        @Override
        protected Boolean compute() {
            if (failed)
                return false;
            if (node.isTerminal()) {
                if (node.getWinner() == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    failed = true;
                    return false;
                }
            } else if (team != node.getTurn()) {
                for (FFTNode child : node.getChildren()) {
                    addTask(child);
                }
            } else {
                Set<FFTMove> moves = apply(node).getMoves();
                List<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                // If move is null, check that all possible (random) moves are ok
                if (moves.isEmpty()) {
                    for (FFTMove m : node.getLegalMoves()) {
                        if (optimalMoves.contains(m)) {
                            addTask(node.getNextNode(m));
                        } else if (complete) {
                            failingPoint = node.clone();
                            failed = true;
                            return false;
                        }
                    }
                } else {
                    for (FFTMove m : moves) {
                        if (!optimalMoves.contains(m)) {
                            failingPoint = node.clone();
                            failed = true;
                            return false;
                        }
                        addTask(node.getNextNode(m));
                    }
                }
            }
            boolean result = true;
            for (VerificationTask t : forks) {
                result = result && t.join();
            }
            return result;
        }

        private void addTask(FFTNode n) {
            if (!closedMap.containsKey(n)) {
                closedMap.put(n, true);
                VerificationTask t = new VerificationTask(n, team, complete);
                forks.add(t);
                t.fork();
            }
        }
    }

    public class GGPVerificationTask extends RecursiveTask<Boolean> {

        int team, opponent;
        MachineState ms;
        List<RecursiveTask<Boolean>> forks;
        boolean complete;

        GGPVerificationTask(MachineState ms, int team, boolean complete) {
            this.ms = ms;
            this.team = team;
            this.complete = complete;

            forks = new LinkedList<>();
            this.opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;

        }

        @Override
        protected Boolean compute() {
            if (failed)
                return false;
            Role currRole = GGPManager.getRole(ms);
            int currTurn = GGPManager.roleToPlayer(currRole);
            try {
                if (GGPManager.isTerminal(ms)) {
                    List<Integer> winners = GGPManager.getPlayerWinners(ms);
                    if (winners == null) {// should not happen
                        failed = true;
                        return false;
                    }

                    if (winners.size() == 1 && winners.get(0) == opponent) {
                        // Should not hit this given initial check
                        System.out.println("No chance of winning vs. perfect player");
                        failed = true;
                        return false;
                    }
                } else if (team != currTurn) {
                    for (MachineState child : GGPManager.getNextStates(ms))
                        addTask(child);
                } else {
                    Move move = apply(ms);
                    Set<Move> optimalMoves = Database.optimalMoves(ms);
                    // If move is null, check that all possible (random) moves are ok
                    if (move == null) {
                        for (Move m : GGPManager.getLegalMoves(ms, currRole)) {
                            if (optimalMoves.contains(m)) {
                                addTask(GGPManager.getNextState(ms, m));
                            } else if (complete) {
                                failed = true;
                                return false;
                            }
                        }
                    } else if (!optimalMoves.contains(move)) {
                        failed = true;
                        return false;
                    } else {
                        addTask(GGPManager.getNextState(ms, move));
                    }
                }
            } catch (GoalDefinitionException | MoveDefinitionException | TransitionDefinitionException e) {
                e.printStackTrace();
            }
            boolean result = true;
            for (RecursiveTask<Boolean> t : forks) {
                result = result && t.join();
            }
            return result;
        }

        private void addTask(MachineState ms) {
            if (!closedMapGGP.containsKey(ms)) {
                closedMapGGP.put(ms, true);
                GGPVerificationTask t = new GGPVerificationTask(ms, team, complete);
                forks.add(t);
                t.fork();
            }
        }

    }
}

