package FFT;

import game.Move;
import misc.Config;

import java.util.ArrayList;

public class Action {
    protected ArrayList<Clause> addClauses;
    ArrayList<Clause> remClauses;
    protected boolean actionErr;

    protected Action(ArrayList<String> clauses) {
        this.addClauses = new ArrayList<>();
        this.remClauses = new ArrayList<>();

        for (String c : clauses) {
            if (c.startsWith("+") && Character.isDigit(c.charAt(1))) {
                c = c.substring(1);
                addClauses.add(new Clause(c));
            }
            else if (c.startsWith("-") && Character.isDigit(c.charAt(1))) {
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
        if (addClauses.size() > 1 || remClauses.size() > 1) {
            System.err.println("Only moves with a single add clause and/or a single remove clause is allowed in this game");
            actionErr = true;
        }
    }

    protected Action(ArrayList<Clause> addClauses, ArrayList<Clause> remClauses) {
        this.addClauses = new ArrayList<>(addClauses);
        this.remClauses = new ArrayList<>(remClauses);
    }

    // Kulibrat specific
    public Move getMove() {
        int newRow = -1; int newCol = -1; int oldRow = -1; int oldCol = -1;

        for (Clause c : addClauses) {
            newRow = c.row;
            newCol = c.col;
        }
        for (Clause c : remClauses) {
            oldRow = c.row;
            oldCol = c.col;
        }
        return new Move(oldRow, oldCol, newRow, newCol, -1);
    }

    public Action applySymmetry(int symmetry) {
        switch(symmetry) {
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
        int[][] refH = new int[Config.bHeight][Config.bWidth];

        // Reflect
        for (int i = 0; i < cBoard.length; i++) {
            for (int j = 0; j < cBoard[i].length; j++) {
                refH[i][j] = cBoard[i][cBoard[i].length - 1 - j];
            }
        }
        addClauseBoardToList(refH, rAddClauses, rRemClauses);

        return new Action(rAddClauses, rRemClauses);
    }

    public int[][] makeClauseBoard(ArrayList<Clause> addClauses, ArrayList<Clause> remClauses) {
        int[][] clauseBoard = new int[Config.bHeight][Config.bWidth];
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
        for(int i = 0; i < cb.length; i++) {
            for (int j = 0; j < cb[i].length; j++) {
                int val = cb[i][j];
                if(val == -Clause.PIECEOCC_NONE)
                    remClauses.add(new Clause(i, j, Clause.PIECEOCC_NONE, false));
                else if (val == Clause.PIECEOCC_NONE)
                    addClauses.add(new Clause(i, j, Clause.PIECEOCC_NONE, false));
            }
        }
    }
}
