package tictactoe.FFT;

import fftlib.*;
import fftlib.FFT;
import tictactoe.ai.LookupTableMinimax;
import tictactoe.ai.MinimaxPlay;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.*;

import static fftlib.Literal.PIECEOCC_ANY;
import static fftlib.Literal.PIECEOCC_PLAYER;
import static misc.Config.*;

public class FFTAutoGen {
    private static HashMap<State, MinimaxPlay> lookupTable;
    private static HashMap<State, MinimaxPlay> lookupTableAll;
    private static PriorityQueue<State> states;
    private static FFT fft;

    private static int perspective = PLAYER_ANY;

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
        fft.addRuleGroup(new RuleGroup("Autogen"));
        lookupTable = Database.getLookupTable();
        new LookupTableFullGen(PLAYER1);
        lookupTableAll = Database.getLookupTableAll();

        System.out.println("LOOKUP TABLE SIZE: " + lookupTable.size());
        System.out.println("ILLEGAL LOOKUP TABLE SIZE: " + lookupTableAll.size());

        states = new PriorityQueue<>(new StateComparator());
        populateQueue(perspective);
        System.out.println("AMOUNT OF STATES: " + states.size());
        makeRules();
        System.out.println("TOTAL AMOUNT OF RULES: " + fft.ruleGroups.get(0).rules.size());

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
                    threshold = play.score > 0;
                    break;
                case PLAYER2:
                    threshold = play.score < -1000 || (play.score > 0 && play.score < 1000);
                    break;
                case PLAYER_ANY:
                    if (play.move.getTeam() == PLAYER1 && play.score > 0)
                        threshold = true;
                    else if (play.move.getTeam() == PLAYER2 &&
                            (play.score < -1000 || (play.score > 0 && play.score < 1000)))
                        threshold = true;
                    break;
            }
            if (threshold) {
                states.add(state);
            }
        }
    }

    private static void makeRules() {
        while (!states.isEmpty()) {
            System.out.println("\nREMAINING STATES: " + states.size());
            LinkedList<State> deleteList = new LinkedList<>(); // TODO - deleteList slow shit
            State state = states.poll();

            Rule r = makeRule(state);
            fft.ruleGroups.get(0).rules.add(r);
            System.out.println("FINAL RULE: " + r.print());
            for (State s : states) {
                ArrayList<Move> nonLosingMoves = Database.nonLosingMoves(s);
                Move m = (Move) r.apply(s);
                if (m != null && nonLosingMoves.contains(m))
                    deleteList.add(s);
            }
            states.removeAll(deleteList);
        }
    }

    private static Rule makeRule(State s) {
        HashSet<Literal> minSet = new HashSet<>();
        HashSet<Literal> copy = new HashSet<>();
        MinimaxPlay bestPlay = lookupTable.get(s);
        State enemyS = new State(s);
        enemyS.setTurn(s.getTurn() == PLAYER1 ? PLAYER2 : PLAYER1);
        MinimaxPlay bestEnemyPlay = lookupTableAll.get(enemyS);

        for (Literal l : s.getAllLiterals()) {
            minSet.add(new Literal(l));
            copy.add(new Literal(l));
        }

        // DEBUG
        System.out.print("ORIGINAL LITERALS: ");
        for (Literal l : copy)
            System.out.print(l.name + " ");
        System.out.println();
        System.out.println("ORIGINAL STATE: " + s.print());
        System.out.println("ORIGINAL MOVE: " + bestPlay.move.print());
        System.out.println("ORIGINAL SCORE: " + bestPlay.score);
        System.out.println("ORIGINAL ENEMY SCORE: " + bestEnemyPlay.score);

        for (Literal l : copy) {
            if (!isLiteralRelevant(l, copy, bestPlay, bestEnemyPlay)) {
                System.out.println("REMOVING: " + l.name);
                minSet.remove(l);
            }
        }

        // DEBUG
        System.out.print("ALL LITERALS: ");
        for (Literal l : s.getAllLiterals())
            System.out.print(l.name + " ");
        System.out.println();
        System.out.print("MINSET: ");
        for (Literal l : minSet)
            System.out.print(l.name + " ");
        System.out.println();

        // Convert minSet with move to rule
        ArrayList<Literal> lits = new ArrayList<>(minSet);
        Clause precons = new Clause(lits);
        return new Rule(precons, bestPlay.move.getAction());
    }

    private static boolean isLiteralRelevant(Literal l, HashSet<Literal> stateLits, MinimaxPlay bestPlay, MinimaxPlay bestEnemyPlay) {
        Literal origLiteral = new Literal(l);
        int team = bestPlay.move.team;
        int opponent = team == PLAYER1 ? PLAYER2 : PLAYER1;
        for (int i = PIECEOCC_PLAYER; i <= PIECEOCC_ANY; i++) {
            if (origLiteral.pieceOcc == i) // Only change to new pieceocc
                continue;
            System.out.print("CHANGING: " + origLiteral.name);
            l.setPieceOcc(i);
            l.setNegation(i == PIECEOCC_ANY);
            System.out.println(" TO: " + l.name);

            State newState = (State) FFTManager.preconsToState.apply(stateLits, team);
/*
            FFTMove verificationMove = fft.apply(newState);
            if (verificationMove != null && verificationMove.equals(bestPlay.move)) {
                System.out.println("NEWSTATE APPLIES TO PREVIOUS RULES: " + verificationMove.print());
                continue;
            }
*/
            State newEnemyState = new State(newState);
            newEnemyState.setTurn(opponent);
            ArrayList<Move> nonLosingMoves = Database.nonLosingMoves(newState);
            MinimaxPlay newBestPlay = lookupTableAll.get(newState);
            MinimaxPlay newBestEnemyPlay = lookupTableAll.get(newEnemyState);
            System.out.println("NEWSTATE" + newState.print() + " , TEAM: " + newState.getTurn());
            if (newBestPlay != null)
                System.out.println("NEW SCORE: " + newBestPlay.score + " , OLD SCORE: " + bestPlay.score + "  , ENEMY SCORE: " + newBestEnemyPlay.score);

            boolean relevant = false;
            if (newBestEnemyPlay != null) {
                if (team == PLAYER1 && bestEnemyPlay.score > -1000 && newBestEnemyPlay.score < -1000) {
                    System.out.print("ENEMY STRATEGY (P2) CHANGED!\t");
                    relevant = true;
                } else if (team == PLAYER2 && bestEnemyPlay.score < 1000 && newBestEnemyPlay.score > 1000) {
                    System.out.println("ENEMY STRATEGY (P1) CHANGED!\t");
                    relevant = true;

                }
                else if (team == PLAYER1 && bestPlay.score > 0 && bestPlay.score < 1000 && bestEnemyPlay.score < -1000 && newBestEnemyPlay.score > -1000) {
                    System.out.println("ENEMY STRATEGY (P1) CHANGED\t");
                    relevant = true;
                }
                else if (team == PLAYER2 && bestPlay.score > 0 && bestPlay.score < 1000 && bestEnemyPlay.score > 1000 && newBestEnemyPlay.score < 1000) {
                    System.out.println("ENEMY STRATEGY (P2) CHANGED!\t");
                    relevant = true;
                }
            }
            if (newBestPlay != null && !relevant) {
                if (team == PLAYER1 && bestPlay.score > 1000 && newBestPlay.score < 1000) {
                    System.out.print("FROM WIN (P1) TO DRAW/LOSS!\t");
                    relevant = true;
                } else if (team == PLAYER2 && bestPlay.score < -1000 && newBestPlay.score > -1000) {
                    System.out.println("FROM WIN (P2) TO DRAW/LOSS!\t");
                    relevant = true;
                } else if (team == PLAYER1 && bestPlay.score >= 0 && newBestPlay.score < -1000) {
                    System.out.print("FROM DRAW (P1) TO LOSS!\t");
                    relevant = true;
                } else if (team == PLAYER2 && bestPlay.score >= 0 && newBestPlay.score > 1000) {
                    System.out.println("FROM DRAW (P2) TO LOSS!\t");
                    relevant = true;
                }
            }
            System.out.print("NONLOSING MOVES:\t");
            for (Move m : nonLosingMoves)
                System.out.print(m.print() + "\t");
            System.out.println();
            if (!relevant && !nonLosingMoves.isEmpty() && !nonLosingMoves.contains(bestPlay.move)) {
                //System.out.print("MOVE IS NOT AMONG NONLOSING MOVES FOR THAT STATE! NONLOSING MOVES ARE:\t");
                //for (Move m : nonLosingMoves)
                //    System.out.print(m.print() + "\t");
                //System.out.println();
                relevant = true;
            }

            if (relevant) {
                l.setPieceOcc(origLiteral.pieceOcc);
                l.setNegation(origLiteral.negation);
                System.out.println(l.name + " IS RELEVANT");
                return true;
            }

        }
        l.setPieceOcc(origLiteral.pieceOcc);
        l.setNegation(origLiteral.negation);
        return false;
    }

    private static class StateComparator implements Comparator<State>{

        @Override
        public int compare(State s1, State s2) {
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
            return 0;
        }
    }

}
