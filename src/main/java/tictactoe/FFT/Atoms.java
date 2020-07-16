package tictactoe.FFT;

import fftlib.logic.rule.Action;
import fftlib.auxiliary.Position;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import misc.Config;

import java.util.*;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Atoms {

    public static ArrayList<Integer> gameAtoms;
    public static HashMap<Integer, String> idToString;
    public static HashMap<String, Integer> stringToId;
    public static HashMap<Integer, Position> idToPos;
    public static HashMap<Position, Integer> posToId;
    public static HashMap<Action, LiteralSet> actionToPrecons;

    public static void addToSets(int id, String name, int row, int col, int occ) {
        gameAtoms.add(id);
        stringToId.put(name, id);
        idToString.put(id, name);
        idToPos.put(id, new Position(row, col, occ));
        posToId.put(new Position(row, col, occ), id);
    }

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
        for (int i = 1; i <= 36; i++)
            ids.add(i);
        Collections.shuffle(ids, random);

        int id;
        String s;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                id = ids.pop();
                s = String.format("P1(%s, %s)", i, j);
                addToSets(id, s, i, j, PLAYER1);

                id = ids.pop();
                s = String.format("!P1(%s, %s)", i, j);
                addToSets(id, s, i, j, -PLAYER1);

                id = ids.pop();
                s = String.format("P2(%s, %s)", i, j);
                addToSets(id, s, i, j, PLAYER2);

                id = ids.pop();
                s = String.format("!P2(%s, %s)", i, j);
                addToSets(id, s, i, j, -PLAYER2);

                LiteralSet actionPrecons = new LiteralSet();
                actionPrecons.add(new PropLiteral(String.format("!P1(%s, %s)", i, j)));
                actionPrecons.add(new PropLiteral(String.format("!P2(%s, %s)", i, j)));

                Action p1 = new Action(String.format("P1(%s, %s)", i, j));
                Action p2 = new Action(String.format("P2(%s, %s)", i, j));
                actionToPrecons.put(p1, actionPrecons);
                actionToPrecons.put(p2, actionPrecons);
            }
        }
    }
}
