package fftlib;

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
    public ArrayList<RuleGroup> ruleGroups;
    public FFTNodeAndMove failingPoint = null;

    // For concurrency purposes
    private static ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    private ConcurrentHashMap<FFTNode, Boolean> closedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<MachineState, Boolean> closedMapGGP = new ConcurrentHashMap<>();
    private boolean failed; // failed is set to true if verification failed

    public HashMap<FFTNode, NodeMapping> singleStrategy; // used for alternative version of program

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

    public void addRuleGroup(RuleGroup ruleGroup) {
        ruleGroups.add(ruleGroup);
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RuleGroup rg : ruleGroups) {
            sb.append(rg.name).append(":\n");
            for (Rule r : rg.rules)
                sb.append(r).append("\n");
        }
        return sb.toString();
    }

    public FFTMove apply(FFTNode node) {
        State state = node.convert();
        for (RuleGroup rg : ruleGroups) {
            for (Rule r : rg.rules) {
                Action a = r.apply(state);
                if (a != null)
                    return a.convert();
            }
        }
        return null;
    }

    public int minimize(int team, boolean minimize_precons) { // Returns amount of iterations
        if (!verify(team, true)) {
            System.out.println("FFT is not a winning strategy, so it can not be minimized");
            return -1;
        }

        int ruleSize = getAmountOfRules();
        int precSize = getAmountOfPreconditions();

        minimizeRules(team);
        if (!MINIMIZE_RULE_BY_RULE && minimize_precons) {
            minimizePreconditions(team);
        }

        int minRuleSize = getAmountOfRules();
        int minPrecSize = getAmountOfPreconditions();

        int i = 0;
        while (ruleSize != minRuleSize || precSize != minPrecSize) {
            ruleSize = minRuleSize;
            precSize = minPrecSize;

            minimizeRules(team);
            if (!MINIMIZE_RULE_BY_RULE && minimize_precons)
                minimizePreconditions(team);

            minRuleSize = getAmountOfRules();
            minPrecSize = getAmountOfPreconditions();
            i++;
        }
        return i;
    }

    private ArrayList<Rule> minimizeRules(int team) {
        ArrayList<Rule> redundantRules = new ArrayList<>();

        for (RuleGroup rg : ruleGroups) {
            if (rg.locked) // don't minimize if rulegroup is locked
                continue;
            ListIterator<Rule> itr = rg.rules.listIterator();
            while(itr.hasNext()) {
                Rule r = itr.next();
                itr.remove();
                if (verify(team, true)) {
                    redundantRules.add(r);
                }
                else {
                    itr.add(r);
                    if (MINIMIZE_RULE_BY_RULE)
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
            ArrayList<Literal> literals = new ArrayList<>();
            for (Literal l : r.getPreconditions())
                literals.add(l.clone());

            for (Literal l : literals) {
                r.removePrecondition(l);
                if (!verify(team, true))
                    r.addPrecondition(l);
            }
        }
    }

    public boolean verify(int team, boolean complete) {
        if (!Config.ENABLE_GGP && SINGLE_THREAD) {
            return verifySingleThread(team, complete);
        }
        else {
            return (Config.ENABLE_GGP) ? verifyGGP(team, complete) : verify_(team, complete);
        }
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
            if (FFTManager.logic.gameOver(node)) {
                if (FFTManager.logic.getWinner(node) == opponent) {
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
                FFTMove move = apply(node);
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
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
                } else if (!optimalMoves.contains(move)) {
                    failingPoint = new FFTNodeAndMove(node, move, false);
                    return false;
                } else { // move is not null, expand on node from that move
                    FFTNode nextNode = node.getNextNode(move);
                    if (!closedSet.contains(nextNode)) {
                        closedSet.add(nextNode);
                        frontier.add(nextNode);
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
        List<RecursiveTask<Boolean>> forks;
        boolean complete;

        VerificationTask(FFTNode node, int team, boolean complete) {
            this.node = node;
            this.team = team;
            this.complete = complete;
            forks = new LinkedList<>();
            this.opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;

        }

        @Override
        protected Boolean compute() {
            if (failed)
                return false;
            if (FFTManager.logic.gameOver(node)) {
                if (FFTManager.logic.getWinner(node) == opponent) {
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
                FFTMove move = apply(node);
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(node);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    for (FFTMove m : node.getLegalMoves()) {
                        if (optimalMoves.contains(m)) {
                            addTask(node.getNextNode(m));
                        } else if (complete) {
                            failingPoint = new FFTNodeAndMove(node, m, true);
                            failed = true;
                            return false;
                        }
                    }
                } else if (!optimalMoves.contains(move)) {
                    failingPoint = new FFTNodeAndMove(node, move, false);
                    failed = true;
                    return false;
                } else {
                    addTask(node.getNextNode(move));
                }
            }
            boolean result = true;
            for (RecursiveTask<Boolean> t : forks) {
                result = result && t.join();
            }
            return result;
        }

        private void addTask(FFTNode node) {
            if (!closedMap.containsKey(node)) {
                closedMap.put(node, true);
                VerificationTask t = new VerificationTask(node, team, complete);
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

