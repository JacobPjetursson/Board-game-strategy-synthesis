package mens_morris;

import fftlib.auxiliary.Position;
import fftlib.logic.LiteralSet;
import fftlib.logic.Literal;
import misc.Config;

import java.util.*;

import static misc.Config.THREE_MENS;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Atoms {
    public static HashMap<Integer, String> idToString;
    public static HashMap<String, Integer> stringToId;
    public static HashMap<Integer, Position> idToPos;
    public static HashMap<Position, Integer> posToId;
    public static HashMap<Literal, LiteralSet> addToPrecons; // precondition for adding piece
    public static HashMap<Literal, LiteralSet> remToPrecons; // precondition for removing piece
    public static ArrayList<Integer> gameAtoms;

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
        addToPrecons = new HashMap<>();
        remToPrecons = new HashMap<>();

        // Make ids random
        Random random = new Random();
        if (!Config.RANDOM_SEED)
            random.setSeed(Config.SEED);
        LinkedList<Integer> ids = new LinkedList<>();
        int max = (THREE_MENS) ? 40 : 70;
        for (int i = 1; i <= max; i++)
            ids.add(i);
        Collections.shuffle(ids, random);

        int id;
        String s;

        id = ids.pop();
        gameAtoms.add(id);
        stringToId.put("p1Turn", id);
        idToString.put(id, "p1Turn");

        id = ids.pop();
        gameAtoms.add(id);
        stringToId.put("!p1Turn", id);
        idToString.put(id, "!p1Turn");

        id = ids.pop();
        gameAtoms.add(id);
        stringToId.put("phase2", id);
        idToString.put(id, "phase2");

        id = ids.pop();
        gameAtoms.add(id);
        stringToId.put("!phase2", id);
        idToString.put(id, "!phase2");

        if (!THREE_MENS) {
            id = ids.pop();
            gameAtoms.add(id);
            stringToId.put("canRemove", id);
            idToString.put(id, "canRemove");

            id = ids.pop();
            gameAtoms.add(id);
            stringToId.put("!canRemove", id);
            idToString.put(id, "!canRemove");
        }

        for (int i = 0; i < Node.BOARD_SIZE; i++) {
            for (int j = 0; j < Node.BOARD_SIZE; j++) {
                if (!Node.validPos(i, j))
                    continue;
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

                // Make sure new position is empty
                LiteralSet addPrecons = new LiteralSet();
                addPrecons.add(new Literal(String.format("!P1(%s, %s)", i, j)));
                addPrecons.add(new Literal(String.format("!P2(%s, %s)", i, j)));

                Literal addP1 = new Literal(String.format("P1(%s, %s)", i, j));
                Literal addP2 = new Literal(String.format("P2(%s, %s)", i, j));
                addToPrecons.put(addP1, addPrecons);
                addToPrecons.put(addP2, addPrecons);

                LiteralSet remPrecons = new LiteralSet();
                Literal rem = new Literal(String.format("P1(%s, %s)", i, j));
                remPrecons.add(rem);
                remToPrecons.put(rem, remPrecons);

                remPrecons = new LiteralSet();
                rem = new Literal(String.format("P2(%s, %s)", i, j));
                remPrecons.add(rem);
                remToPrecons.put(rem, remPrecons);
            }
        }
    }
}
