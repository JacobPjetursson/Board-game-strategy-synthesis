package fftlib;

import fftlib.GGPAutogen.GGPManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

import java.util.*;

import static misc.Config.ENABLE_GGP_PARSER;
import static misc.Config.SYMMETRY_DETECTION;


public class Rule {
    private static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "∧"));
    public HashSet<Literal> preconditions;
    public Action action;
    public HashSet<SymmetryRule> symmetryRules;

    // General game playing
    public Set<GdlSentence> sentences;
    private Move move;

    // parsing constructor
    public Rule(String preconStr, String actionStr) {
        if (ENABLE_GGP_PARSER) {
            Rule r = FFTManager.gdlToRule.apply(preconStr, actionStr);
            this.action = r.action;
            this.preconditions = r.preconditions;
        } else {
            this.action = getAction(actionStr);
            this.preconditions = getPreconditions(preconStr);
        }
        this.symmetryRules = getSymmetryRules();
    }

    public Rule(HashSet<Literal> precons, Action action) {
        this.action = action;
        this.preconditions = precons;
        this.symmetryRules = getSymmetryRules();
    }

    // GDL Constructor
    public Rule(Set<GdlSentence> sentences, Move move) {
        this.sentences = sentences;
        this.move = move;
    }

    // Empty constructor to allow rule buildup
    public Rule() {
        this.preconditions = new HashSet<>();
        this.action = new Action();
    }

    public Rule(Set<GdlSentence> sentences) {
        this.sentences = sentences;
    }

    // Duplicate constructor
    public Rule(Rule duplicate) {
        this.action = new Action(duplicate.action);
        this.preconditions = new HashSet<>(duplicate.preconditions);
        this.symmetryRules = new HashSet<>(duplicate.symmetryRules);

    }

    public void addPrecondition(Literal l) {
        preconditions.add(l);
        this.symmetryRules = getSymmetryRules();
    }

    public void addPrecondition(GdlSentence s) {
        this.sentences.add(s);
        //this.transformedSentences = getTransformedSentences();
    }

    public void removePrecondition(Literal l) {
        this.preconditions.remove(l);
        this.symmetryRules = getSymmetryRules();
    }

    public void removePrecondition(GdlSentence s) {
        this.sentences.remove(s);
        //this.transformedSentences = getTransformedSentences();

    }

    public void setAction(Action a) {
        if (a == null)
            this.action = new Action();
        else
            this.action = a;
        this.symmetryRules = getSymmetryRules();
    }

    // Gdl
    public void setMove(Move m) {
        this.move = m;
        this.symmetryRules = getSymmetryRules();
    }

    public void setPreconditions(HashSet<Literal> precons) {
        if (precons == null)
            this.preconditions = new HashSet<>();
        else
            this.preconditions = precons;
        this.symmetryRules = getSymmetryRules();
    }

    // Gdl
    public void setSentences(Set<GdlSentence> sentences) {
        this.sentences = sentences;
        //this.transformedSentences = getTransformedSentences();
    }

    private static ArrayList<String> prepPreconditions(String preconStr) {
        ArrayList<String> precons = new ArrayList<>();
        StringBuilder regex = new StringBuilder();
        regex.append(separators.get(0));
        for (int i = 1; i < separators.size(); i++)
            regex.append("|").append(separators.get(i));

        String[] parts = preconStr.split(regex.toString());
        for (String part : parts) {
            if (part.length() < 2)
                continue;
            part = part.trim();
            precons.add(part);
        }
        return precons;
    }

    private static ArrayList<String> prepAction(String actionStr) {
        StringBuilder regex = new StringBuilder();
        regex.append(separators.get(0));
        for (int i = 1; i < separators.size(); i++)
            regex.append("|").append(separators.get(i));

        String[] parts = actionStr.split(regex.toString());
        ArrayList<String> corrected_parts = new ArrayList<>();
        for (String part : parts) {
            if (part.length() < 2)
                continue;
            part = part.trim();
            corrected_parts.add(part);

        }

        return corrected_parts;
    }

    // This is an upper bound due to how symmetry works (may lead to duplicate states
    // that are hard to detect)
    // FIXME - make it work with negative preconditions
    // TODO - somehow make it more precise (do a pre-run to compute size?)
    public long getNumberOfCoveredStates() {
        int number = 0;
        for (Rule r : symmetryRules) {
            number += FFTManager.getNumberOfCoveredStates.apply(r);
        }
        return number;
    }

    public HashSet<Long> getCoveredStateBitCodes() {
        HashSet<Long> bitCodes = new HashSet<>();
        for (Rule r : symmetryRules) {
            bitCodes.addAll(FFTManager.getCoveredStateBitCodes.apply(r));
        }
        return bitCodes;
    }

    private static HashSet<Literal> getPreconditions(String preconStr) {
        HashSet<Literal> literals = new HashSet<>();
        for (String precons : prepPreconditions(preconStr)) {
            literals.add(new Literal(precons));
        }
       return literals;
    }

    private static Action getAction(String actionStr) {
        return new Action(prepAction(actionStr));
    }

    public String toString() {
        if (sentences != null || move != null) { // ggp
            return "IF: " + sentences + " THEN: " + move;
        }

        if (ENABLE_GGP_PARSER) {
            // TODO
        }

        return "IF [" + getPreconString() + "] THEN [" + getActionString() + "]";
    }

    public String getPreconString() {
        StringBuilder pStr = new StringBuilder();
        for (Literal l : preconditions) {
            if (pStr.length() > 0)
                pStr.append(" ∧ ");
            pStr.append(l.getName());
        }
        return pStr.toString();
    }

    public String getActionString() {
        return action.getFormattedString();
    }

    public HashSet<SymmetryRule> getSymmetryRules() {
        return FFTManager.getSymmetryRules.apply(this);
    }

    public FFTMove apply(FFTState state) {
        HashSet<Literal> stLiterals = state.getLiterals();
        FFTMove m = match(this, state, stLiterals);
        if (m != null || !SYMMETRY_DETECTION) {
            return m;
        }
        for (Rule rule : symmetryRules) {
            m = match(rule, state, stLiterals);
            // returns the first symmetry with a legal move
            // We want to ensure that rule is optimal in all symmetric states, but since all these will be explored
            // by the algorithm, we are good. This is the case since we always start with checking the default state
            // (no applied symmetries), meaning every symmetric states will inevitably be checked.
            // Alternative is to check that all non-null moves here are legal.
            // TODO - check which method is fastest
            if (m != null) {
                return m;
            }
        }
        return null;
    }

    private FFTMove match(Rule rule, FFTState state, HashSet<Literal> stLiterals) {
        boolean match = true;
        for (Literal l : rule.preconditions) {
            match = matchLiteral(l, stLiterals);
            if (!match)
                break;

        }
        if (match) {
            FFTMove move = rule.action.getMove();
            if (FFTManager.logic.isLegalMove(state, move)) {
                return move;
            }
        }
        return null;
    }

    public Move apply(MachineState ms) throws MoveDefinitionException {
        Set<GdlSentence> stSentences = ms.getContents();
        // TODO - transformations of sentences
            boolean match = true;
            for (GdlSentence s : sentences) {
                match = matchSentence(s, stSentences);
                if (!match)
                    break;

            }

            if (match) {
                // todo - transform move
                //Move m = FFTManager.transformMove(move, transformations);
                Role r = GGPManager.getRole(ms);
                if (GGPManager.getLegalMoves(ms, r).contains(move)) {
                    return move;
                }
            }

        return null;
    }

    private boolean matchLiteral(Literal l, HashSet<Literal> stLiterals) {
        if (l.negation) {
            Literal temp = new Literal(l);
            temp.negation = false;
            return !stLiterals.contains(temp);
        }
        return stLiterals.contains(l);
    }

    private boolean matchSentence(GdlSentence s, Set<GdlSentence> stSentences) {
        if (false) // todo - negation
            return false;
        return stSentences.contains(s);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rule)) return false;

        Rule rule = (Rule) obj;
        if (this == rule)
            return true;

        return this.symmetryRules.equals(rule.symmetryRules);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(symmetryRules);
    }

}
