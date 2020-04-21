package sim;

import fftlib.auxiliary.Position;

import java.util.ArrayList;
import java.util.HashMap;

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
        int counter = 0;
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
            }
        }
    }

    public static ArrayList<Integer> getGameAtoms() {
        return gameAtoms;
    }
}
