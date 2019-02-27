package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.Transform;
import misc.Config;

import java.util.ArrayList;
import java.util.Objects;

import static fftlib.Literal.PIECEOCC_PLAYER;
import static kulibrat.game.Logic.POS_NONBOARD;


public class Action {
    public Clause addClause;
    public Clause remClause;
    public boolean actionErr;

    public Action() {
        this.addClause = new Clause();
        this.remClause = new Clause();
    }

    public Action(Action duplicate) {
        this.addClause = new Clause(duplicate.addClause);
        this.remClause = new Clause(duplicate.remClause);
        this.actionErr = duplicate.actionErr;
    }

    Action(ArrayList<String> literals) {
        this.addClause = new Clause();
        this.remClause = new Clause();

        for (String lStr : literals) {
            if (lStr.startsWith("+")) {
                lStr = lStr.substring(1);
                Literal l = new Literal(lStr);
                if (l.error) {
                    actionErr = true;
                    return;
                }
                addClause.add(l);
            } else if (lStr.startsWith("-")) {
                lStr = lStr.substring(1);
                Literal l = new Literal(lStr);
                if (l.error) {
                    actionErr = true;
                    return;
                }
                remClause.add(l);
            } else {
                System.err.println("Invalid action format! Literal should be prefixed with plus (+) or minus (-)");
                actionErr = true;
                return;
            }
        }

        if (addClause.isEmpty() && remClause.isEmpty()) {
            System.err.println("No action was specified");
            actionErr = true;
            return;
        }
        if (Config.CURRENT_GAME == Config.KULIBRAT) {
            if(addClause.size() > 1 || remClause.size() > 1) {
                System.err.println("Only moves with a single add literal and/or a single remove literal is allowed in this game");
                actionErr = true;
                return;
            }
            for (Literal l : addClause.literals) {
                if (l.pieceOcc != PIECEOCC_PLAYER) {
                    System.err.println("It is only allowed to move the player's own pieces in this game, i.e. P(x, y)");
                    actionErr = true;
                    return;
                }
            }
        }
    }

    public Action(Clause addClause, Clause remClause) {
        this.addClause = new Clause(addClause);
        this.remClause = new Clause(remClause);
    }

    Action transform(ArrayList<Integer> transformations) {
        int[][] aBoard = actionToBoard();
        int[][] tBoard = Transform.apply(transformations, aBoard);
        return boardToAction(transformations, tBoard);
    }

    public FFTMove getMove(int team) {
        return FFTManager.actionToMove.apply(this, team);
    }

    // Takes as input copies of the addClause, remClause lists and removes all the literals that are board placements
    // It returns a board with the literals on it, the value equals to the piece occ.
    private int[][] actionToBoard() {
        int[][] clauseBoard = new int[FFTManager.gameBoardHeight][FFTManager.gameBoardWidth];

        for (Literal l : addClause.literals) {
            if (l.row != POS_NONBOARD)
                clauseBoard[l.row][l.col] = l.pieceOcc;
        }
        for (Literal l : remClause.literals) {
            if (l.row != POS_NONBOARD)
                clauseBoard[l.row][l.col] = -l.pieceOcc;
        }

        return clauseBoard;
    }

    String getFormattedString() {
        StringBuilder actionMsg = new StringBuilder();
        for (Literal literal : addClause.literals) {
            if (actionMsg.length() > 0)
                actionMsg.append(" ∧ ");
            actionMsg.append("+").append(literal.name);
        }
        for (Literal literal : remClause.literals) {
            if (actionMsg.length() > 0)
                actionMsg.append(" ∧ ");
            actionMsg.append("-").append(literal.name);
        }
        return actionMsg.toString();
    }

    // Takes empty addClause and remClause lists (except for constants),
    // and fill them up with the rotated pieces from the cb array.
    private Action boardToAction(ArrayList<Integer> transformations, int[][] cb) {
        ArrayList<Literal> addLits = new ArrayList<>();
        ArrayList<Literal> remLits = new ArrayList<>();
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
        Clause addC = new Clause(transformations, addLits);
        Clause remC = new Clause(transformations, remLits);
        return new Action(addC, remC);
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
