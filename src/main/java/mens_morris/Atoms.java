package mens_morris;

import fftlib.auxiliary.Position;

import java.util.ArrayList;
import java.util.HashMap;

import static misc.Config.THREE_MENS;

public class Atoms {
    public static HashMap<Integer, String> idToString;
    public static HashMap<String, Integer> stringToId;
    public static HashMap<Integer, Position> idToPos;
    public static HashMap<Position, Integer> posToId;
    public static ArrayList<Integer> gameAtoms;

    static {
        stringToId = new HashMap<>();
        idToString = new HashMap<>();
        idToPos = new HashMap<>();
        posToId = new HashMap<>();
        gameAtoms = new ArrayList<>();

        int counter = 1;
        String s;
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

                s = String.format("B(%s, %s)", i, j);
                gameAtoms.add(counter);
                stringToId.put(s, counter);
                idToPos.put(counter, new Position(i, j, 3));
                posToId.put(new Position(i, j, 3), counter);
                idToString.put(counter++, s);
            }
        }
    }
}
