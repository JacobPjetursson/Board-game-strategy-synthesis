package fftlib.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class Transform {

    // LEVEL OF SYMMETRIES
    public static final int TRANS_NONE = 0;
    public static final int TRANS_HREF = 1;
    public static final int TRANS_VREF = 2;
    public static final int TRANS_ROT = 3;

    public static class TransformedArray {
        public ArrayList<Integer> transformations;
        public int[][] board;

        TransformedArray(ArrayList<Integer> transformations, int[][] board) {
            this.transformations = new ArrayList<>(transformations);
            this.board = copyArray(board);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TransformedArray)) return false;

            TransformedArray arr = (TransformedArray) obj;
            if (this == arr) return true;
            return Arrays.deepEquals(this.board, arr.board);
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash += Arrays.deepHashCode(this.board);
            return 31 * hash;
        }
    }

    // reflect horizontally, with mirror along y-axis
    private static int[][] reflectH(int[][] board) {
        int[][] refH = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                refH[i][j] = board[i][board[i].length - 1 - j];
            }
        }
        return refH;
    }
    // reflect vertically, with mirror along x-axis
    private static int[][] reflectV(int[][] board) {
        int[][] refV = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                refV[i][j] = board[board.length - 1 - i][j];
            }
        }
        return refV;
    }

    // Rotate 90 degrees same direction as the clock
    private static int[][] rotate(int[][] board) {
        int[][] rot = new int[board[0].length][board.length];
        for (int i = 0; i < board[0].length; ++i) {
            for (int j = 0; j < board.length; ++j) {
                rot[i][j] = board[board.length - j - 1][i];
            }
        }
        return rot;
    }

    public static HashSet<TransformedArray> applyAll(int[] transformations, int[][] board) {
        ArrayList<Integer> tArray = new ArrayList<>();
        boolean rotate = false;
        boolean refH = false;
        boolean refV = false;
        for (int transformation : transformations) {
            if (transformation == TRANS_ROT)
                rotate = true;
            else if (transformation == TRANS_HREF)
                refH = true;
            else if (transformation == TRANS_VREF)
                refV = true;
        }
        HashSet<TransformedArray> tSet = new HashSet<>(reflectAll(refH, refV, tArray, board));
        int[][] copy = copyArray(board);
        if (rotate) {
            // Rotate 3 times
            for (int i = 0; i < 3; i++) {
                copy = rotate(copy);
                tArray.add(TRANS_ROT);
                tSet.addAll(reflectAll(refH, refV, tArray, copy));
            }
        }
        return tSet;
    }

    public static int[][] copyArray(int[][] arr) {
        int[][] copy = new int[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i++) {
            copy[i] = Arrays.copyOf(arr[i], arr[i].length);
        }
        return copy;
    }

    private static HashSet<TransformedArray> reflectAll(boolean refH, boolean refV, ArrayList<Integer> currTransformations, int[][] board) {
        HashSet<TransformedArray> tSet = new HashSet<>();
        int[][] copy = copyArray(board);
        ArrayList<Integer> transformations = new ArrayList<>(currTransformations);
        tSet.add(new TransformedArray(transformations, copy));
        if (refH) {
            transformations.add(TRANS_HREF);
            copy = reflectH(copy);
            tSet.add(new TransformedArray(transformations, copy));
        }
        if (refV) {
            transformations.add(TRANS_VREF);
            copy = reflectV(copy);
            tSet.add(new TransformedArray(transformations, copy));
        }
        return tSet;
    }

    public static int[][] apply(ArrayList<Integer> transformations, int[][] board) {
        int[][] copy = copyArray(board);
        for (int transformation : transformations) {
            switch (transformation) {
                case TRANS_HREF:
                    copy = reflectH(copy);
                    break;
                case TRANS_VREF:
                    copy = reflectV(copy);
                    break;
                case TRANS_ROT:
                    copy = rotate(copy);
                    break;
            }
        }
        return copy;
    }
}
