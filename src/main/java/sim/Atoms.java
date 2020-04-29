package sim;

import fftlib.Action;
import fftlib.Literal;
import fftlib.auxiliary.Position;
import fftlib.game.LiteralSet;

import java.util.ArrayList;
import java.util.HashMap;

public class Atoms {
    public static HashMap<Integer, String> idToString;
    public static HashMap<String, Integer> stringToId;
    public static HashMap<Integer, Position> idToPos;
    public static HashMap<Position, Integer> posToId;
    public static ArrayList<Integer> gameAtoms;
    public static HashMap<Action, LiteralSet> actionToPrecons;

    static {
        stringToId = new HashMap<>();
        idToString = new HashMap<>();
        idToPos = new HashMap<>();
        posToId = new HashMap<>();
        gameAtoms = new ArrayList<>();
        actionToPrecons = new HashMap<>();

        int counter = 1;
        String s;
        for (int i = 0; i < 6; i++) {
            for (int j = i+1; j < 6; j++) {
                s = String.format("P1(%s, %s)", i, j);
                gameAtoms.add(counter);
                stringToId.put(s, counter);
                idToPos.put(counter, new Position(i, j, 1));
                posToId.put(new Position(i, j, 1), counter);
                idToString.put(counter++, s);

                s = String.format("P2(%s, %s)", i, j);
                gameAtoms.add(counter);
                stringToId.put(s, counter);
                idToPos.put(counter, new Position(i, j, 2));
                posToId.put(new Position(i, j, 2), counter);
                idToString.put(counter++, s);

                s = String.format("B(%s, %s)", i, j);
                gameAtoms.add(counter);
                stringToId.put(s, counter);
                idToPos.put(counter, new Position(i, j, 3));
                posToId.put(new Position(i, j, 3), counter);
                idToString.put(counter++, s);


                Action p1 = new Action(String.format("P1(%s, %s)", i, j));
                Action p2 = new Action(String.format("P2(%s, %s)", i, j));
                String actionPrecon = String.format("B(%s, %s)", i, j);
                actionToPrecons.put(p1, new LiteralSet(new Literal(actionPrecon)));

                actionToPrecons.put(p2, new LiteralSet(new Literal(actionPrecon)));
            }
        }
    }
}
