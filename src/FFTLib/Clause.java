package fftlib;

import java.util.ArrayList;
import java.util.Objects;

public class Clause {
    public ArrayList<Literal> literals;
    ArrayList<Integer> transformations;

    public Clause() {
        this.literals = new ArrayList<>();
    }

    public Clause(ArrayList<Literal> literals) {
        this.literals = literals;
    }

    Clause(ArrayList<Integer> transformations, Clause clause) {
        this.literals = new ArrayList<>(clause.literals);
        this.transformations = transformations;
    }

    public Clause(Clause duplicate) {
        this.literals = new ArrayList<>(duplicate.literals);
        this.transformations = new ArrayList<>(duplicate.transformations);
    }

    Clause(ArrayList<Integer> transformations, ArrayList<Literal> literals) {
        this.literals = literals;
        this.transformations = transformations;
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


    ArrayList<Literal> extractNonBoardPlacements() {
        ArrayList<Literal> nonBoardPlacements = new ArrayList<>();
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
        return 31 * Objects.hashCode(this.literals);
    }
}