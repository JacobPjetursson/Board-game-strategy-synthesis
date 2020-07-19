package fftlib.logic;

import fftlib.FFTManager;
import fftlib.GGPAutogen.Database;
import fftlib.GGPAutogen.GGPManager;
import fftlib.auxiliary.InvertedList;
import fftlib.auxiliary.RuleList;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.FFTNodeAndMove;
import fftlib.game.FFTSolution;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.*;
import misc.Config;
import org.ggp.base.util.gdl.grammar.GdlSentence;
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
    public String name;
    // used for a visual representation and for locking (preventing) a group of rules of being minimized
    public ArrayList<RuleEntity> ruleEntities;
    // used for the efficient computation of looking up rules that apply
    // rulelist is only initialized after autogeneration, to avoid a bunch of unnecessary sorts
    private RuleList ruleList;
    private final InvertedList invertedList = new InvertedList(false);


    public FFTNodeAndMove failingPoint = null;

    private boolean stronglyOptimal;

    // For concurrency purposes
    private static ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    private ConcurrentHashMap<FFTNode, Boolean> closedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<MachineState, Boolean> closedMapGGP = new ConcurrentHashMap<>();
    private boolean failed; // failed is set to true if verification failed

    public FFT(String name) {
        this.name = name;
        ruleEntities = new ArrayList<>();
    }

    public FFT(FFT duplicate) {
        this.name = duplicate.name;
        ruleEntities = new ArrayList<>();
        for (RuleEntity re : duplicate.ruleEntities) {
            ruleEntities.add(re.clone());
        }
        this.failingPoint = duplicate.failingPoint;
        if (ruleList != null)
            this.ruleList = new RuleList(duplicate.ruleList);
    }

    public void setStronglyOptimal(boolean stronglyOptimal) {
        this.stronglyOptimal = stronglyOptimal;
    }

    public boolean isStronglyOptimal() {
        return stronglyOptimal;
    }

    public void initializeRuleList() {
        ruleList = new RuleList();
        for (Rule r : getRules())
            ruleList.add(r); // can't be replaced by addAll since add() has been overridden
        ruleList.sort();
    }

    public int getAmountOfRules() {
        int size = 0;
        for (RuleEntity re : ruleEntities)
            size += re.size();
        return size;
    }

    public ArrayList<Rule> getRules() {
        ArrayList<Rule> rules = new ArrayList<>();
        for (RuleEntity re : ruleEntities) {
            if (re instanceof  Rule) {
                rules.add((Rule)re);

            }
            else {
                RuleGroup rg = (RuleGroup) re;
                rules.addAll(rg.rules);
            }
        }
        return rules;
    }

    public RuleList getRuleList() {
        return ruleList;
    }

    public void addRuleGroup(RuleGroup ruleGroup) {
        ruleEntities.add(ruleGroup);
    }

    public void append(Rule r) {
        r.setRuleIndex(getAmountOfRules());
        ruleEntities.add(r);
        // add to sorted list of rules if initialized
        if (USE_APPLY_OPT && ruleList != null)
            ruleList.sortedAdd(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            // only works when no predicate rules and no symmetry
            PropRule propRule = (PropRule) r;
            invertedList.add(propRule);
        }
    }

    public void removePrecondition(Rule r, Literal l) {
        if (USE_APPLY_OPT && ruleList != null) {
            ruleList.sortedRemove(r);
            r.removePrecondition(l);
            ruleList.sortedAdd(r);
        } else if (USE_INVERTED_LIST_RULES_OPT) {
            // no predicate rules or symmetry
            PropRule propRule = (PropRule) r;
            invertedList.remove(propRule);
            propRule.removePrecondition(l);
            invertedList.add(propRule);
        } else {
            r.removePrecondition(l);
        }
    }

    public void addPrecondition(Rule r, Literal l) {
        if (ruleList != null && USE_APPLY_OPT) {
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

    public void removeRule(Rule r, int entityIdx) {
        removeRule(r, entityIdx, -1);
    }

    // faster since we don't have to search for rule
    public void removeRule(Rule r, int entityIdx, int rgIdx) {
        if (rgIdx == -1)
            ruleEntities.remove(entityIdx);
        else {
            RuleGroup rg = (RuleGroup) ruleEntities.get(entityIdx);
            rg.rules.remove(rgIdx);
        }

        if (USE_APPLY_OPT && ruleList != null)
            ruleList.sortedRemove(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            PropRule propRule = (PropRule) r;
            invertedList.remove(propRule);
        }
    }

    public void addRule(Rule r, int index) {
        addRule(r, index, -1);
    }

    public void addRule(Rule r, int entIdx, int rgIdx) {
        if (rgIdx == -1) {
            ruleEntities.add(entIdx, r);
        } else {
            RuleGroup rg = (RuleGroup) ruleEntities.get(entIdx);
            rg.rules.add(rgIdx, r);
        }

        if (USE_APPLY_OPT && ruleList != null)
            ruleList.sortedAdd(r);
        else if (USE_INVERTED_LIST_RULES_OPT) {
            PropRule propRule = (PropRule) r;
            invertedList.add(propRule);
        }
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
        for (RuleEntity re : ruleEntities)
            precSize += re.getAmountOfPreconditions();
        return precSize;
    }

    public Rule getLastRule() {
        RuleEntity re = ruleEntities.get(ruleEntities.size()-1);
        if (re instanceof Rule)
            return (Rule) re;
        RuleGroup rg = (RuleGroup) re;
        return rg.rules.get(rg.size()-1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RuleEntity re : ruleEntities) {
            sb.append(re).append("\n");
        }
        return sb.toString();
    }

    public HashSet<FFTMove> apply_slow(FFTNode node, boolean safe) {
        HashSet<FFTMove> moves = new HashSet<>();
        for (RuleEntity re : ruleEntities) {
            moves = re.apply(node, safe);
            if (!moves.isEmpty())
                return moves;
        }
        return moves;
    }

    public HashSet<FFTMove> apply(FFTNode node, boolean safe) {
        if (USE_APPLY_OPT && ruleList != null)
            return ruleList.apply(node, safe);
        if (USE_INVERTED_LIST_RULES_OPT)
            return invertedList.apply(node, safe);
        return apply_slow(node, safe);
    }

    public HashSet<FFTMove> apply(FFTNode node) {
        return apply(node, false);
    }

    // for all rules r' with a higher index than r.index, check if r is subset of r'
    // if yes, then r' is dead. Maybe we can use containsAll
    // Alternative: We can use r.apply(r') to determine the same thing.
    // Perhaps we can even use the invertedList/ruleList stuff
    public void removeDeadRules(Rule rule) {
        ArrayList<RuleEntity> rulesCopy = new ArrayList<>(ruleEntities);
        int removedEnts = 0;
        for (int entIdx = 0; entIdx < rulesCopy.size(); entIdx++) {
            RuleEntity re = rulesCopy.get(entIdx);
            if (re instanceof RuleGroup) {
                RuleGroup rg = (RuleGroup) re;
                ArrayList<Rule> ruleGroupCopy = new ArrayList<>(rg.rules);
                int removedRgs = 0;
                for (int rgIdx = 0; rgIdx < ruleGroupCopy.size(); rgIdx++) {
                    Rule r = ruleGroupCopy.get(rgIdx);
                    if (isDead(rule, r)) {
                        removeRule(r, entIdx - removedEnts, rgIdx - removedRgs);
                        removedRgs++;
                    }
                }
            }
            else {
                Rule r = (Rule) re;
                if (isDead(rule, r)) {

                    removeRule(r, entIdx - removedEnts);
                    removedEnts++;
                }
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

    // TODO - this function doesn't account for inverted lists
    public void replaceRule(Rule oldRule, Rule newRule) {
        ArrayList<RuleEntity> rulesCopy = new ArrayList<>(ruleEntities);
        for (int i = 0; i < rulesCopy.size(); i++) {
            RuleEntity re = rulesCopy.get(i);
            if (re instanceof RuleGroup) {
                RuleGroup rg = (RuleGroup) re;
                ArrayList<Rule> rgCopy = new ArrayList<>(rg.rules);
                for (int j = 0; j < rgCopy.size(); j++) {
                    Rule r = rgCopy.get(j);
                    if (oldRule == r) {
                        removeRule(oldRule, i, j);
                        addRule(newRule, i, j);
                        return;
                    }
                }
            } else {
                Rule r = (Rule) ruleEntities.get(i);
                if (oldRule == r) {
                    removeRule(oldRule, i);
                    addRule(newRule, i);
                    return;
                }

            }
        }
    }

    public boolean verify(int team) {
        return verify(team, stronglyOptimal);
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
        FFTNode initialNode = FFTManager.initialFFTNode;
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
                HashSet<FFTMove> moves = apply(node);
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
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
                            failingPoint = new FFTNodeAndMove(node, m, true);
                            return false;
                        }
                    }
                } else {
                    for (FFTMove m : moves) {
                        if (!optimalMoves.contains(m)) {
                            failingPoint = new FFTNodeAndMove(node, m, false);
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
        FFTNode initialNode = FFTManager.initialFFTNode;
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
        /*
        for (RuleEntity re : ruleEntities) {
            Move move = re.apply(ms);
            for (Rule rule : ruleGroup.rules) {
                    Move move = rule.apply(ms);
                    if (move != null) {
                        return move;
                    }
            }
        }

         */
        return null;
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
                HashSet<FFTMove> moves = apply(node);
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                // If move is null, check that all possible (random) moves are ok
                if (moves.isEmpty()) {
                    for (FFTMove m : node.getLegalMoves()) {
                        if (optimalMoves.contains(m)) {
                            addTask(node.getNextNode(m));
                        } else if (complete) {
                            failingPoint = new FFTNodeAndMove(node, m, true);
                            failed = true;
                            return false;
                        }
                    }
                } else {
                    for (FFTMove m : moves) {
                        if (!optimalMoves.contains(m)) {
                            failingPoint = new FFTNodeAndMove(node, m, false);
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

