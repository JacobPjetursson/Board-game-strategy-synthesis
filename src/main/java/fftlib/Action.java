package fftlib;

import fftlib.game.FFTMove;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;


public class Action {
    public HashSet<Literal> adds;
    public HashSet<Literal> rems;

    public Action() {
        this.adds = new HashSet<>();
        this.rems = new HashSet<>();
    }

    public Action(int id) {
        this.adds = new HashSet<>();
        this.rems = new HashSet<>();
        this.adds.add(new Literal(id, false));
    }

    public Action(String name) {
        this.adds = new HashSet<>();
        this.rems = new HashSet<>();
        this.adds.add(new Literal(FFTManager.getAtomId.apply(name), false));
    }

    public Action(Action duplicate) {
        this.adds = new HashSet<>(duplicate.adds);
        this.rems = new HashSet<>(duplicate.rems);
    }

    Action(ArrayList<String> literals) {
        this.adds = new HashSet<>();
        this.rems = new HashSet<>();

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


    public Action(HashSet<Literal> adds, HashSet<Literal> rems) {
        this.adds = new HashSet<>(adds);
        this.rems = new HashSet<>(rems);
    }

    public FFTMove getMove() {
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
                (this.adds.equals(action.adds) && (this.rems.equals(action.rems)));
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(adds, rems);
        return 31 * hash;
    }

    public String toString() {
        return getFormattedString();
    }
}
