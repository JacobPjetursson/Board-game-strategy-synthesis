package tictactoe.FFT;

import fftlib.*;
import fftlib.FFT;
import fftlib.game.FFTMove;
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
    static HashMap<Move, MoveGroup> moveGroups = new HashMap<>();
    private static HashMap<State, MinimaxPlay> lookupTable;
    private static HashMap<State, MinimaxPlay> lookupTableAll;
    private static FFT fft;

    public static boolean INCLUDE_ILLEGAL_STATES = false; // Used for FFT autogen, when solving

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics(null);
        new FFTManager(gs);
        fft = new FFT("Autogen");
        fft.addRuleGroup(new RuleGroup("Autogen"));
        new LookupTableMinimax(PLAYER1, new State());
        lookupTable = Database.getLookupTable();

        INCLUDE_ILLEGAL_STATES = true;
        new LookupTableFullGen(PLAYER1);
        lookupTableAll = Database.getLookupTableAll();

        populateGroups(PLAYER1);
        Move move = new Move(1, 1, PLAYER1);
        moveGroups.get(move).makeRules();

        move = new Move(0, 0, PLAYER1);
        moveGroups.get(move).makeRules();
        move = new Move(1, 0, PLAYER1);
        moveGroups.get(move).makeRules();


        INCLUDE_ILLEGAL_STATES = false;
    }

    public static FFT generateFFT(int team) {
        fft = new FFT("Autogen");
        fft.addRuleGroup(new RuleGroup("Autogen"));
        lookupTable = Database.getLookupTable();

        INCLUDE_ILLEGAL_STATES = true;
        new LookupTableFullGen(PLAYER1);
        lookupTableAll = Database.getLookupTableAll();

        populateGroups(team);
        Move move = new Move(1, 1, PLAYER1);
        moveGroups.get(move).makeRules();
        move = new Move(0, 0, PLAYER1);
        moveGroups.get(move).makeRules();
        move = new Move(1, 0, PLAYER1);
        moveGroups.get(move).makeRules();
        INCLUDE_ILLEGAL_STATES = false;
        return fft;
    }


    private static void populateGroups(int team) {
        for (Map.Entry<State, MinimaxPlay> entry : lookupTable.entrySet()) {
            State state = entry.getKey();
            MinimaxPlay play = entry.getValue();

            boolean threshold;
            switch(team) {
                case PLAYER1:
                    threshold = play.score > 0;
                    break;
                case PLAYER2:
                    threshold = play.score < -1000 || (play.score > 0 && play.score < 1000);
                    break;
                case PLAYER_ANY:
                    threshold = true;
                    break;
                default:
                    threshold = false;

            }
            if (threshold) {
                Move m = play.getMove();
                if (moveGroups.get(m) == null) {
                    MoveGroup mg = new MoveGroup(m);
                    mg.add(state);
                    moveGroups.put(m, mg);
                } else {
                    moveGroups.get(m).add(state);
                }
            }
        }
    }

    private static class MoveGroup {
        Move move;
        private PriorityQueue<State> states;

        MoveGroup(Move move) {
            this.move = move;

            states = new PriorityQueue<>(new StateComparator());
        }

        public void add(State s) {
            states.add(s);
        }

        void makeRules() {
            while (!states.isEmpty()) {
                System.out.println("\nREMAINING STATES: " + states.size());
                LinkedList<State> deleteList = new LinkedList<>();
                State state = states.poll();
                Rule r = makeRule(state);
                fft.ruleGroups.get(0).rules.add(r);
                System.out.println("FINAL RULE: " + r.print());
                for (State s : states) {
                    FFTMove m = r.apply(s);
                    if (m != null)
                        deleteList.add(s);
                }
                states.removeAll(deleteList);
            }
        }

        Rule makeRule(State s) {
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
            System.out.println("ORIGINAL MOVE: " + move.print());
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
            return new Rule(precons, move.getAction());
        }

        boolean isLiteralRelevant(Literal l, HashSet<Literal> stateLits, MinimaxPlay bestPlay, MinimaxPlay bestEnemyPlay) {
            Literal origLiteral = new Literal(l);
            for (int i = PIECEOCC_PLAYER; i <= PIECEOCC_ANY; i++) {
                if (origLiteral.pieceOcc == i) // Only change to new pieceocc
                    continue;
                System.out.print("CHANGING: " + origLiteral.name);
                l.setPieceOcc(i);
                l.setNegation(i == PIECEOCC_ANY);
                System.out.println(" TO: " + l.name);

                State newState = (State) FFTManager.preconsToState.apply(stateLits, PLAYER1);
/*
                FFTMove verificationMove = fft.apply(newState);
                if (verificationMove != null && verificationMove.equals(move)) {
                    System.out.println("NEWSTATE APPLIES TO PREVIOUS RULES: " + verificationMove.print());
                    continue;
                }
*/
                State newEnemyState = new State(newState);
                newEnemyState.setTurn(newState.getTurn() == PLAYER1 ? PLAYER2 : PLAYER1);
                System.out.println("NEWSTATE" + newState.print() + " , TEAM: " + newState.getTurn());
                ArrayList<Move> bestMoves = Database.bestPlays(newState);
                MinimaxPlay newBestPlay = lookupTableAll.get(newState);
                MinimaxPlay newBestEnemyPlay = lookupTableAll.get(newEnemyState);
                boolean relevant = false;
                if (newBestPlay != null)
                    System.out.println("NEW SCORE: " + newBestPlay.score + " , OLD SCORE: " + bestPlay.score + "  , ENEMY SCORE: " + newBestEnemyPlay.score);
                if (newBestEnemyPlay != null && bestEnemyPlay.score > -1000 && newBestEnemyPlay.score < -1000) {
                    System.out.print("ENEMY STRATEGY CHANGED!\t");
                    relevant = true;
                }
                else if (newBestPlay != null && bestPlay.score > 1000 && newBestPlay.score < bestPlay.score) {
                    System.out.print("WIN IN LESS TURNS TO NO WIN AT ALL!\t");
                    relevant = true;
                }
                else if (newBestPlay != null && bestPlay.score >= 0 && newBestPlay.score < 0) {
                    System.out.print("FROM DRAW TO LOSS!\t");
                    relevant = true;
                }
                else if (!bestMoves.isEmpty() && !bestMoves.contains(this.move)) {
                    System.out.print("MOVE IS NOT AMONG BEST MOVES FOR THAT STATE! BEST MOVES ARE:\t");
                    for (Move m : bestMoves)
                        System.out.print(m.print() + "\t");
                    System.out.println();
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

    }

    private static class StateComparator implements Comparator<State>{

        @Override
        public int compare(State s1, State s2) {
            int s1_score = lookupTable.get(s1).score;
            int s2_score = lookupTable.get(s2).score;
            if (s1_score < s2_score)
                return 1;
            else if (s1_score > s2_score)
                return -1;
            return 0;
        }
    }

}
