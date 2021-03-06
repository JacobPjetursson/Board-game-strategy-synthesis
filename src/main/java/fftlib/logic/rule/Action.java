package fftlib.logic.rule;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;

import java.util.ArrayList;


public class Action {
    public LiteralSet adds;
    public LiteralSet rems;

    public Action() {
        this.adds = new LiteralSet();
        this.rems = new LiteralSet();
    }

    public Action(int id) {
        this.adds = new LiteralSet();
        this.rems = new LiteralSet();
        this.adds.add(new PropLiteral(id));
    }

    public Action(String name) {
        this.adds = new LiteralSet();
        this.rems = new LiteralSet();
        this.adds.add(new PropLiteral(FFTManager.getAtomId.apply(name)));
    }

    public Action(Action duplicate) {
        this.adds = new LiteralSet(duplicate.adds);
        this.rems = new LiteralSet(duplicate.rems);
    }

    public Action(ArrayList<String> literals) {
        this.adds = new LiteralSet();
        this.rems = new LiteralSet();

        for (String lStr : literals) {
            if (lStr.startsWith("+")) {
                lStr = lStr.substring(1);
                PropLiteral l = new PropLiteral(lStr);
                adds.add(l);
            } else if (lStr.startsWith("-")) {
                lStr = lStr.substring(1);
                PropLiteral l = new PropLiteral(lStr);
                rems.add(l);
            } else {
                System.err.println("Invalid action format! Literal should be prefixed with plus (+) or minus (-)");
                System.err.println("Action: " + this);
                System.exit(1);
                return;
            }
        }

        if (adds.isEmpty() && rems.isEmpty()) {
            System.err.println("No action was specified");
        }
    }


    public Action(LiteralSet adds, LiteralSet rems) {
        this.adds = new LiteralSet(adds);
        this.rems = new LiteralSet(rems);
    }

    public LiteralSet getPreconditions() {
        LiteralSet precons = FFTManager.getActionPreconditions.apply(this);
        if (precons == null)
            return new LiteralSet();
        return precons;
    }

    public boolean isLegal(LiteralSet stateLiterals)  {
        for (Literal l : getPreconditions()) {
            if (!stateLiterals.contains(l))
                return false;
        }
        return true;
    }

    public FFTMove convert() {
        return FFTManager.actionToMove.apply(this);
    }

    String getFormattedString() {
        StringBuilder actionMsg = new StringBuilder();
        for (Literal literal : adds) {
            if (actionMsg.length() > 0)
                actionMsg.append(" ∧ ");
            actionMsg.append("+").append(literal);
        }
        for (Literal literal : rems) {
            if (actionMsg.length() > 0)
                actionMsg.append(" ∧ ");
            actionMsg.append("-").append(literal);
        }
        return actionMsg.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Action)) return false;

        Action action = (Action) obj;
        return this == action ||
                (this.adds.equals(action.adds) && this.rems.equals(action.rems));
    }

    @Override
    // attempt to semi-uniquely hash two numbers
    public int hashCode() {
        int hash = 23;
        hash = hash * 31 + adds.getBitString().intValue();
        hash = hash * 31 + rems.getBitString().intValue();
        return hash;
    }

    public String toString() {
        return getFormattedString();
    }

    public boolean isEmpty() {
        return adds.isEmpty() && rems.isEmpty();
    }

    public void clear() {
        adds.clear();
        rems.clear();
    }
}
