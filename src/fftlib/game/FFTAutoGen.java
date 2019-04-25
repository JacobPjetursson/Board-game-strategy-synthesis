package fftlib.game;

import fftlib.*;
import fftlib.FFT;

import java.util.*;

import static misc.Config.*;

public class FFTAutoGen {
    private static HashMap<? extends FFTState, ? extends FFTMinimaxPlay> lookupTable;
    private static PriorityQueue<FFTState> states;
    private static FFT fft;
    private static RuleGroup rg;

    // CONFIGURATION
    private static int perspective;
    private static int winner;
    private static boolean detailedDebug = false;

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

        states = new PriorityQueue<>(new StateComparator());
        System.out.println("Filtering...");
        populateQueue();
        deleteIrrelevantStates();
        System.out.println("Amount of states after filtering: " + states.size());
        System.out.println("Making rules");
        makeRules();

        System.out.println("Amount of rules before minimizing: " + rg.rules.size());
        fft.minimize(perspective);
        System.out.println("Amount of rules after minimizing: " + rg.rules.size());

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on Autogenerating: " + timeSpent + " seconds");
    }


    private static void populateQueue() {
        for (Map.Entry<? extends FFTState, ? extends FFTMinimaxPlay> entry : lookupTable.entrySet()) {
            FFTState state = entry.getKey();
            FFTMinimaxPlay play = entry.getValue();
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
                states.add(state);
            }
        }
    }

    // States where all moves are correct should not be part of FFT
    private static void deleteIrrelevantStates() {
        LinkedList<FFTState> deleteList = new LinkedList<>();
        for (FFTState s : states) {
            ArrayList<? extends FFTMove> nonLosingMoves = FFTManager.db.nonLosingMoves(s);
            if (nonLosingMoves.size() == s.getLegalMoves().size()) {
                deleteList.add(s);
            }
        }
        states.removeAll(deleteList);
    }

    private static void makeRules() {
        while (!states.isEmpty()) {
            System.out.println("Remaining states: " + states.size() + ". Current amount of rules: " + rg.rules.size());
            FFTState state = states.poll();

            Rule r = addRule(state);
            if (detailedDebug) System.out.println("FINAL RULE: " + r.print());

            // Delete states that apply
            LinkedList<FFTState> deleteList = new LinkedList<>();
            for (FFTState s : states)
                if (r.apply(s) != null)
                    deleteList.add(s);

            states.removeAll(deleteList);
        }
    }

    private static Rule addRule(FFTState s) {
        HashSet<Literal> minSet = new HashSet<>();
        HashSet<Literal> literals = s.getAllLiterals();
        FFTMinimaxPlay bestPlay = lookupTable.get(s);
        Action bestAction = bestPlay.getMove().getAction();

        for (Literal l : literals)
            minSet.add(new Literal(l));

        Rule r = new Rule(new Clause(minSet), bestAction);
        rg.rules.add(r);

        // DEBUG
        if (detailedDebug) {
            System.out.print("ORIGINAL LITERALS: ");
            for (Literal l : literals)
                System.out.print(l.name + " ");
            System.out.println();
            System.out.println("ORIGINAL STATE: " + s.print());
            System.out.println("ORIGINAL MOVE: " + bestPlay.getMove().print());
            System.out.println("ORIGINAL SCORE: " + bestPlay.getScore());
        }

        for (Literal l : literals) {
            if (detailedDebug) System.out.println("INSPECTING: " + l.name);
            r.removePrecondition(l);

            if (!fft.verify(perspective, false)) {
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
            // TODO - consider if ordering is important, and how to order in best way
            // TODO - also, why is a 0-compare version twice as fast as linkedlist, and better results than manual compare function?
/*
            int s1_score = lookupTable.get(s1).getScore();
            int s2_score = lookupTable.get(s2).getScore();
            if (perspective == PLAYER1) {
                return s1_score - s2_score;
            } else if (perspective == PLAYER2) {
                return s2_score - s1_score;
            } else {
                return Math.abs(s1_score) - Math.abs(s2_score);
            }
*/
            return -1;
        }
    }

}
