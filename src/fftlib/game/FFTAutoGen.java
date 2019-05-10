package fftlib.game;

import fftlib.*;
import fftlib.FFT;

import java.util.*;

import static misc.Config.*;

public class FFTAutoGen {
    private static HashMap<? extends FFTState, ? extends FFTStateMapping> lookupTable;
    //private static HashMap<Long, FFTState> states;
    private static LinkedList<FFTState> states;
    private static HashMap<FFTState, FFTStateMapping> strategy;
    private static FFT fft;
    private static RuleGroup rg;

    // CONFIGURATION
    private static int perspective;
    private static int winner;
    private static boolean detailedDebug = false;
    private static boolean fullRules = false; // mainly for debug

    public static FFT generateFFT(int perspective_, int winner_) {
        perspective = perspective_;
        winner = winner_;
        setup();
        return fft;
    }

    private static void setup() {
        long timeStart = System.currentTimeMillis();
        fft = new FFT("Autogen");
        rg = new RuleGroup("Autogen");
        fft.addRuleGroup(rg);
        lookupTable = FFTManager.db.getSolution();

        System.out.println("Solution size: " + lookupTable.size());

        //states = new HashMap<>();
        states = new LinkedList<>();

        System.out.println("Filtering...");
        if (VERIFY_SINGLE_STRATEGY) {
            strategy = new HashMap<>();
            System.out.println("Filtering for single strategy");
            filterToSingleStrat();
        } else {
            System.out.println("Filtering for all strategies");
            filterSolution();
            deleteIrrelevantStates();
        }
        System.out.println("Amount of states after filtering: " + states.size());

        System.out.println("Making rules");
        makeRules();

        System.out.println("Amount of rules before minimizing: " + rg.rules.size());
        fft.minimizeRules(perspective);
        System.out.println("Amount of rules after minimizing: " + rg.rules.size());

        System.out.println("Amount of preconditions before minimizing: " + rg.getAmountOfPreconditions());
        fft.minimizePreconditions(perspective);
        System.out.println("Amount of preconditions after minimizing: " + rg.getAmountOfPreconditions());

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
    }


    private static void filterSolution() {
        for (Map.Entry<? extends FFTState, ? extends FFTStateMapping> entry : lookupTable.entrySet()) {
            FFTState state = entry.getKey();
            FFTStateMapping play = entry.getValue();
            if (perspective == PLAYER1 && play.getMove().getTeam() != PLAYER1)
                continue;
            else if (perspective == PLAYER2 && play.getMove().getTeam() != PLAYER2)
                continue;

            boolean threshold = false;
            switch(perspective) {
                case PLAYER1:
                    threshold = play.getWinner() == PLAYER1 || winner == play.getWinner();
                    break;
                case PLAYER2:
                    threshold = play.getWinner() == PLAYER2 || winner == play.getWinner();
                    break;
                case PLAYER_ANY:
                    if (play.getMove().getTeam() == PLAYER1 && (play.getWinner() == PLAYER1 || play.getWinner() == PLAYER_NONE))
                        threshold = true;

                    else if (play.getMove().getTeam() == PLAYER2 &&
                            (play.getWinner() == PLAYER2 || play.getWinner() == PLAYER_NONE))
                        threshold = true;
                    break;
            }
            if (threshold) {
                //states.put(state.getZobristKey(), state);
                states.add(state);
            }
        }
    }

    // States where all moves are correct should not be part of FFT
    private static void deleteIrrelevantStates() {
        //Iterator<Map.Entry<Long, FFTState>> itr = states.entrySet().iterator();
        Iterator<FFTState> itr = states.listIterator();
        while (itr.hasNext()) {
            //FFTState s = itr.next().getValue();
            FFTState s = itr.next();
            ArrayList<? extends FFTMove> nonLosingMoves = FFTManager.db.nonLosingMoves(s);
            if (nonLosingMoves.size() == s.getLegalMoves().size()) {
                itr.remove();
            }
        }
    }

    private static void filterToSingleStrat() {
        LinkedList<FFTState> frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        FFTState initialState = FFTManager.initialFFTState;
        frontier.add(initialState);
        while (!frontier.isEmpty()) {
            FFTState state = frontier.pop();
            if (FFTManager.logic.gameOver(state))
                continue;
            if (perspective != state.getTurn()) {
                for (FFTState child : state.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                //states.put(state.getZobristKey(), state);
                states.add(state);
                FFTStateMapping info = lookupTable.get(state);
                strategy.put(state, info);
                FFTState nextState = state.getNextState(info.getMove());
                if (!closedSet.contains(nextState)) {
                    closedSet.add(nextState);
                    frontier.add(nextState);
                }
            }
        }
    }

    private static void makeRules() {
        while (!states.isEmpty()) {
            System.out.println("Remaining states: " + states.size() + ". Current amount of rules: " + rg.rules.size());
            //FFTState state = states.entrySet().iterator().next().getValue();
            FFTState state = states.listIterator().next();

            Rule r = addRule(state);
            //states.remove(state.getZobristKey());
            states.remove(state);
            if (detailedDebug) System.out.println("FINAL RULE: " + r);


            if (fft.verify(perspective, true)) {
                System.out.println("FFT verified before empty statespace");
                return;
            }

            // Delete states that apply
            //Iterator<Map.Entry<Long, FFTState>> itr = states.entrySet().iterator();
            Iterator<FFTState> itr = states.listIterator();
            while (itr.hasNext()) {
                //FFTState s = itr.next().getValue();
                FFTState s = itr.next();
                if (r.apply(s) != null)
                    itr.remove();

            }

        }

    }

    private static Rule addRule(FFTState s) {
        HashSet<Literal> minSet = new HashSet<>();
        HashSet<Literal> literals = s.getAllLiterals();
        FFTStateMapping mapping = lookupTable.get(s);
        Action bestAction = mapping.getMove().getAction();

        for (Literal l : literals)
            minSet.add(new Literal(l));

        Rule r = new Rule(new Clause(minSet), bestAction);
        rg.rules.add(r);

        if (fullRules)
            return r;

        // DEBUG
        if (detailedDebug) {
            System.out.print("ORIGINAL LITERALS: ");
            for (Literal l : literals)
                System.out.print(l.name + " ");
            System.out.println();
            System.out.println("ORIGINAL STATE: " + s);
            System.out.println("ORIGINAL MOVE: " + mapping.getMove());
            System.out.println("ORIGINAL SCORE: " + mapping.getScore());
        }

        for (Literal l : literals) {
            if (detailedDebug) System.out.println("INSPECTING: " + l.name);
            r.removePrecondition(l);

            boolean verify = fft.verify(perspective, false, strategy); // strategy is null if VERIFY_SINGLE_STRAT is false
            if (!verify) {
                if (detailedDebug) System.out.println("FAILED TO VERIFY RULE!");
                r.addPrecondition(l);
            } else if (detailedDebug)
                System.out.println("REMOVING: " + l.name);
        }

        return r;
    }

    private static class StateComparator implements Comparator<FFTState>{
        @Override
        public int compare(FFTState s1, FFTState s2) {

            int s1_score = lookupTable.get(s1).getScore();
            int s2_score = lookupTable.get(s2).getScore();
            if (perspective == PLAYER1) {
                return s1_score - s2_score;
            } else if (perspective == PLAYER2) {
                return s2_score - s1_score;
            } else {
                int score = Math.abs(s1_score) - Math.abs(s2_score);
                if (score == 0)  return -1;
                else return score;
            }
        }
    }

}
