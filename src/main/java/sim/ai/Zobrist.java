package sim.ai;

import misc.Config;
import misc.Globals;

import java.util.Random;

public class Zobrist {
    public static long[][][] points;
    public static long[] turn;

    static {
        Random r = new Random();
        long range = Long.MAX_VALUE;
        int seed = (Globals.ENABLE_AUTOGEN && Config.RANDOM_SEED) ? r.nextInt() : Config.SEED;
        r.setSeed(seed);
        System.out.println("Zobrist seed: " + seed);

        // line keys
        int width = 2;
        int height = 3;
        int colors = 3;


        points = new long[height][width][colors];
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[j].length; j++) {
                for (int k = 0; k < colors; k++) {
                    long rLong = (long) (r.nextDouble() * range);
                    points[i][j][k] = rLong;
                }
            }
        }
        // turn keys
        turn = new long[3];
        for (int i = 0; i < turn.length; i++) {
            turn[i] = (long) (r.nextDouble() * range);
        }
    }
}
