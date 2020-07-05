package fftlib.logic;

import fftlib.FFTManager;
import fftlib.game.FFTMove;

import java.math.BigInteger;
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
        this.adds.add(new Literal(id));
    }

    public Action(String name) {
        this.adds = new LiteralSet();
        this.rems = new LiteralSet();
        this.adds.add(new Literal(FFTManager.getAtomId.apply(name)));
    }

    public Action(Action duplicate) {
        this.adds = new LiteralSet(duplicate.adds);
        this.rems = new LiteralSet(duplicate.rems);
    }

    Action(ArrayList<String> literals) {
        this.adds = new LiteralSet();
        this.rems = new LiteralSet();

        for (String lStr : literals) {
            if (lStr.startsWith("+")) {
                lStr = lStr.substring(1);
                Literal l = new Literal(lStr);
                adds.add(l);
            } else if (lStr.startsWith("-")) {
                lStr = lStr.substring(1);
                Literal l = new Literal(lStr);
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
            actionMsg.append("+").append(literal.getName());
        }
        for (Literal literal : rems) {
            if (actionMsg.length() > 0)
                actionMsg.append(" ∧ ");
            actionMsg.append("-").append(literal.getName());
        }
        return actionMsg.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Action)) return false;

        Action action = (Action) obj;
        return this == action ||
                (this.getCode() == action.getCode());
    }

    @Override
    public int hashCode() {
        return getCode().intValue();
    }

    private BigInteger getCode() {
        return this.adds.getBitString().add(this.rems.getBitString());
    }

    public String toString() {
        return getFormattedString();
    }
}
