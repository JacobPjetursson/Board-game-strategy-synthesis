package fftlib;

import misc.Config;

import java.util.ArrayList;
import java.util.Objects;

public class Clause {
    public ArrayList<Literal> literals;
    int symmetry = Config.SYM_NONE;

    public Clause() {
        this.literals = new ArrayList<>();
    }

    public Clause(ArrayList<Literal> literals) {
        this.literals = literals;
    }

    Clause(int symmetry, Clause clause) {
        this.literals = new ArrayList<>(clause.literals);
        this.symmetry = symmetry;
    }

    public Clause(Clause duplicate) {
        this.literals = new ArrayList<>(duplicate.literals);
        this.symmetry = duplicate.symmetry;
    }

    Clause(int symmetry, ArrayList<Literal> literals) {
        this.literals = literals;
        this.symmetry = symmetry;
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
        String clauseMsg = "";
        for (Literal literal : literals) {
            if (!clauseMsg.isEmpty())
                clauseMsg += " ∧ ";
            clauseMsg += literal.name;
        }
        return clauseMsg;
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