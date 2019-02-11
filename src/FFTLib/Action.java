package fftlib;

import fftlib.game.FFTMove;
import misc.Config;
import java.util.ArrayList;
import java.util.Objects;


public class Action {
    public Clause addClause;
    public Clause remClause;
    boolean actionErr;

    Action() {
        this.addClause = new Clause();
        this.remClause = new Clause();
    }

    Action(ArrayList<String> literals) {
        this.addClause = new Clause();
        this.remClause = new Clause();

        for (String l : literals) {
            if (l.startsWith("+") && Character.isDigit(l.charAt(1))) {
                l = l.substring(1);
                addClause.add(new Literal(l));
            } else if (l.startsWith("-") && Character.isDigit(l.charAt(1))) {
                l = l.substring(1);
                remClause.add(new Literal(l));
            } else {
                System.err.println("Invalid action format! Should be plus or minus, followed by row and column specification");
                actionErr = true;
                return;
            }
        }

        if (addClause.isEmpty() && remClause.isEmpty()) {
            System.err.println("Action clause list was empty");
            actionErr = true;
            return;
        }
        if (Config.CURRENT_GAME == Config.KULIBRAT &&
                (addClause.size() > 1 || remClause.size() > 1)) {
            System.err.println("Only moves with a single add clause and/or a single remove clause is allowed in this game");
            actionErr = true;
        }
    }

    public Action(Clause addClause, Clause remClause) {
        this.addClause = new Clause(addClause);
        this.remClause = new Clause(remClause);
    }


    // TODO - do for all
    Action applySymmetry(int symmetry) {
        switch (symmetry) {
            case Config.SYM_HREF:
                return reflectH();
            default:
                return this;
        }
    }

    private Action reflectH() {
        Clause rotAddClause = new Clause(addClause);
        Clause rotRemClause = new Clause(remClause);
        int[][] cBoard = makeClauseBoard(rotAddClause, rotRemClause);
        int[][] refH = new int[FFTManager.gameBoardHeight][FFTManager.gameBoardWidth];

        // Reflect
        for (int i = 0; i < cBoard.length; i++) {
            for (int j = 0; j < cBoard[i].length; j++) {
                refH[i][j] = cBoard[i][cBoard[i].length - 1 - j];
            }
        }
        addClauseBoardToList(refH, rotAddClause, rotRemClause);

        return new Action(rotAddClause, rotRemClause);
    }

    public FFTMove getMove() {
        return FFTManager.actionToMove.apply(this);
    }

    private int[][] makeClauseBoard(Clause addClause, Clause remClause) {
        int[][] clauseBoard = new int[FFTManager.gameBoardHeight][FFTManager.gameBoardWidth];
        // These literals will be reflected/rotated
        ArrayList<Literal> addChangeLiterals = new ArrayList<>();
        ArrayList<Literal> remChangeLiterals = new ArrayList<>();

        for (Literal l : addClause.literals) {
            if (l.row != -1) {
                addChangeLiterals.add(l);
                clauseBoard[l.row][l.col] = l.pieceOcc;
            }
        }
        addClause.literals.removeAll(addChangeLiterals);
        for (Literal l : remClause.literals) {
            if (l.row != -1) {
                remChangeLiterals.add(l);
                clauseBoard[l.row][l.col] = -l.pieceOcc;
            }
        }
        remClause.literals.removeAll(remChangeLiterals);

        return clauseBoard;
    }

    String getFormattedString() {
        String actionMsg = "";
        for (Literal literal : addClause.literals) {
            if (!actionMsg.isEmpty())
                actionMsg += " ∧ ";
            actionMsg += "+" + literal.name;
        }
        for (Literal literal : remClause.literals) {
            if (!actionMsg.isEmpty())
                actionMsg += " ∧ ";
            actionMsg += "-" + literal.name;
        }
        return actionMsg;
    }

    private void addClauseBoardToList(int[][] cb, Clause addClause, Clause remClause) {
        // Add back to list
        for (int i = 0; i < cb.length; i++) {
            for (int j = 0; j < cb[i].length; j++) {
                int val = cb[i][j];
                if (val == -Literal.PIECEOCC_NONE)
                    remClause.add(new Literal(i, j, Literal.PIECEOCC_NONE, false));
                else if (val == Literal.PIECEOCC_NONE)
                    addClause.add(new Literal(i, j, Literal.PIECEOCC_NONE, false));
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Action)) return false;

        Action action = (Action) obj;
        return this == action ||
                (this.addClause.equals(action.addClause) && (this.remClause.equals(action.remClause)));
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(addClause, remClause);
        return 31 * hash;
    }
}
