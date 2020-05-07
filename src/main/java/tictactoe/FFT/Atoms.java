package tictactoe.FFT;

import fftlib.FFTManager;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.auxiliary.Position;
import fftlib.game.LiteralSet;
import misc.Config;

import java.util.*;

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

        // Make ids random
        Random random = new Random();
        if (!Config.RANDOM_SEED)
            random.setSeed(Config.SEED);
        LinkedList<Integer> ids = new LinkedList<>();
        for (int i = 1; i <= 18; i++)
            ids.add(i);
        Collections.shuffle(ids, random);

        int id;
        String s;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                id = ids.pop();
                s = String.format("P1(%s, %s)", i, j);
                gameAtoms.add(id);
                stringToId.put(s, id);
                idToString.put(id, s);
                idToPos.put(id, new Position(i, j, 1));
                posToId.put(new Position(i, j, 1), id);

                id = ids.pop();
                s = String.format("P2(%s, %s)", i, j);
                gameAtoms.add(id);
                stringToId.put(s, id);
                idToString.put(id, s);
                idToPos.put(id, new Position(i, j, 2));
                posToId.put(new Position(i, j, 2), id);

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
