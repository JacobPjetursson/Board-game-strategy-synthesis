package sim;

import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.auxiliary.Position;
import fftlib.logic.LiteralSet;
import misc.Config;

import java.util.*;

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

        // Make ids random
        Random random = new Random();
        if (!Config.RANDOM_SEED)
            random.setSeed(Config.SEED);
        LinkedList<Integer> ids = new LinkedList<>();
        for (int i = 1; i <= 30; i++)
            ids.add(i);
        Collections.shuffle(ids, random);

        int id;
        String s;
        for (int i = 0; i < 6; i++) {
            for (int j = i+1; j < 6; j++) {
                id = ids.pop();
                s = String.format("P1(%s, %s)", i, j);
                gameAtoms.add(id);
                stringToId.put(s, id);
                idToPos.put(id, new Position(i, j, 1));
                posToId.put(new Position(i, j, 1), id);
                idToString.put(id, s);

                id = ids.pop();
                s = String.format("P2(%s, %s)", i, j);
                gameAtoms.add(id);
                stringToId.put(s, id);
                idToPos.put(id, new Position(i, j, 2));
                posToId.put(new Position(i, j, 2), id);
                idToString.put(id, s);


                LiteralSet actionPrecons = new LiteralSet();
                actionPrecons.add(new Literal(String.format("!P1(%s, %s)", i, j)));
                actionPrecons.add(new Literal(String.format("!P2(%s, %s)", i, j)));

                Action p1 = new Action(String.format("P1(%s, %s)", i, j));
                Action p2 = new Action(String.format("P2(%s, %s)", i, j));
                actionToPrecons.put(p1, actionPrecons);
                actionToPrecons.put(p2, actionPrecons);
            }
        }
    }
}
