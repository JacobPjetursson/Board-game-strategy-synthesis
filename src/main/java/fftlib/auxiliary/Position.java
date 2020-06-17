package fftlib.auxiliary;

import fftlib.FFTManager;
import misc.Config;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

// Is useful for all position-based games
// team is the occupation of the position, e.g. cross or nought for tic-tac-toe
public class Position {
    // rotation/reflection of matrices
    public static final int OCC_PLAYER1 = 1;
    public static final int OCC_PLAYER2 = 2;

    public int row, col, occ;

    // Hashcode for row, col, occ to speed up the hashCode() function
    private static HashMap<Integer, Long> rowMap = new HashMap<>();
    private static HashMap<Integer, Long> colMap = new HashMap<>();
    private static HashMap<Integer, Long> occMap = new HashMap<>();


    static {
        long range = Long.MAX_VALUE;
        Random r = new Random();
        for (int i = 0; i < FFTManager.gameBoardHeight; i++) {
            long rLong = (long) (r.nextDouble() * range);
            rowMap.put(i, rLong);
        }
        for (int i = 0; i < FFTManager.gameBoardWidth; i++) {
            long rLong = (long) (r.nextDouble() * range);
            colMap.put(i, rLong);
        }

        for (int i = 0; i < 3; i++) { // occ
            long rLong = (long) (r.nextDouble() * range);
            occMap.put(i, rLong);
        }
    }

    public Position(int row, int col, int occ) {
        this.row = row;
        this.col = col;
        this.occ = occ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position that = (Position) o;
        return row == that.row &&
                col == that.col &&
                occ == that.occ;
    }

    @Override
    public int hashCode() {
        return (int) (rowMap.get(row) ^ colMap.get(col) ^ occMap.get(occ));
    }

    @Override
    public String toString() {
        return "Row: " + row + " , Col: " + col + " , Occ: " + occ;
    }
}