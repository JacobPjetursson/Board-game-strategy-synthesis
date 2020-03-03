package fftlib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Clause{
    public HashSet<Literal> literals;
    ArrayList<Integer> transformations;

    public Clause() {
        this.literals = new HashSet<>();
        this.transformations = new ArrayList<>();
    }

    public Clause(HashSet<Literal> literals) {
        this.literals = literals;
        this.transformations = new ArrayList<>();
    }

    public Clause(Clause duplicate) {
        this.literals = new HashSet<>();
        for (Literal l : duplicate.literals) {
            Literal copy = new Literal(l);
            literals.add(copy);
        }
        this.transformations = new ArrayList<>(duplicate.transformations);
    }

    Clause(ArrayList<Integer> transformations, HashSet<Literal> literals) {
        this.transformations = transformations;
        this.literals = literals;
    }

    public void add(Literal l) {
        this.literals.add(l);
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


    public HashSet<Literal> extractNonBoardPlacements() {
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
                (this.literals.equals(list.literals) && this.transformations.equals(list.transformations));
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += Objects.hashCode(this.literals);
        hashCode += Objects.hashCode(this.transformations);
        return 31 * hashCode;
    }
}