package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.Transform;
import misc.Globals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static fftlib.FFTManager.gameBoardHeight;
import static fftlib.FFTManager.gameBoardWidth;
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

    public Action(int row, int col, int occ, boolean neg) {
        this.addClause = new Clause();
        this.remClause = new Clause();
        this.addClause.add(new Literal(row, col, occ, neg));
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
        if (Globals.CURRENT_GAME == Globals.KULIBRAT) {
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

    public FFTMove getMove(int team) {
        return FFTManager.actionToMove.apply(this, team);
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

    public String toString() {
        return getFormattedString();
    }
}
