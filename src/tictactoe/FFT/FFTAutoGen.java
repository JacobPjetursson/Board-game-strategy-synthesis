package tictactoe.FFT;

import fftlib.FFTManager;
import fftlib.Literal;
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

    public static boolean INCLUDE_ILLEGAL_STATES = false; // Used for FFT autogen, when solving

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics(null);
        new FFTManager(gs);
        INCLUDE_ILLEGAL_STATES = true;
        new LookupTableMinimax(PLAYER1, new State());
        lookupTable = Database.getLookupTable();
        lookupTableAll = Database.getLookupTableAll();

        populateGroups(PLAYER1);
        Move move = new Move(1, 0, PLAYER1);
        moveGroups.get(move).makeStateGroups();


        INCLUDE_ILLEGAL_STATES = false;
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

        void makeStateGroups() {
            State s = states.get(0);
            HashSet<Literal> minSet = new HashSet<>();
            HashSet<Literal> copy = new HashSet<>();
            MinimaxPlay bestPlay = lookupTable.get(s);
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

            for (Literal l : copy) {
                if (!isLiteralRelevant(l, copy, bestPlay)) {
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

        boolean isLiteralRelevant(Literal l, HashSet<Literal> stateLits, MinimaxPlay bestPlay) {
            Literal origLiteral = new Literal(l);
            for (int i = PIECEOCC_PLAYER; i <= PIECEOCC_ANY; i++) {
                if (origLiteral.pieceOcc == i) // Only change to new pieceocc
                    continue;
                System.out.print("CHANGING: " + origLiteral.name);
                l.setPieceOcc(i);
                l.setNegation(i == PIECEOCC_ANY);
                System.out.println(" TO: " + l.name);

                State newState = (State) FFTManager.preconsToState.apply(stateLits, PLAYER1);
                System.out.println("NEWSTATE" + newState.print());
                ArrayList<Move> bestMoves = Database.bestPlays(newState);
                MinimaxPlay newBestPlay = lookupTableAll.get(newState);
                boolean relevant = false;
                if (newBestPlay != null && newBestPlay.score != bestPlay.score)
                    relevant = true;
                if (!bestMoves.isEmpty() && !bestMoves.contains(this.move))
                    relevant = true;

                if (relevant) {
                    l.setPieceOcc(origLiteral.pieceOcc);
                    l.setNegation(origLiteral.negation);
                    System.out.println("WRONG PLAY");
                    return true;
                }

            }
            l.setPieceOcc(origLiteral.pieceOcc);
            l.setNegation(origLiteral.negation);
            return false;
        }
    }

}
