package fftlib;

import java.util.HashSet;
import java.util.Objects;

public class Clause{
    public HashSet<Literal> literals;

    public Clause() {
        this.literals = new HashSet<>();
    }

    public Clause(HashSet<Literal> literals) {
        this.literals = literals;
    }

    public Clause(Clause duplicate) {
        this.literals = new HashSet<>();
        for (Literal l : duplicate.literals) {
            Literal copy = new Literal(l);
            literals.add(copy);
        }
    }

    public void add(Literal l) {
        this.literals.add(l);
    }

    public void addAll(HashSet<Literal> literals) {
        this.literals.addAll(literals);
    }

    public void remove(Literal l) {
        this.literals.remove(l);
    }

    public boolean isEmpty() {
        return literals.isEmpty();
    }

    int size() {
        return literals.size();
    }

    String getFormattedString() {
        StringBuilder clauseMsg = new StringBuilder();
        for (Literal literal : literals) {
            if (clauseMsg.length() > 0)
                clauseMsg.append(" ∧ ");
            clauseMsg.append(literal.name);
        }
        return clauseMsg.toString();
    }


    public HashSet<Literal> nonBoardPlacements() {
        HashSet<Literal> nonBoardPlacements = new HashSet<>();
        for (Literal l : literals)
            if (!l.boardPlacement)
                nonBoardPlacements.add(l);
        return nonBoardPlacements;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Clause)) return false;

        Clause list = (Clause) obj;
        return this == list ||
                (this.literals.equals(list.literals));
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += Objects.hashCode(this.literals);
        return 31 * hashCode;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Literal l : literals)
            s.append(l).append(", ");
        return s.toString();
    }
}