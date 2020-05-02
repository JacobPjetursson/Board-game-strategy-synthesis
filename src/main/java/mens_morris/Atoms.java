package mens_morris;

import fftlib.auxiliary.Position;
import fftlib.game.LiteralSet;
import fftlib.logic.Action;
import fftlib.logic.Literal;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Config.THREE_MENS;

public class Atoms {
    public static HashMap<Integer, String> idToString;
    public static HashMap<String, Integer> stringToId;
    public static HashMap<Integer, Position> idToPos;
    public static HashMap<Position, Integer> posToId;
    public static HashMap<Literal, LiteralSet> addToPrecons; // precondition for adding piece
    public static HashMap<Literal, LiteralSet> remToPrecons; // precondition for removing piece
    public static ArrayList<Integer> gameAtoms;

    static {
        stringToId = new HashMap<>();
        idToString = new HashMap<>();
        idToPos = new HashMap<>();
        posToId = new HashMap<>();
        gameAtoms = new ArrayList<>();
        addToPrecons = new HashMap<>();
        remToPrecons = new HashMap<>();

        int counter = 1;
        String s;

        gameAtoms.add(counter);
        stringToId.put("p1Turn", counter);
        idToString.put(counter++, "p1Turn");

        gameAtoms.add(counter);
        stringToId.put("phase2", counter);
        idToString.put(counter++, "phase2");
        if (!THREE_MENS) {
            gameAtoms.add(counter);
            stringToId.put("canRemove", counter);
            idToString.put(counter++, "canRemove");
        }
        for (int i = 0; i < Node.BOARD_SIZE; i++) {
            for (int j = 0; j < Node.BOARD_SIZE; j++) {
                if (!Node.validPos(i, j))
                    continue;
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
