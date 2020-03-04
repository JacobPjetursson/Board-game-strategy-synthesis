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
    private static ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    private static ConcurrentHashMap<FFTState, Boolean> closedMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MachineState, Boolean> closedMapGGP = new ConcurrentHashMap<>();
    private static boolean failed; // failed is set to true if verification failed

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

    public boolean verify(int team, boolean complete) {
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


    private boolean verifyGGP(int team, boolean complete, HashMap<MachineState, GGPMapping> strategy) {
        if (team == PLAYER_ANY)
            return verifyGGP(PLAYER1, complete, strategy) && verifyGGP(PLAYER2, complete, strategy);
        MachineState initialState = GGPManager.getInitialState();
        closedMapGGP.clear();
        failed = false;
        // Check if win or draw is even possible
        int winner = Database.queryState(initialState).getWinner();
        if (team == PLAYER1 && winner == PLAYER2) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && winner == PLAYER1) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }
        return forkJoinPool.invoke(new GGPVerificationTask(initialState, team, complete, strategy));
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

    public int minimize(int perspective, boolean minimize_precons) { // Returns amount of iterations
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

    private ArrayList<Rule> minimizeRules(int team) {
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
    private void minimizePreconditions(int team) {
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
        List<RecursiveTask<Boolean>> forks;

        boolean complete;
        HashMap<FFTState, FFTStateMapping> strategy;

        VerificationTask(FFTState state, int team, boolean complete, HashMap<FFTState, FFTStateMapping> strategy) {
            this.state = state;
            this.team = team;
            this.complete = complete;
            this.strategy = strategy;
            forks = new LinkedList<>();
            this.opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;

        }

        @Override
        protected Boolean compute() {
            if (failed)
                return false;
            if (FFTManager.logic.gameOver(state)) {
                if (FFTManager.logic.getWinner(state) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    failed = true;
                    return false;
                }
            } else if (team != state.getTurn()) {
                for (FFTState child : state.getChildren()) {
                    addTask(child);
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
                        addTask(state.getNextState(info.getMove()));
                        boolean result = true;
                        for (RecursiveTask<Boolean> t : forks) {
                            result = result && t.join();
                        }
                        return result;
                    }

                    for (FFTMove m : state.getLegalMoves()) {
                        if (nonLosingMoves.contains(m)) {
                            addTask(state.getNextState(m));
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
                    addTask(state.getNextState(move));
                }
            }
            boolean result = true;
            for (RecursiveTask<Boolean> t : forks) {
                result = result && t.join();
            }
            return result;
        }

        private void addTask(FFTState state) {
            if (!closedMap.containsKey(state)) {
                closedMap.put(state, true);
                VerificationTask t = new VerificationTask(state, team, complete, strategy);
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
        HashMap<MachineState, GGPMapping> strategy;

        GGPVerificationTask(MachineState ms, int team, boolean complete, HashMap<MachineState, GGPMapping> strategy) {
            this.ms = ms;
            this.team = team;
            this.complete = complete;
            this.strategy = strategy;

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
                    Set<Move> nonLosingMoves = Database.nonLosingMoves(ms);
                    // If move is null, check that all possible (random) moves are ok
                    if (move == null) {
                        if (!complete && strategy != null) { // only expand on move from strategy
                            GGPMapping mapping = strategy.get(ms);
                            if (mapping == null) {
                                System.out.println("Given strategy is not a winning strategy");
                                failed = true;
                                return false;
                            }
                            addTask(GGPManager.getNextState(ms, mapping.getMove()));
                            boolean result = true;
                            for (RecursiveTask<Boolean> t : forks) {
                                result = result && t.join();
                            }
                            return result;
                        }
                        for (Move m : GGPManager.getLegalMoves(ms, currRole)) {
                            if (nonLosingMoves.contains(m)) {
                                addTask(GGPManager.getNextState(ms, m));
                            } else if (complete) {
                                failed = true;
                                return false;
                            }
                        }
                    } else if (!nonLosingMoves.contains(move)) {
                        failed = true;
                        return false;
                    } else {
                        if (!complete && strategy != null) { // check that move is same as from strategy
                            GGPMapping mapping = strategy.get(ms);
                            if (mapping == null) {
                                System.out.println("Given strategy is not a winning strategy");
                                failed = true;
                                return false;
                            }
                            if (!mapping.getMove().equals(move)) { // DOESN'T WORK BECAUSE OF TRANSFORMATIONS!
                                failed = true;
                                return false;
                            }

                        }
                        addTask(GGPManager.getNextState(ms, move));
                    }
                }
            } catch (GoalDefinitionException | TransitionDefinitionException | MoveDefinitionException e) {
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
                GGPVerificationTask t = new GGPVerificationTask(ms, team, complete, strategy);
                forks.add(t);
                t.fork();
            }
        }
    }
}

