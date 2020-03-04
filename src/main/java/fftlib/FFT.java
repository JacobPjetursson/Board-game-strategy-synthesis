package fftlib;

import fftlib.GGPAutogen.Database;
import fftlib.GGPAutogen.GGPManager;
import fftlib.GGPAutogen.GGPMapping;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.game.FFTStateAndMove;
import fftlib.game.FFTStateMapping;
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

import static misc.Globals.*;


public class FFT {
    public String name;
    public ArrayList<RuleGroup> ruleGroups;
    public FFTStateAndMove failingPoint = null;

    // For concurrency purposes
    private int nThreads = Runtime.getRuntime().availableProcessors();
    private ForkJoinPool forkJoinPool = new ForkJoinPool(nThreads);
    private static HashSet<FFTState> closedSet = new HashSet<>();
    private static ConcurrentHashMap<FFTState, Boolean> closedMap = new ConcurrentHashMap<>();
    private static boolean failed; // failed true if verification failed

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

    public void addRuleGroup(RuleGroup ruleGroup) {
        ruleGroups.add(ruleGroup);
    }

    public boolean verify(int team, boolean complete) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        return (Config.ENABLE_GGP) ? verifyGGP(team, complete, null) : verify(team, complete, null);
    }

    public boolean verify(int team, boolean complete, HashMap<FFTState, FFTStateMapping> strategy) {
        if (team == PLAYER_ANY)
            return verify(PLAYER1, complete, strategy) && verify(PLAYER2, complete, strategy);
        FFTState initialState = FFTManager.initialFFTState;
        closedMap.clear();
        failed = false;
        // Check if win or draw is even possible
        int winner = FFTManager.db.queryState(initialState).getWinner();
        if (team == PLAYER1 && winner == PLAYER2) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && winner == PLAYER1) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }

        return forkJoinPool.invoke(new VerificationTask(initialState, team, complete, strategy));
    }


    private boolean verifyGGP(int team, boolean complete, HashMap<MachineState, GGPMapping> strategy) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        if (team == PLAYER_ANY)
            return verifyGGP(PLAYER1, complete, strategy) && verifyGGP(PLAYER2, complete, strategy);
        MachineState initialState = GGPManager.getInitialState();
        LinkedList<MachineState> frontier = new LinkedList<>();
        HashSet<MachineState> closedSet = new HashSet<>();
        frontier.add(initialState);
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        // Check if win or draw is even possible
        int winner = Database.queryState(initialState).getWinner();
        if (team == PLAYER1 && winner == PLAYER2) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && winner == PLAYER1) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }

        while (!frontier.isEmpty()) {
            MachineState ms = frontier.pop();
            Role currRole = GGPManager.getRole(ms);
            int currTurn = GGPManager.roleToPlayer(currRole);
            if (GGPManager.isTerminal(ms)) {
                List<Integer> winners = GGPManager.getPlayerWinners(ms);
                if (winners == null) {// should not happen
                    return false;
                }

                if (winners.size() == 1 && winners.get(0) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            } else if (team != currTurn) {
                for (MachineState child : GGPManager.getNextStates(ms))
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                Move move = apply(ms);
                Set<Move> nonLosingMoves = Database.nonLosingMoves(ms);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {

                    if (!complete && strategy != null) { // only expand on move from strategy
                        GGPMapping mapping = strategy.get(ms);
                        if (mapping == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        MachineState nextState = GGPManager.getNextState(ms, mapping.getMove());
                        if (!closedSet.contains(nextState)) {
                            closedSet.add(nextState);
                            frontier.add(nextState);
                        }
                        continue;
                    }
                    for (Move m : GGPManager.getLegalMoves(ms, currRole)) {
                        if (nonLosingMoves.contains(m)) {
                            MachineState nextState = GGPManager.getNextState(ms, m);
                            if (!closedSet.contains(nextState)) {
                                closedSet.add(nextState);
                                frontier.add(nextState);
                            }
                        } else if (complete) {
                            return false;
                        }
                    }
                } else if (!nonLosingMoves.contains(move)) {
                    return false;
                } else {
                    if (!complete && strategy != null) { // check that move is same as from strategy
                        GGPMapping mapping = strategy.get(ms);
                        if (mapping == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        if (!mapping.getMove().equals(move)) { // DOESN'T WORK BECAUSE OF TRANSFORMATIONS!
                            return false;
                        }

                    }
                    MachineState nextState = GGPManager.getNextState(ms, move);
                    if (!closedSet.contains(nextState)) {
                        closedSet.add(nextState);
                        frontier.add(nextState);
                    }
                }
            }
        }
        return true;
    }


    public Move apply(MachineState state) throws MoveDefinitionException {
        for (RuleGroup ruleGroup : ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                    Move move = rule.apply(state);
                    if (move != null) {
                        return move;
                    }
            }
        }
        return null;
    }

    public FFTMove apply(FFTState state) {
        for (RuleGroup rg : ruleGroups) {
            for (Rule r : rg.rules) {
                FFTMove m = r.apply(state);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
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

    public int minimize(int perspective, boolean minimize_precons) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException { // Returns amount of iterations
        if (!verify(perspective, true)) {
            System.out.println("FFT is not a winning strategy, so it can not be minimized");
            return -1;
        }

        int ruleSize = getAmountOfRules();
        int precSize = getAmountOfPreconditions();

        minimizeRules(perspective);
        if (minimize_precons)
            minimizePreconditions(perspective);


        int minRuleSize = getAmountOfRules();
        int minPrecSize = getAmountOfPreconditions();

        int i = 0;
        while (ruleSize != minRuleSize || precSize != minPrecSize) {
            ruleSize = minRuleSize;
            precSize = minPrecSize;

            minimizeRules(perspective);
            if (minimize_precons)
                minimizePreconditions(perspective);

            minRuleSize = getAmountOfRules();
            minPrecSize = getAmountOfPreconditions();
            i++;
        }
        return i;
    }

    private ArrayList<Rule> minimizeRules(int team) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        ArrayList<Rule> redundantRules = new ArrayList<>();

        for (RuleGroup rg : ruleGroups) {
            ListIterator<Rule> itr = rg.rules.listIterator();
            while(itr.hasNext()) {
                Rule r = itr.next();
                if (r.multiRule) continue; // TODO - support multirule when minimizing?
                itr.remove();
                if (verify(team, true)) {
                    redundantRules.add(r);
                }
                else
                    itr.add(r);
            }
        }
        return redundantRules;
    }
    // TODO - fix this ugly beast, either by making common type for precons and sentences or by typecasting
    private void minimizePreconditions(int team) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
        for (RuleGroup rg : ruleGroups) {
            for(Rule r : rg.rules) {
                if (r.multiRule) continue; // TODO - support multirule when minimizing?
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
                    for (Literal l : r.preconditions.literals)
                        literals.add(l.clone());

                    for (Literal l : literals) {
                        r.removePrecondition(l);
                        if (!verify(team, true))
                            r.addPrecondition(l);
                    }
                }
            }
        }
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

    public class VerificationTask extends RecursiveTask<Boolean> {

        int team, opponent;
        FFTState state;

        boolean complete;
        HashMap<FFTState, FFTStateMapping> strategy;

        VerificationTask(FFTState state, int team, boolean complete, HashMap<FFTState, FFTStateMapping> strategy) {
            this.state = state;
            this.team = team;
            this.complete = complete;
            this.strategy = strategy;

            this.opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;

        }

        @Override
        protected Boolean compute() {
            if (failed)
                return false;
            List<RecursiveTask<Boolean>> forks = new LinkedList<>();
            if (FFTManager.logic.gameOver(state)) {
                if (FFTManager.logic.getWinner(state) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    failed = true;
                    return false;
                }
            } else if (team != state.getTurn()) {
                for (FFTState child : state.getChildren()) {
                    if (!closedMap.containsKey(child)) {
                        closedMap.put(child, true);
                        //addTask(new VerificationTask(child, team, complete, strategy));
                        VerificationTask t = new VerificationTask(child, team, complete, strategy);
                        forks.add(t);
                        t.fork();
                    }

                }
            } else {
                FFTMove move = apply(state);
                ArrayList<? extends FFTMove> nonLosingMoves = FFTManager.db.nonLosingMoves(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    if (!complete && strategy != null) { // only expand on move from strategy
                        FFTStateMapping info = strategy.get(state);
                        if (info == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            failed = true;
                            return false;
                        }
                        FFTState nextState = state.getNextState(info.getMove());
                        if (!closedMap.containsKey(nextState)) {
                            closedMap.put(nextState, true);
                            //addTask(new VerificationTask(nextState, team, complete, strategy));
                            VerificationTask t = new VerificationTask(nextState, team, complete, strategy);
                            forks.add(t);
                            t.fork();

                        }
                        boolean result = true;
                        for (RecursiveTask<Boolean> t : forks) {
                            result = result && t.join();
                        }
                        return result;
                    }

                    for (FFTMove m : state.getLegalMoves()) {
                        if (nonLosingMoves.contains(m)) {
                            FFTState nextState = state.getNextState(m);
                            if (!closedMap.containsKey(nextState)) { // mutual exclusion
                                closedMap.put(nextState, true);
                                //addTask(new VerificationTask(nextState, team, complete, strategy));
                                VerificationTask t = new VerificationTask(nextState, team, complete, strategy);
                                forks.add(t);
                                t.fork();
                            }
                        } else if (complete) {
                            failingPoint = new FFTStateAndMove(state, m, true);
                            failed = true;
                            return false;
                        }
                    }
                } else if (!nonLosingMoves.contains(move)) {
                    failingPoint = new FFTStateAndMove(state, move, false);
                    failed = true;
                    return false;
                } else {
                    if (!complete && strategy != null) { // check that move is same as from strategy
                        FFTStateMapping info = strategy.get(state);
                        if (info == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            failed = true;
                            return false;
                        }
                        if (!info.getMove().equals(move)) { // DOESN'T WORK BECAUSE OF TRANSFORMATIONS!
                            failed = true;
                            return false;
                        }

                    }
                    FFTState nextState = state.getNextState(move);
                    if (!closedMap.containsKey(nextState)) {
                        closedMap.put(nextState, true);
                        //addTask(new VerificationTask(nextState, team, complete, strategy));
                        VerificationTask t = new VerificationTask(nextState, team, complete, strategy);
                        forks.add(t);
                        t.fork();
                    }
                }
            }
            boolean result = true;
            for (RecursiveTask<Boolean> t : forks) {
                result = result && t.join();
            }
            return result;
        }
/*
        private void addTask(VerificationTask t) {
            forks.add(t);
            t.fork();
        }

 */
    }
}

