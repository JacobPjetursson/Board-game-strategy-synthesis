package fftlib.game;

import fftlib.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static fftlib.FFTManager.*;

public class Transform {

    // LEVEL OF SYMMETRIES
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

    public static HashSet<SymmetryRule> getSymmetryRules(int[] transformations, Rule rule) {
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
        HashSet<SymmetryRule> symmetryRules = new HashSet<>();
        int [][] pBoard = preconsToBoard(rule.preconditions);
        int [][] aBoard = actionToBoard(rule.action);
        ArrayList<int[][]> pBoards = applyAll(refH, refV, rotate, pBoard);
        ArrayList<int[][]> aBoards = applyAll(refH, refV, rotate, aBoard);
        for (int i = 0; i < pBoards.size(); i++) {
            HashSet<Literal> precons = boardToPrecons(pBoards.get(i));
            Action action = boardToAction(aBoards.get(i));
            symmetryRules.add(new SymmetryRule(precons, action));
        }

        return symmetryRules;
    }

    private static ArrayList<int[][]> applyAll(boolean refH, boolean refV, boolean rotate, int[][] board) {
        ArrayList<int[][]> boards = new ArrayList<>(reflectAll(refH, refV, board));
        int[][] copy = copyArray(board);
        if (rotate) {
            // Rotate 3 times
            for (int i = 0; i < 3; i++) {
                copy = rotate(copy);
                boards.addAll(reflectAll(refH, refV, copy));
            }
        }
        return boards;
    }

    private static ArrayList<int[][]> reflectAll(boolean refH, boolean refV, int[][] board) {
        ArrayList<int[][]> reflectedRules = new ArrayList<>();
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
    public static int[][] preconsToBoard(HashSet<Literal> literals) {
        int height = FFTManager.gameBoardHeight;
        int width = FFTManager.gameBoardWidth;
        int[][] preconBoard = new int[height][width];

        for (Literal l : literals) {
            Position pos = getPosFromId.apply(l.id);
            if (pos != null) {
                preconBoard[pos.row][pos.col] = l.negation ? -pos.occ : pos.occ;
            }
        }
        return preconBoard;
    }

    // Takes as input copies of the add and remove lists and removes all the literals that are board placements
    // It returns a board with the literals on it, the value equals to the piece occ.
    private static int[][] actionToBoard(Action action) {
        int[][] literalBoard = new int[gameBoardHeight][gameBoardWidth];

        for (Literal l : action.adds) {
            Position pos = getPosFromId.apply(l.id);
            if (pos != null)
                literalBoard[pos.row][pos.col] = pos.occ;
        }
        for (Literal l : action.rems) {
            Position pos = getPosFromId.apply(l.id);
            if (pos != null)
                literalBoard[pos.row][pos.col] = -pos.occ;
        }

        return literalBoard;
    }

    // returns preconditions derived from transformed integer matrix and non-boardplacement literals
    private static HashSet<Literal> boardToPrecons(int[][] board) {
        HashSet<Literal> literals = new HashSet<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {

                Position pos = new Position(i, j, board[i][j]);
                if (pos.occ < 0)
                    literals.add(new Literal(getIdFromPos.apply(pos), true));
                else if (pos.occ > 0)
                    literals.add(new Literal(getIdFromPos.apply(pos), false));
            }
        }
        return literals;
    }

    // Takes empty addClause and remClause lists (except for constants),
    // and fill them up with the rotated pieces from the cb array.
    private static Action boardToAction(int[][] lb) {
        HashSet<Literal> adds = new HashSet<>();
        HashSet<Literal> rems = new HashSet<>();
        // Add back to list
        for (int i = 0; i < lb.length; i++) {
            for (int j = 0; j < lb[i].length; j++) {
                if (lb[i][j] == 0) continue;
                Position pos = new Position(i, j, lb[i][j]);
                Literal l = new Literal(getIdFromPos.apply(pos), false);
                if (pos.occ < 0)
                    rems.add(l);
                else if (pos.occ > 0)
                    adds.add(l);
            }
        }
        return new Action(adds, rems);
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

            HashSet<Literal> precons = new HashSet<>();
            Action action = null;
            for (Literal lit : rule.action.adds) {
                Position pos = getPosFromId.apply(lit.id);
                int n1 = arr[pos.row];
                int n2 = arr[pos.col];
                if (Math.abs(n1) > Math.abs(n2)) { // enforce lowest number first
                    int temp = n1;
                    n1 = n2;
                    n2 = temp;
                }
                Position newPos = new Position(n1, n2, pos.occ);
                action = new Action(getIdFromPos.apply(newPos));
            }
            for (Literal lit : rule.preconditions) {
                Position pos = getPosFromId.apply(lit.id);
                int n1 = arr[pos.row];
                int n2 = arr[pos.col];
                if (Math.abs(n1) > Math.abs(n2)) { // enforce lowest number first
                    int temp = n1;
                    n1 = n2;
                    n2 = temp;
                }
                Position newPos = new Position(n1, n2, pos.occ);
                precons.add(new Literal(getIdFromPos.apply(newPos), lit.negation));
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
