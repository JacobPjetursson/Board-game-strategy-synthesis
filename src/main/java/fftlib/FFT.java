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
    public FFTStateAndMove failingPoint = null;

    // For concurrency purposes
    private static ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    private ConcurrentHashMap<FFTState, Boolean> closedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<MachineState, Boolean> closedMapGGP = new ConcurrentHashMap<>();
    private boolean failed; // failed is set to true if verification failed

    public HashMap<FFTState, StateMapping> singleStrategy; // used for alternative version of program

    // used for optimization
    private ConcurrentHashMap<FFTState, FFTMove> strategy = new ConcurrentHashMap<>(); // used for storing verification results, e.g. moves from strat
    private ConcurrentHashMap<MachineState, Move> strategyGGP = new ConcurrentHashMap<>();
    public boolean SAFE_RUN; // used as flag to signal that current run is a safe run, e.g. verification guaranteed to succeed
    public boolean USE_STRATEGY;

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

    public void setSingleStrategy(HashMap<FFTState, StateMapping> strat) {
        singleStrategy = strat;
    }

    public Rule getLastRule() {
        if (ruleGroups.isEmpty())
            return null;
        RuleGroup rg = ruleGroups.get(ruleGroups.size() - 1);
        if (rg.rules.isEmpty())
            return null;
        return rg.rules.get(rg.rules.size() - 1);
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
        FFTState initialState = FFTManager.initialFFTState;
        LinkedList<FFTState> frontier;
        frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        frontier.add(initialState);
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        // TODO - move this elsewhere
        // Check if win or draw is even possible (initial checks)
        int winner = FFTSolution.queryState(initialState).getWinner();
        if (team == PLAYER1 && winner == PLAYER2) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && winner == PLAYER1) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }

        while (!frontier.isEmpty()) {
            FFTState state = frontier.pop();
            if (FFTManager.logic.gameOver(state)) {
                if (FFTManager.logic.getWinner(state) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            } else if (team != state.getTurn()) {
                for (FFTState child : state.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                FFTMove move;
                Rule newRule = this.getLastRule();
                if (!USE_STRATEGY || newRule == null) {
                    move = apply(state);
                }
                else {
                    // re-use move from strategy if it's from a previous rule, otherwise apply with new rule
                    move = strategy.get(state);
                    if (move == null) {
                        move = newRule.apply(state);
                    }
                }
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    if (!complete && singleStrategy != null) { // only expand on move from strategy
                        StateMapping info = singleStrategy.get(state);
                        if (info == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        FFTState nextState = state.getNextState(info.getMove());
                        if (!closedSet.contains(nextState)) {
                            closedSet.add(nextState);
                            frontier.add(nextState);
                        }
                        continue;
                    }

                    for (FFTMove m : state.getLegalMoves()) {
                        if (optimalMoves.contains(m)) {
                            FFTState nextState = state.getNextState(m);
                            if (!closedSet.contains(nextState)) {
                                closedSet.add(nextState);
                                frontier.add(nextState);
                            }
                        } else if (complete) {
                            failingPoint = new FFTStateAndMove(state, m, true);
                            return false;
                        }
                    }
                } else if (!optimalMoves.contains(move)) {
                    failingPoint = new FFTStateAndMove(state, move, false);
                    return false;
                } else { // move is not null, expand on state from that move
                    if (!complete && singleStrategy != null) { // check that move is same as from strategy
                        StateMapping info = singleStrategy.get(state);
                        if (info == null) {
                            System.out.println("Given strategy is not a winning strategy");
                            return false;
                        }
                        if (!info.getMove().equals(move)) { // DOESN'T WORK BECAUSE OF TRANSFORMATIONS!
                            return false;
                        }

                    }
                    // insert into strategy if we know verification will succeed
                    if (USE_STRATEGY && SAFE_RUN)
                        strategy.put(state, move);
                    FFTState nextState = state.getNextState(move);
                    if (!closedSet.contains(nextState)) {
                        closedSet.add(nextState);
                        frontier.add(nextState);
                    }
                }
            }
        }
        return true;
    }

    public boolean verify_(int team, boolean complete) {
        if (team == PLAYER_ANY)
            return verify_(PLAYER1, complete) && verify_(PLAYER2, complete);
        FFTState initialState = FFTManager.initialFFTState;
        closedMap.clear();
        failed = false;
        // Check if win or draw is even possible
        int winner = FFTSolution.queryState(initialState).getWinner();
        if (team == PLAYER1 && winner == PLAYER2) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && winner == PLAYER1) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }
        return forkJoinPool.invoke(new VerificationTask(initialState, team, complete));
    }


    private boolean verifyGGP(int team, boolean complete) {
        if (team == PLAYER_ANY)
            return verifyGGP(PLAYER1, complete) && verifyGGP(PLAYER2, complete);
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
        return forkJoinPool.invoke(new GGPVerificationTask(initialState, team, complete));
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
        // Once we've started minimizing, we can't use our strategy hashset anymore
        USE_STRATEGY = false;

        int ruleSize = getAmountOfRules();
        int precSize = getAmountOfPreconditions();

        minimizeRules(perspective);
        if (!MINIMIZE_RULE_BY_RULE && minimize_precons) {
            minimizePreconditions(perspective);
        }

        int minRuleSize = getAmountOfRules();
        int minPrecSize = getAmountOfPreconditions();

        int i = 0;
        while (ruleSize != minRuleSize || precSize != minPrecSize) {
            ruleSize = minRuleSize;
            precSize = minPrecSize;

            minimizeRules(perspective);
            if (!MINIMIZE_RULE_BY_RULE && minimize_precons)
                minimizePreconditions(perspective);

            minRuleSize = getAmountOfRules();
            minPrecSize = getAmountOfPreconditions();
            i++;
        }
        return i;
    }

    private ArrayList<Rule> minimizeRules(int team) {
        ArrayList<Rule> redundantRules = new ArrayList<>();
        if (MINIMIZE_BOTTOMS_UP)
            return minimizeRulesBottomsUp(team);


        for (RuleGroup rg : ruleGroups) {
            if (rg.locked) // don't minimize if rulegroup is locked
                continue;
            ListIterator<Rule> itr = rg.rules.listIterator();
            while(itr.hasNext()) {
                Rule r = itr.next();
                if (r.multiRule) continue; // TODO - support multirule when minimizing?
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

    private ArrayList<Rule> minimizeRulesBottomsUp(int team) {
        ArrayList<Rule> redundantRules = new ArrayList<>();
        for (int i = ruleGroups.size() - 1; i >= 0; i--) {
            RuleGroup rg = ruleGroups.get(i);
            if (rg.locked) // don't minimize if rulegroup is locked
                continue;
            ListIterator<Rule> itr = rg.rules.listIterator(rg.rules.size());
            while(itr.hasPrevious()) {
                Rule r = itr.previous();
                if (r.multiRule) continue; // TODO - support multirule when minimizing?
                itr.remove();
                if (verify(team, true)) {
                    redundantRules.add(r);
                }
                else {
                    itr.add(r);
                    itr.previous();
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
                if (r.multiRule) continue; // TODO - support multirule when minimizing?
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
            for (Literal l : r.preconditions.literals)
                literals.add(l.clone());

            for (Literal l : literals) {
                r.removePrecondition(l);
                if (!verify(team, true))
                    r.addPrecondition(l);
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

        VerificationTask(FFTState state, int team, boolean complete) {
            this.state = state;
            this.team = team;
            this.complete = complete;
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
                FFTMove move;
                Rule newRule = getLastRule();
                if (!USE_STRATEGY || newRule == null) {
                    move = apply(state);
                }
                else {
                    // re-use move from strategy if it's from a previous rule, otherwise apply with new rule
                    move = strategy.get(state);
                    if (move == null) {
                        move = newRule.apply(state);
                    }
                }
                ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    if (!complete && singleStrategy != null) { // only expand on move from strategy
                        StateMapping info = singleStrategy.get(state);
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
                        if (optimalMoves.contains(m)) {
                            addTask(state.getNextState(m));
                        } else if (complete) {
                            failingPoint = new FFTStateAndMove(state, m, true);
                            failed = true;
                            return false;
                        }
                    }
                } else if (!optimalMoves.contains(move)) {
                    failingPoint = new FFTStateAndMove(state, move, false);
                    failed = true;
                    return false;
                } else {
                    if (!complete && singleStrategy != null) { // check that move is same as from strategy
                        StateMapping info = singleStrategy.get(state);
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
                    if (USE_STRATEGY && SAFE_RUN)
                        strategy.put(state, move);
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
                VerificationTask t = new VerificationTask(state, team, complete);
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
                    Move move;
                    Rule newRule = getLastRule();
                    if (!USE_STRATEGY || newRule == null) {
                        move = apply(ms);
                    }
                    else {
                        // re-use move from strategy if it's from a previous rule, otherwise apply with new rule
                        move = strategyGGP.get(ms);
                        if (move == null) {
                            move = newRule.apply(ms);
                        }
                    }
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
                        if (USE_STRATEGY && SAFE_RUN)
                            strategyGGP.put(ms, move);
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
                GGPVerificationTask t = new GGPVerificationTask(ms, team, complete);
                forks.add(t);
                t.fork();
            }
        }
    }
}

