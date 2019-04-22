package tictactoe.FFT;

import fftlib.*;
import fftlib.FFT;
import tictactoe.ai.LookupTableMinimax;
import tictactoe.ai.MinimaxPlay;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.*;

import static misc.Config.*;

public class FFTAutoGen {
    private static HashMap<State, MinimaxPlay> lookupTable;
    private static HashMap<State, MinimaxPlay> lookupTableAll;
    private static LinkedList<State> states;
    private static FFT fft;
    private static RuleGroup rg;

    // CONFIGURATION
    private static int perspective = PLAYER_ANY;
    private static int winner = PLAYER_NONE;
    private static boolean detailedDebug = true;

    public static boolean INCLUDE_ILLEGAL_STATES = false; // Used for FFT autogen, when solving

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics(null);
        new FFTManager(gs);
        new LookupTableMinimax(PLAYER1, new State());
        setup();
    }

    public static FFT generateFFT(int team) {
        setup();
        return fft;
    }

    private static void setup() {
        INCLUDE_ILLEGAL_STATES = true;
        fft = new FFT("Autogen");
        rg = new RuleGroup("Autogen");
        fft.addRuleGroup(rg);
        lookupTable = Database.getLookupTable();
        new LookupTableFullGen(PLAYER1);
        lookupTableAll = Database.getLookupTableAll();

        System.out.println("LOOKUP TABLE SIZE: " + lookupTable.size());
        System.out.println("ILLEGAL LOOKUP TABLE SIZE: " + lookupTableAll.size());

        states = new LinkedList<>();
        populateQueue(perspective);
        System.out.println("AMOUNT OF STATES BEFORE DELETION: " + states.size());
        deleteIrrelevantStates();
        System.out.println("AMOUNT OF STATES AFTER DELETION: " + states.size());
        makeRules();
        System.out.println("TOTAL AMOUNT OF RULES: " + rg.rules.size());

        ArrayList<Rule> redundantRules = fft.minimize(perspective);
        System.out.println("REDUNDANT RULES REMOVED: " + redundantRules.size());


        INCLUDE_ILLEGAL_STATES = false;
    }


    private static void populateQueue(int team) {
        for (Map.Entry<State, MinimaxPlay> entry : lookupTable.entrySet()) {
            State state = entry.getKey();
            MinimaxPlay play = entry.getValue();
            if (team == PLAYER1 && play.move.getTeam() != PLAYER1)
                continue;
            else if (team == PLAYER2 && play.move.getTeam() != PLAYER2)
                continue;

            boolean threshold = false;
            switch(team) {
                case PLAYER1:
                    threshold = play.getWinner() == PLAYER1 || winner == play.getWinner();
                    break;
                case PLAYER2:
                    threshold = play.getWinner() == PLAYER2 || winner == play.getWinner();
                    break;
                case PLAYER_ANY:
                    if (play.move.getTeam() == PLAYER1 && (play.getWinner() == PLAYER1 || play.getWinner() == PLAYER_NONE))
                        threshold = true;

                    else if (play.move.getTeam() == PLAYER2 &&
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
        Iterator<State> itr = states.listIterator();
        while(itr.hasNext()) {
            State s = itr.next();
            ArrayList<Move> nonLosingMoves = Database.nonLosingMoves(s);
            if (nonLosingMoves.size() == s.getLegalMoves().size())
                itr.remove();
        }
    }

    private static void makeRules() {
        while (!states.isEmpty()) {
            if (detailedDebug) System.out.println("\nREMAINING STATES: " + states.size());
            State state = states.poll();

            Rule r = makeRule(state);
            if (rg.rules.contains(r))
                continue;

            rg.rules.add(r);
            if (detailedDebug) System.out.println("FINAL RULE: " + r.print());
            Iterator<State> itr = states.listIterator();
            while(itr.hasNext()) {
                State s = itr.next();
                Move m = (Move) r.apply(s);
                if (m != null)
                    itr.remove();
            }
        }
    }

    private static Rule makeRule(State s) {
        HashSet<Literal> minSet = new HashSet<>();
        ArrayList<Literal> copy = new ArrayList<>();
        MinimaxPlay bestPlay = lookupTable.get(s);
        Action bestAction = bestPlay.move.getAction();

        for (Literal l : s.getAllLiterals()) {
            minSet.add(new Literal(l));
            copy.add(new Literal(l));
        }

        // DEBUG
        if (detailedDebug) {
            System.out.print("ORIGINAL LITERALS: ");
            for (Literal l : copy)
                System.out.print(l.name + " ");
            System.out.println();
            System.out.println("ORIGINAL STATE: " + s.print());
            System.out.println("ORIGINAL MOVE: " + bestPlay.move.print());
            System.out.println("ORIGINAL SCORE: " + bestPlay.score);
        }

        for (Literal l : copy) {
            if (detailedDebug) System.out.println("INSPECTING: " + l.name);
            minSet.remove(l);
            Rule r = new Rule(new Clause(minSet), bestAction);
            rg.rules.add(r);

            if (!fft.verify(perspective, false)) {
                if (detailedDebug) System.out.println("FAILED TO VERIFY RULE!");
                minSet.add(l);
            } else if (detailedDebug)
                System.out.println("REMOVING: " + l.name);

            rg.rules.remove(r);
        }

        // DEBUG
        if (detailedDebug) {
            System.out.print("ALL LITERALS: ");
            for (Literal l : s.getAllLiterals())
                System.out.print(l.name + " ");
            System.out.println();
            System.out.print("MINSET: ");
            for (Literal l : minSet)
                System.out.print(l.name + " ");
            System.out.println();
        }

        // Convert minSet with move to rule
        Clause precons = new Clause(minSet);
        return new Rule(precons, bestAction);
    }

    private static class StateComparator implements Comparator<State>{

        @Override
        public int compare(State s1, State s2) {
            /*
            // TODO - consider if ordering is important, and how to order in best way
            int s1_score = lookupTable.get(s1).score;
            int s2_score = lookupTable.get(s2).score;
            if (perspective == PLAYER1) {
                if (s1_score < s2_score)
                    return 1;
                else if (s1_score > s2_score)
                    return -1;
            } else if (perspective == PLAYER2) {
                if (s1_score < s2_score)
                    return -1;
                else if (s1_score > s2_score)
                    return 1;
            } else {
                if (Math.abs(s1_score) < Math.abs(s2_score))
                    return 1;
                else if (Math.abs(s1_score) > Math.abs(s2_score))
                    return -1;
            }
            */
            // Change this value to 1 or -1 to avoid non-determinism
            return 0;
        }
    }

}
