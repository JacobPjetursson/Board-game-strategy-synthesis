package tictactoe.FFT;

import fftlib.auxiliary.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Atoms {
    public static int P1_0_0 = 0;
    public static int P2_0_0 = 1;
    public static int B_0_0 = 2;

    public static int P1_0_1 = 3;
    public static int P2_0_1 = 4;
    public static int B_0_1 = 5;

    public static int P1_0_2 = 6;
    public static int P2_0_2 = 7;
    public static int B_0_2 = 8;

    public static int P1_1_0 = 9;
    public static int P2_1_0 = 10;
    public static int B_1_0 = 11;

    public static int P1_1_1 = 12;
    public static int P2_1_1 = 13;
    public static int B_1_1 = 14;

    public static int P1_1_2 = 15;
    public static int P2_1_2 = 16;
    public static int B_1_2 = 17;

    public static int P1_2_0 = 18;
    public static int P2_2_0 = 19;
    public static int B_2_0 = 20;

    public static int P1_2_1 = 21;
    public static int P2_2_1 = 22;
    public static int B_2_1 = 23;

    public static int P1_2_2 = 24;
    public static int P2_2_2 = 25;
    public static int B_2_2 = 26;

    public static HashMap<Integer, String> idToString;
    public static HashMap<String, Integer> stringToId;
    public static HashMap<Integer, Position> idToPos;
    public static HashMap<Position, Integer> posToId;

    static {
        idToString = new HashMap<>();
        idToString.put(P1_0_0, "P1(0, 0)");
        idToString.put(P2_0_0, "P2(0, 0)");
        idToString.put(B_0_0, "B(0, 0)");

        idToString.put(P1_0_1, "P1(0, 1)");
        idToString.put(P2_0_1, "P2(0, 1)");
        idToString.put(B_0_1, "B(0, 1)");

        idToString.put(P1_0_2, "P1(0, 2)");
        idToString.put(P2_0_2, "P2(0, 2)");
        idToString.put(B_0_2, "B(0, 2)");

        idToString.put(P1_1_0, "P1(1, 0)");
        idToString.put(P2_1_0, "P2(1, 0)");
        idToString.put(B_1_0, "B(1, 0)");

        idToString.put(P1_1_1, "P1(1, 1)");
        idToString.put(P2_1_1, "P2(1, 1)");
        idToString.put(B_1_1, "B(1, 1)");

        idToString.put(P1_1_2, "P1(1, 2)");
        idToString.put(P2_1_2, "P2(1, 2)");
        idToString.put(B_1_2, "B(1, 2)");

        idToString.put(P1_2_0, "P1(2, 0)");
        idToString.put(P2_2_0, "P2(2, 0)");
        idToString.put(B_2_0, "B(2, 0)");

        idToString.put(P1_2_1, "P1(2, 1)");
        idToString.put(P2_2_1, "P2(2, 1)");
        idToString.put(B_2_1, "B(2, 1)");

        idToString.put(P1_2_2, "P1(2, 2)");
        idToString.put(P2_2_2, "P2(2, 2)");
        idToString.put(B_2_2, "B(2, 2)");

        stringToId = new HashMap<>();
        stringToId.put("P1(0, 0)", P1_0_0);
        stringToId.put("P2(0, 0)", P2_0_0);
        stringToId.put("B(0, 0)", B_0_0);

        stringToId.put("P1(0, 1)", P1_0_1);
        stringToId.put("P2(0, 1)", P2_0_1);
        stringToId.put("B(0, 1)", B_0_1);

        stringToId.put("P1(0, 2)", P1_0_2);
        stringToId.put("P2(0, 2)", P2_0_2);
        stringToId.put("B(0, 2)", B_0_2);

        stringToId.put("P1(1, 0)", P1_1_0);
        stringToId.put("P2(1, 0)", P2_1_0);
        stringToId.put("B(1, 0)", B_1_0);

        stringToId.put("P1(1, 1)", P1_1_1);
        stringToId.put("P2(1, 1)", P2_1_1);
        stringToId.put("B(1, 1)", B_1_1);

        stringToId.put("P1(1, 2)", P1_1_2);
        stringToId.put("P2(1, 2)", P2_1_2);
        stringToId.put("B(1, 2)", B_1_2);

        stringToId.put("P1(2, 0)", P1_2_0);
        stringToId.put("P2(2, 0)", P2_2_0);
        stringToId.put("B(2, 0)", B_2_0);

        stringToId.put("P1(2, 1)", P1_2_1);
        stringToId.put("P2(2, 1)", P2_2_1);
        stringToId.put("B(2, 1)", B_2_1);

        stringToId.put("P1(2, 2)", P1_2_2);
        stringToId.put("P2(2, 2)", P2_2_2);
        stringToId.put("B(2, 2)", B_2_2);

        idToPos = new HashMap<>();
        idToPos.put(P1_0_0, new Position(0, 0, 1));
        idToPos.put(P2_0_0, new Position(0, 0, 2));
        idToPos.put(B_0_0, new Position(0, 0, 3));

        idToPos.put(P1_0_1, new Position(0, 1, 1));
        idToPos.put(P2_0_1, new Position(0, 1, 2));
        idToPos.put(B_0_1, new Position(0, 1, 3));

        idToPos.put(P1_0_2, new Position(0, 2, 1));
        idToPos.put(P2_0_2, new Position(0, 2, 2));
        idToPos.put(B_0_2, new Position(0, 2, 3));

        idToPos.put(P1_1_0, new Position(1, 0, 1));
        idToPos.put(P2_1_0, new Position(1, 0, 2));
        idToPos.put(B_1_0, new Position(1, 0, 3));

        idToPos.put(P1_1_1, new Position(1, 1, 1));
        idToPos.put(P2_1_1, new Position(1, 1, 2));
        idToPos.put(B_1_1, new Position(1, 1, 3));

        idToPos.put(P1_1_2, new Position(1, 2, 1));
        idToPos.put(P2_1_2, new Position(1, 2, 2));
        idToPos.put(B_1_2, new Position(1, 2, 3));

        idToPos.put(P1_2_0, new Position(2, 0, 1));
        idToPos.put(P2_2_0, new Position(2, 0, 2));
        idToPos.put(B_2_0, new Position(2, 0, 3));

        idToPos.put(P1_2_1, new Position(2, 1, 1));
        idToPos.put(P2_2_1, new Position(2, 1, 2));
        idToPos.put(B_2_1, new Position(2, 1, 3));

        idToPos.put(P1_2_2, new Position(2, 2, 1));
        idToPos.put(P2_2_2, new Position(2, 2, 2));
        idToPos.put(B_2_2, new Position(2, 2, 3));

        posToId = new HashMap<>();
        posToId.put(new Position(0, 0, 1), P1_0_0);
        posToId.put(new Position(0, 0, 2), P2_0_0);
        posToId.put(new Position(0, 0, 3), B_0_0);

        posToId.put(new Position(0, 1, 1), P1_0_1);
        posToId.put(new Position(0, 1, 2), P2_0_1);
        posToId.put(new Position(0, 1, 3), B_0_1);

        posToId.put(new Position(0, 2, 1), P1_0_2);
        posToId.put(new Position(0, 2, 2), P2_0_2);
        posToId.put(new Position(0, 2, 3), B_0_2);

        posToId.put(new Position(1, 0, 1), P1_1_0);
        posToId.put(new Position(1, 0, 2), P2_1_0);
        posToId.put(new Position(1, 0, 3), B_1_0);

        posToId.put(new Position(1, 1, 1), P1_1_1);
        posToId.put(new Position(1, 1, 2), P2_1_1);
        posToId.put(new Position(1, 1, 3), B_1_1);

        posToId.put(new Position(1, 2, 1), P1_1_2);
        posToId.put(new Position(1, 2, 2), P2_1_2);
        posToId.put(new Position(1, 2, 3), B_1_2);

        posToId.put(new Position(2, 0, 1), P1_2_0);
        posToId.put(new Position(2, 0, 2), P2_2_0);
        posToId.put(new Position(2, 0, 3), B_2_0);

        posToId.put(new Position(2, 1, 1), P1_2_1);
        posToId.put(new Position(2, 1, 2), P2_2_1);
        posToId.put(new Position(2, 1, 3), B_2_1);

        posToId.put(new Position(2, 2, 1), P1_2_2);
        posToId.put(new Position(2, 2, 2), P2_2_2);
        posToId.put(new Position(2, 2, 3), B_2_2);
    }

    public static ArrayList<Integer> getGameAtoms() {
        return new ArrayList<>(List.of(P1_0_0, P2_0_0, B_0_0, P1_0_1, P2_0_1, B_0_1,
                          P1_0_2, P2_0_2, B_0_2, P1_1_0, P2_1_0, B_1_0,
                          P1_1_1, P2_1_1, B_1_1, P1_1_2, P2_1_2, B_1_2,
                          P1_2_0, P2_2_0, B_2_0, P1_2_1, P2_2_1, B_2_1,
                          P1_2_2, P2_2_2, B_2_2));
    }
}
