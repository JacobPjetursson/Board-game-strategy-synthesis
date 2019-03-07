package tictactoe.FFT;

import fftlib.FFTManager;
import fftlib.Literal;
import tictactoe.ai.MinimaxPlay;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import static misc.Config.*;


public class FFTAutoGen {
    static HashMap<Move, MoveGroup> moveGroups = new HashMap<>();
    private static HashMap<State, MinimaxPlay> lookupTable;

    public static void main(String[] args) {
        populateGroups(PLAYER1);
        lookupTable = Database.getLookupTable();

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
        public void makeStateGroups() {
            State s = states.get(0);
            HashSet<Literal> minSet = new HashSet<>();
            for (Literal l : s.getAllLiterals())
                minSet.add(new Literal(l));

            for (Literal l : s.getAllLiterals()) {
                minSet.remove(l);
                State newState = (State) FFTManager.preconsToState.apply(minSet, PLAYER1);
                MinimaxPlay play = lookupTable.get(newState);
                if (play != null && play.move == this.move) { // Action unchanged when changing something. TODO - change instead of remove

                }
            }
        }
    }

}
