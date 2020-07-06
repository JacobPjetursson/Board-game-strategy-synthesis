package fftlib.logic;

import fftlib.FFTManager;
import fftlib.game.FFTSolution;
import fftlib.GGPAutogen.Database;
import fftlib.GGPAutogen.GGPManager;
import fftlib.game.*;
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
    public ArrayList<RuleGroup> ruleGroups;
    // used for the efficient computation of looking up rules that apply
    // rulelist is only initialized after autogeneration, to avoid a bunch of unnecessary sorts
    private RuleList ruleList;


    public FFTNodeAndMove failingPoint = null;

    // For concurrency purposes
    private static ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    private ConcurrentHashMap<FFTNode, Boolean> closedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<MachineState, Boolean> closedMapGGP = new ConcurrentHashMap<>();
    private boolean failed; // failed is set to true if verification failed

    public FFT(String name) {
        this.name = name;
        ruleGroups = new ArrayList<>();
    }

    public FFT(FFT duplicate) {
        this.name = duplicate.name;
        ruleGroups = new ArrayList<>();
        for (RuleGroup rg : duplicate.ruleGroups) {
            ruleGroups.add(new RuleGroup(rg));
        }
        this.failingPoint = duplicate.failingPoint;
        this.ruleList = new RuleList(duplicate.ruleList);
    }

    public void initializeRuleList() {
        ruleList = new RuleList();
        for (Rule r : getRules())
            ruleList.add(r); // can't be replaced by addAll since add() has been overridden
        ruleList.sort();
    }

    public int size() {
        int size = 0;
        for (RuleGroup rg : ruleGroups)
            size += rg.rules.size();
        return size;
    }

    public ArrayList<Rule> getRules() {
        ArrayList<Rule> rules = new ArrayList<>();
        for (RuleGroup rg : ruleGroups)
            rules.addAll(rg.rules);
        return rules;
    }

    public RuleList getRuleList() {
        return ruleList;
    }

    public void addRuleGroup(RuleGroup ruleGroup) {
        ruleGroups.add(ruleGroup);
    }

    public void append(Rule r) {
        r.setRuleIndex(size());
        // add to last rulegroup
        ruleGroups.get(ruleGroups.size()-1).rules.add(r);
        // add to sorted list of rules if initialized
        if (ruleList != null)
            ruleList.sortedAdd(r);
    }

    public void remove(Rule r) {
        for (RuleGroup rg : ruleGroups) {
            rg.rules.remove(r);
        }
        ruleList.sortedRemove(r);
    }

    public void removePrecondition(Rule r, Literal l) {
        if (ruleList == null)
            r.removePrecondition(l);
        else {
            ruleList.sortedRemove(r);
            r.removePrecondition(l);
            ruleList.sortedAdd(r);
        }
    }

    public void addPrecondition(Rule r, Literal l) {
        if (ruleList == null)
            r.addPrecondition(l);
        else {
            ruleList.sortedRemove(r);
            r.addPrecondition(l);
            ruleList.sortedAdd(r);
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

    public int getAmountOfRules() {
        int ruleSize = 0;
        for (RuleGroup rg : ruleGroups) {
            ruleSize += rg.rules.size();
        }
        return ruleSize;
    }

    public int getAmountOfPreconditions() {
        int precSize = 0;
        for (RuleGroup rg : ruleGroups) {
            precSize += rg.getAmountOfPreconditions();
        }
        return precSize;
    }

    public Rule getLastRule() {
        RuleGroup last = ruleGroups.get(ruleGroups.size() - 1);
        return last.rules.get(last.rules.size() - 1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RuleGroup rg : ruleGroups) {
            sb.append(rg.name).append(":\n");
            for (Rule r : rg.rules)
                sb.append(r).append("\n");
        }
        return sb.toString();
    }

    // optimized way of finding the correct move using the sorted ruleList
    public HashSet<FFTMove> apply_optim(FFTNode node) {
        return ruleList.apply(node);
    }

    public HashSet<FFTMove> apply_slow(FFTNode node) {
        HashSet<FFTMove> moves = new HashSet<>();
        for (RuleGroup rg : ruleGroups) {
            for (Rule r : rg.rules) {
                moves = r.apply(node);
                if (!moves.isEmpty())
                    return moves;
            }
        }
        return moves;
    }

    public HashSet<FFTMove> apply(FFTNode node) {
        if (USE_APPLY_OPT && ruleList != null)
            return apply_optim(node);
        return apply_slow(node);
    }

    public int minimize(int team, boolean minimize_precons) { // Returns amount of iterations
        if (!verify(team, true)) {
            System.out.println("FFT is not a winning strategy, so it can not be minimized");
            return -1;
        }

        int ruleSize, precSize;
        int i = 0;
        do {
            ruleSize = getAmountOfRules();
            precSize = getAmountOfPreconditions();
            i++;
            System.out.println("Minimizing, iteration no. " + i);
            minimizeRules(team, minimize_precons);
            if (!MINIMIZE_RULE_BY_RULE && minimize_precons) {
                if (DETAILED_DEBUG) System.out.println("Minimizing preconditions");
                minimizePreconditions(team);
            }
        } while(ruleSize != getAmountOfRules() || precSize != getAmountOfPreconditions());
        return i;
    }

    private ArrayList<Rule> minimizeRules(int team, boolean minimize_precons) {
        ArrayList<Rule> redundantRules = new ArrayList<>();

        for (RuleGroup rg : ruleGroups) {
            if (rg.locked) // don't minimize if rulegroup is locked
                continue;
            ListIterator<Rule> itr = rg.rules.listIterator();
            while(itr.hasNext()) {
                if (DETAILED_DEBUG) {
                    System.out.println("Remaining amount of rules: " + getAmountOfRules());
                }
                Rule r = itr.next();
                itr.remove();
                if (ruleList != null)
                    ruleList.sortedRemove(r);

                if (verify(team, true)) {
                    redundantRules.add(r);
                }
                else {
                    itr.add(r);
                    if (ruleList != null)
                        ruleList.sortedAdd(r);

                    if (MINIMIZE_RULE_BY_RULE && minimize_precons)
                        minimizePreconditions(r, team);
                }
            }
        }
        return redundantRules;
    }

    private void minimizePreconditions(int team) {
        for (RuleGroup rg : ruleGroups) {
            if (rg.locked) continue; // don't minimize if rg is locked
            for(Rule r : rg.rules) {
                minimizePreconditions(r, team);
            }
        }
    }

    // TODO - fix this ugly beast, either by making common type for precons and sentences or by typecasting
    private void minimizePreconditions(Rule r, int team) {
        if (Config.ENABLE_GGP) {
            Set<GdlSentence> sentences = new HashSet<>();
            for (GdlSentence s : r.sentences)
                sentences.add(s.clone());

            for (GdlSentence s : sentences) {
                r.removePrecondition(s);
                if (!verify(team, true))
                    r.addPrecondition(s);
            }
        } else {
            LiteralSet precons = new LiteralSet(r.getPreconditions());

            for (Literal l : precons) {
                removePrecondition(r, l);
                if (!verify(team, true))
                    addPrecondition(r, l);
            }
            if (LIFT_WHEN_MINIMIZING && !(r instanceof PredRule))
                liftRule(r, team);
        }
    }

    private void liftRule(Rule r, int team) {
        if (DETAILED_DEBUG) System.out.println("Attempting to lift rule: " + r);
        for (int prop : r.getSortedProps()) {
            PredRule pr = r.liftAll(prop);
            if (pr == null) // inconsistent
                continue;
            replaceRule(r, pr);
            if (verify(team, true)) {
                if (DETAILED_DEBUG) System.out.println("Successfully lifted rule to: " + pr);
                return;
            } else {
                replaceRule(pr, r);
            }
        }
    }

    public void replaceRule(Rule oldRule, Rule newRule) {
        int rgIdx = -1;
        int rIdx = -1;
        for (int i = 0; i < ruleGroups.size(); i++) {
            RuleGroup rg = ruleGroups.get(i);
            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                if (oldRule == r) {
                    rgIdx = i;
                    rIdx = j;
                }

            }
        }
        if (rgIdx == -1) {
            if (DETAILED_DEBUG) System.out.println("Error: Failed to find oldRule when replacing");
            return;
        }
        ruleGroups.get(rgIdx).rules.set(rIdx, newRule);

        if (ruleList != null) {
            ruleList.sortedRemove(oldRule);
            ruleList.sortedAdd(newRule);
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
        for (RuleGroup ruleGroup : ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                    Move move = rule.apply(ms);
                    if (move != null) {
                        return move;
                    }
            }
        }
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

