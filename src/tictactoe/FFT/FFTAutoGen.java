package tictactoe.FFT;

import fftlib.FFTManager;
import fftlib.Literal;
import tictactoe.ai.LookupTableMinimax;
import tictactoe.ai.MinimaxPlay;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import static fftlib.Literal.*;
import static misc.Config.*;


public class FFTAutoGen {
    static HashMap<Move, MoveGroup> moveGroups = new HashMap<>();
    private static HashMap<State, MinimaxPlay> lookupTable;

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics(null);
        new FFTManager(gs);
        new LookupTableMinimax(PLAYER1, new State());
        lookupTable = Database.getLookupTable();
        populateGroups(PLAYER1);
        Move move = new Move(1, 0, PLAYER1);
        moveGroups.get(move).makeStateGroups();

    }


    private static void populateGroups(int team) {
        HashMap<State, MinimaxPlay> lookupTable = Database.getLookupTable();
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
        private LinkedList<State> states;
        private HashSet<Literal> allLiterals;
        private LinkedList<LinkedList<State>> stateGroupings;

        MoveGroup(Move move) {
            this.move = move;
            states = new LinkedList<>();
            allLiterals = new HashSet<>();
            stateGroupings = new LinkedList<>();
        }

        public void add(State s) {
            states.add(s);
        }

        public void generateAllLiterals() {
            for (State s : states) {
                allLiterals.addAll(s.getAllLiterals());
            }
        }
        /*
        public HashSet<Literal> getNonSharedLiterals() { // Literals that are not shared by all states
            HashSet<Literal> nonSharedLiterals = new HashSet<>();
            for (Literal l : allLiterals) {
                for (State s : states) {
                    if (!s.getLiterals().contains(l)) {
                        nonSharedLiterals.add(l);
                        break;
                    }
                }
            }
            return nonSharedLiterals;
        }
        */
        void makeStateGroups() {
            State s = states.get(0);
            HashSet<Literal> minSet = new HashSet<>();
            HashSet<Literal> copy = new HashSet<>();
            for (Literal l : s.getAllLiterals()) {
                minSet.add(new Literal(l));
                copy.add(new Literal(l));
            }

            State origState = (State) FFTManager.preconsToState.apply(copy, PLAYER1);
            System.out.print("ORIGINAL LITERALS: ");
            for (Literal l : copy)
                System.out.print(l.name + " ");
            System.out.println();
            System.out.println("ORIGINAL STATE: " + origState.print());
            System.out.println("ORIGINAL MOVE: " + move.print());

            for (Literal l : copy) {
                if (!isLiteralRelevant(l, copy)) {
                    System.out.println("REMOVING: " + l.name);
                    minSet.remove(l);
                }
            }
            System.out.print("ALL LITERALS: ");
            for (Literal l : s.getAllLiterals())
                System.out.print(l.name + " ");
            System.out.println();
            System.out.print("MINSET: ");
            for (Literal l : minSet)
                System.out.print(l.name + " ");
            System.out.println();
        }

        boolean isLiteralRelevant(Literal l, HashSet<Literal> stateLits) {
            Literal origLiteral = new Literal(l);
            for (int i = PIECEOCC_NONE; i <= PIECEOCC_ANY; i++) {
                if (origLiteral.pieceOcc == i) // Only change to new pieceocc
                    continue;
                System.out.print("CHANGING: " + l.name);
                l.setPieceOcc(i);
                System.out.println(" TO: " + l.name);
                for (Literal l1 : stateLits) {
                    if (l1.equals(origLiteral))
                        continue;
                    changeLiteral(l, l1, origLiteral.pieceOcc);
                    State newState = (State) FFTManager.preconsToState.apply(stateLits, PLAYER1);
                    System.out.println("NEWSTATE" + newState.print());
                    MinimaxPlay play = lookupTable.get(newState);
                    if (play != null && play.move == this.move) {
                        l.setPieceOcc(origLiteral.pieceOcc);
                        System.out.print("PLAY: " + play.move.print());
                        System.out.print(",  STILL APPLIES: ");
                        return false;
                    }
                }
            }
            l.setPieceOcc(origLiteral.pieceOcc);
            return false;
        }

        void changeLiteral(Literal stableLit, Literal targetLit, int prevPieceOcc) {
            if (prevPieceOcc == PIECEOCC_PLAYER) {
                if (stableLit.pieceOcc == PIECEOCC_NONE) {
                    if (targetLit.pieceOcc == PIECEOCC_ENEMY)
                        targetLit.setPieceOcc(PIECEOCC_NONE);
                    else if (targetLit.pieceOcc == PIECEOCC_NONE)
                        targetLit.setPieceOcc(PIECEOCC_PLAYER);
                }
                else if (stableLit.pieceOcc == PIECEOCC_ENEMY) {
                    if (targetLit.pieceOcc == PIECEOCC_ENEMY)
                        targetLit.setPieceOcc(PIECEOCC_PLAYER);
                }
            }
            else if (prevPieceOcc == PIECEOCC_ENEMY) {
                if (stableLit.pieceOcc == PIECEOCC_NONE) {
                    if (targetLit.pieceOcc == PIECEOCC_PLAYER)
                        targetLit.setPieceOcc(PIECEOCC_NONE);
                    else if (targetLit.pieceOcc == PIECEOCC_NONE)
                        targetLit.setPieceOcc(PIECEOCC_ENEMY);
                }
                else if (stableLit.pieceOcc == PIECEOCC_PLAYER) {
                    if (targetLit.pieceOcc == PIECEOCC_PLAYER)
                        targetLit.setPieceOcc(PIECEOCC_ENEMY);
                }
            }
            else if (prevPieceOcc == PIECEOCC_NONE) {
                if (stableLit.pieceOcc == PIECEOCC_PLAYER) {
                    if (targetLit.pieceOcc == PIECEOCC_NONE)
                        targetLit.setPieceOcc(PIECEOCC_ENEMY);
                    else if (targetLit.pieceOcc == PIECEOCC_PLAYER)
                        targetLit.setPieceOcc(PIECEOCC_NONE);
                }
                else if (stableLit.pieceOcc == PIECEOCC_ENEMY) {
                    if (targetLit.pieceOcc == PIECEOCC_NONE)
                        targetLit.setPieceOcc(PIECEOCC_PLAYER);
                    else if (targetLit.pieceOcc == PIECEOCC_ENEMY)
                        targetLit.setPieceOcc(PIECEOCC_NONE);
                }
            }
        }
    }

}
