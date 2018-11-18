package fftlib;

import fftlib.game.FFTMove;
import misc.Config;
import java.util.ArrayList;
import java.util.Objects;

public class Action {
    public ArrayList<Clause> addClauses;
    protected boolean actionErr;
    public ArrayList<Clause> remClauses;

    protected Action(ArrayList<String> clauses) {
        this.addClauses = new ArrayList<>();
        this.remClauses = new ArrayList<>();

        for (String c : clauses) {
            if (c.startsWith("+") && Character.isDigit(c.charAt(1))) {
                c = c.substring(1);
                addClauses.add(new Clause(c));
            } else if (c.startsWith("-") && Character.isDigit(c.charAt(1))) {
                c = c.substring(1);
                remClauses.add(new Clause(c));
            } else {
                System.err.println("Invalid action format! Should be plus or minus, followed by row and column specification");
                actionErr = true;
                return;
            }
        }

        if (addClauses.isEmpty() && remClauses.isEmpty()) {
            System.err.println("Action clause list was empty");
            actionErr = true;
            return;
        }
        if (Config.CURRENT_GAME == Config.KULIBRAT &&
                (addClauses.size() > 1 || remClauses.size() > 1)) {
            System.err.println("Only moves with a single add clause and/or a single remove clause is allowed in this game");
            actionErr = true;
        }
    }

    protected Action(ArrayList<Clause> addClauses, ArrayList<Clause> remClauses) {
        this.addClauses = new ArrayList<>(addClauses);
        this.remClauses = new ArrayList<>(remClauses);
    }


    // TODO - do for all
    public Action applySymmetry(int symmetry) {
        switch (symmetry) {
            case Config.SYM_HREF:
                return reflectH();
            default:
                return this;
        }
    }

    private Action reflectH() {
        ArrayList<Clause> rAddClauses = new ArrayList<>(addClauses);
        ArrayList<Clause> rRemClauses = new ArrayList<>(remClauses);
        int[][] cBoard = makeClauseBoard(rAddClauses, rRemClauses);
        int[][] refH = new int[Config.getBoardHeight()][Config.getBoardWidth()];

        // Reflect
        for (int i = 0; i < cBoard.length; i++) {
            for (int j = 0; j < cBoard[i].length; j++) {
                refH[i][j] = cBoard[i][cBoard[i].length - 1 - j];
            }
        }
        addClauseBoardToList(refH, rAddClauses, rRemClauses);

        return new Action(rAddClauses, rRemClauses);
    }

    // TODO - find a better solution for below
    public FFTMove getMove() {
        switch(Config.CURRENT_GAME) {
            case Config.KULIBRAT:
                int newRow = -1;
                int newCol = -1;
                int oldRow = -1;
                int oldCol = -1;

                for (Clause c : addClauses) {
                    newRow = c.row;
                    newCol = c.col;
                }
                for (Clause c : remClauses) {
                    oldRow = c.row;
                    oldCol = c.col;
                }
                return new kulibrat.game.Move(oldRow, oldCol, newRow, newCol, -1);

            case Config.TICTACTOE:
                int row = -1;
                int col = -1;
                for (Clause c : addClauses) {
                    row = c.row;
                    col = c.col;
                }
                return new tictactoe.game.Move(row, col, -1);
        }
        System.err.println("Current game not found in config list");
        return null;
    }

    public int[][] makeClauseBoard(ArrayList<Clause> addClauses, ArrayList<Clause> remClauses) {
        int[][] clauseBoard = new int[Config.getBoardHeight()][Config.getBoardWidth()];
        // These clauses will be reflected/rotated
        ArrayList<Clause> addChangeClauses = new ArrayList<>();
        ArrayList<Clause> remChangeClauses = new ArrayList<>();

        for (Clause c : addClauses) {
            if (c.row != -1) {
                addChangeClauses.add(c);
                clauseBoard[c.row][c.col] = c.pieceOcc;
            }
        }
        addClauses.removeAll(addChangeClauses);
        for (Clause c : remClauses) {
            if (c.row != -1) {
                remChangeClauses.add(c);
                clauseBoard[c.row][c.col] = -c.pieceOcc;
            }
        }
        remClauses.removeAll(remChangeClauses);

        return clauseBoard;
    }

    public void addClauseBoardToList(int[][] cb, ArrayList<Clause> addClauses, ArrayList<Clause> remClauses) {
        // Add back to list
        for (int i = 0; i < cb.length; i++) {
            for (int j = 0; j < cb[i].length; j++) {
                int val = cb[i][j];
                if (val == -Clause.PIECEOCC_NONE)
                    remClauses.add(new Clause(i, j, Clause.PIECEOCC_NONE, false));
                else if (val == Clause.PIECEOCC_NONE)
                    addClauses.add(new Clause(i, j, Clause.PIECEOCC_NONE, false));
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Action)) return false;

        Action action = (Action) obj;
        return this == action ||
                (this.addClauses.equals(action.addClauses) && (this.remClauses.equals(action.remClauses)));
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(addClauses, remClauses);
        return 31 * hash;
    }
}
