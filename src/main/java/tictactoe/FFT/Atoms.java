package tictactoe.FFT;

import fftlib.Action;
import fftlib.Literal;
import fftlib.auxiliary.Position;
import fftlib.game.LiteralSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Atoms {

    public static ArrayList<Integer> gameAtoms;
    public static HashMap<Integer, String> idToString;
    public static HashMap<String, Integer> stringToId;
    public static HashMap<Integer, Position> idToPos;
    public static HashMap<Position, Integer> posToId;
    public static HashMap<Action, LiteralSet> actionToPrecons;

    static {
        stringToId = new HashMap<>();
        idToString = new HashMap<>();
        idToPos = new HashMap<>();
        posToId = new HashMap<>();
        gameAtoms = new ArrayList<>();
        actionToPrecons = new HashMap<>();

        int counter = 0;
        String s;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                s = String.format("P1(%s, %s)", i, j);
                gameAtoms.add(counter);
                stringToId.put(s, counter);
                idToString.put(counter, s);
                idToPos.put(counter, new Position(i, j, 1));
                posToId.put(new Position(i, j, 1), counter++);

                s = String.format("P2(%s, %s)", i, j);
                gameAtoms.add(counter);
                stringToId.put(s, counter);
                idToString.put(counter, s);
                idToPos.put(counter, new Position(i, j, 2));
                posToId.put(new Position(i, j, 2), counter++);

                Action action = new Action(String.format("P1(%s, %s)", i, j));
                LiteralSet actionPrecons = new LiteralSet();
                actionPrecons.add(new Literal(String.format("!P1(%s, %s)", i, j)));
                actionPrecons.add(new Literal(String.format("!P2(%s, %s)", i, j)));
                actionToPrecons.put(action, actionPrecons);

                action = new Action(String.format("P2(%s, %s)", i, j));
                actionToPrecons.put(action, actionPrecons);
            }
        }
    }
}
