package fftlib;

import java.util.Objects;



// Is useful for all position-based games
// team is the occupation of the position, e.g. cross or nought for tic-tac-toe
public class Position {
    // Zero is reserved for unfilled, e.g. when finding symmetric rules through
    // rotation/reflection of matrices
    public static final int OCC_PLAYER1 = 1;
    public static final int OCC_PLAYER2 = 2;
    public static final int OCC_BLANK = 3;

    public int row, col, occ;

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
        return Objects.hash(row, col, occ);
    }

    @Override
    public String toString() {
        return "Row: " + row + " , Col: " + col + " , Occ: " + occ;
    }
}