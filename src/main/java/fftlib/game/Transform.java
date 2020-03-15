package fftlib.game;

import fftlib.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static fftlib.FFTManager.gameBoardHeight;
import static fftlib.FFTManager.gameBoardWidth;
import static kulibrat.game.Logic.POS_NONBOARD;

public class Transform {

    // LEVEL OF SYMMETRIES
    public static final int TRANS_NONE = 0;
    public static final int TRANS_HREF = 1;
    public static final int TRANS_VREF = 2;
    public static final int TRANS_ROT = 3;


    // reflect horizontally, with mirror along y-axis
    private static int[][] reflectH(int[][] board) {
        int[][] refH = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[i].length; j++)
                refH[i][j] = board[i][board[i].length - 1 - j];
        return refH;
    }
    // reflect vertically, with mirror along x-axis
    private static int[][] reflectV(int[][] board) {
        int[][] refV = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[i].length; j++)
                refV[i][j] = board[board.length - 1 - i][j];
        return refV;
    }

    // Rotate 90 degrees clockwise
    private static int[][] rotate(int[][] board) {
        int[][] rot = new int[board[0].length][board.length];
        for (int i = 0; i < board[0].length; ++i)
            for (int j = 0; j < board.length; ++j)
                rot[i][j] = board[board.length - j - 1][i];
        return rot;
    }

    public static HashSet<int[][]> applyAll(int[] transformations, int[][] board) {
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
        HashSet<int[][]> symmetryBoards = new HashSet<>(reflectAll(refH, refV, board));
        int[][] copy = copyArray(board);
        if (rotate) {
            // Rotate 3 times
            for (int i = 0; i < 3; i++) {
                copy = rotate(copy);
                symmetryBoards.addAll(reflectAll(refH, refV, copy));
            }
        }
        return symmetryBoards;
    }

    private static HashSet<int[][]> reflectAll(boolean refH, boolean refV, int[][] board) {
        HashSet<int[][]> reflectedRules = new HashSet<>();
        int[][] copy = copyArray(board);
        reflectedRules.add(copyArray(copy));
        if (refH) {
            copy = reflectH(copy);
            reflectedRules.add(copyArray(copy));
        }
        if (refV) {
            copy = reflectV(copy);
            reflectedRules.add(copyArray(copy));
        }
        return reflectedRules;
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

    // Returns a board with the literals on it, the value equals to the piece occ.
    public static int[][] preconsToBoard(Clause clause) {
        int height = FFTManager.gameBoardHeight;
        int width = FFTManager.gameBoardWidth;
        int[][] preconBoard = new int[height][width];

        for (Literal l : clause.literals) {
            if (l.boardPlacement && l.row >= 0 && l.col >= 0 && l.row <= height-1 && l.col <= width-1) {
                if (l.negation)
                    preconBoard[l.row][l.col] = -l.pieceOcc;
                else
                    preconBoard[l.row][l.col] = l.pieceOcc;
            }
        }
        return preconBoard;
    }

    // Takes as input copies of the addClause, remClause lists and removes all the literals that are board placements
    // It returns a board with the literals on it, the value equals to the piece occ.
    private static int[][] actionToBoard(Action action) {
        int[][] clauseBoard = new int[gameBoardHeight][gameBoardWidth];

        for (Literal l : action.addClause.literals) {
            if (l.row != POS_NONBOARD)
                clauseBoard[l.row][l.col] = l.pieceOcc;
        }
        for (Literal l : action.remClause.literals) {
            if (l.row != POS_NONBOARD)
                clauseBoard[l.row][l.col] = -l.pieceOcc;
        }

        return clauseBoard;
    }

    // returns preconditions derived from transformed integer matrix and non-boardplacement literals
    private static Clause boardToPrecons(int[][] board) {
        HashSet<Literal> literals = new HashSet<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int val = board[i][j];
                if (val < 0)
                    literals.add(new Literal(i, j, -val, true));
                else if (val > 0)
                    literals.add(new Literal(i, j, val, false));
            }
        }
        return new Clause(literals);
    }

    // Takes empty addClause and remClause lists (except for constants),
    // and fill them up with the rotated pieces from the cb array.
    private static Action boardToAction(int[][] cb) {
        HashSet<Literal> addLits = new HashSet<>();
        HashSet<Literal> remLits = new HashSet<>();
        // Add back to list
        for (int i = 0; i < cb.length; i++) {
            for (int j = 0; j < cb[i].length; j++) {
                int val = cb[i][j];
                if (val < 0)
                    remLits.add(new Literal(i, j, val, false));
                else if (val > 0)
                    addLits.add(new Literal(i, j, val, false));
            }
        }
        Clause addC = new Clause(addLits);
        Clause remC = new Clause(remLits);
        return new Action(addC, remC);
    }

    public static int[][] copyArray(int[][] arr) {
        int[][] copy = new int[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i++)
            copy[i] = Arrays.copyOf(arr[i], arr[i].length);
        return copy;
    }

    public static HashSet<SymmetryRule> findAutomorphisms(Rule rule) {
        int [] vertices = new int[gameBoardHeight];
        for (int i = 0; i < gameBoardHeight; i++) {
            vertices[i] = i;
        }
        ArrayList<int[]> permutations = findPermutations(vertices);
        HashSet<SymmetryRule> transformations = new HashSet<>();
        for(int[] arr : permutations) {

            Clause precons = new Clause();
            Action action = null;
            for (Literal lit : rule.action.addClause.literals) {
                action = new Action(arr[lit.row], arr[lit.col], lit.pieceOcc, lit.negation);
            }
            for (Literal lit : rule.preconditions.literals) {
                precons.add(new Literal(arr[lit.row], arr[lit.col], lit.pieceOcc, lit.negation));
            }
            transformations.add(new SymmetryRule(precons, action));
        }
        return transformations;
    }

    public static ArrayList<int[]> findPermutations(int[] elements) {
        int[] indexes = new int[elements.length];
        ArrayList<int[]> permutations = new ArrayList<>();
        permutations.add(elements.clone());

        int i = 0;
        while (i < elements.length) {
            if (indexes[i] < i) {
                swap(elements, i % 2 == 0 ?  0: indexes[i], i);
                permutations.add(elements.clone());
                indexes[i]++;
                i = 0;
            }
            else {
                indexes[i] = 0;
                i++;
            }
        }
        return permutations;
    }

    private static void swap(int[] input, int a, int b) {
        int tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }
}
