package fftlib;

import fftlib.GGPAutogen.GGPManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.game.Transform;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

import java.util.*;

import static fftlib.Literal.*;
import static misc.Config.ENABLE_GGP_PARSER;
import static misc.Config.SYMMETRY_DETECTION;
import static misc.Globals.CURRENT_GAME;
import static misc.Globals.SIM;


public class Rule {
    private static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "∧"));
    public Clause preconditions;
    public HashSet<SymmetryRule> symmetryRules;
    public Action action;
    private String actionStr, preconStr;


    // If multirule, the rule class instead contains a list of rules
    public boolean multiRule;
    public HashSet<Rule> rules;
    public boolean errors;

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
        errors = !isValidRuleFormat(this);
        this.symmetryRules = getTransformedRules();
    }

    public Rule(HashSet<Literal> precons, Action action) {
        this.multiRule = false;
        this.action = action;
        this.preconditions = new Clause(precons);
        this.symmetryRules = getTransformedRules();
    }

    public Rule(Clause precons, Action action) {
        this.multiRule = false;
        this.action = action;
        this.preconditions = precons;
        this.symmetryRules = getTransformedRules();
    }

    // GDL Constructor
    public Rule(Set<GdlSentence> sentences, Move move) {
        this.multiRule = false;
        this.sentences = sentences;
        this.move = move;
    }

    // Empty constructor to allow rule buildup
    public Rule() {
        this.preconditions = new Clause();
        this.action = new Action();
        preconStr = "";
        actionStr = "";
    }

    public Rule(Set<GdlSentence> sentences) {
        this.multiRule = false;
        this.sentences = sentences;
    }

    // Duplicate constructor
    public Rule(Rule duplicate) {
        this.action = new Action(duplicate.action);
        this.preconditions = new Clause(duplicate.preconditions);
        this.errors = duplicate.errors;
        this.symmetryRules = new HashSet<>(duplicate.symmetryRules);

    }

    public void addPrecondition(Literal l) {
        if (!preconditions.literals.contains(l))
            preconditions.add(l);
        this.symmetryRules = getTransformedRules();
    }

    public void addPrecondition(GdlSentence s) {
        this.sentences.add(s);
        //this.transformedSentences = getTransformedSentences();
    }

    public void removePrecondition(Literal l) {
        this.preconditions.remove(l);
        this.symmetryRules = getTransformedRules();
    }

    public void removePrecondition(GdlSentence s) {
        this.sentences.remove(s);
        //this.transformedSentences = getTransformedSentences();

    }


    public void removeLiterals(int row, int col) {
        ArrayList<Literal> removeList = new ArrayList<>();
        for (Literal l : preconditions.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        preconditions.literals.removeAll(removeList);
        removeList.clear();
        for (Literal l : action.addClause.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        action.addClause.literals.removeAll(removeList);
        removeList.clear();
        for (Literal l : action.remClause.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        action.remClause.literals.removeAll(removeList);

        this.symmetryRules = getTransformedRules();
    }

    public void setAction(Action a) {
        if (a == null)
            this.action = new Action();
        else
            this.action = a;
        this.symmetryRules = getTransformedRules();
    }

    public void setMove(Move m) {
        this.move = m;
        this.symmetryRules = getTransformedRules();
    }

    public void setPreconditions(Clause c) {
        if (c == null)
            this.preconditions = new Clause();
        else
            this.preconditions = c;
        this.symmetryRules = getTransformedRules();
    }

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

    private static Clause getPreconditions(String clauseStr) {
        HashSet<Literal> literals = new HashSet<>();
        for (String c : prepPreconditions(clauseStr)) {
            literals.add(new Literal(c));
        }
       return new Clause(literals);
    }

    private static Action getAction(String actionStr) {
        return new Action(prepAction(actionStr));
    }

    public static boolean isValidRuleFormat(Rule r) {
        if (r.multiRule) {
            for (Rule rule : r.rules)
                if (!isValidRuleFormat(rule))
                    return false;
            return true;
        }
        for (Literal l : r.preconditions.literals)
            if (l.error)
                return false;
        if (r.action.addClause.isEmpty() && r.action.remClause.isEmpty())
            return false;
        if (r.action.actionErr)
            return false;
        for (Literal l : r.action.addClause.literals)
            if (l.error)
                return false;
        for (Literal l : r.action.remClause.literals)
            if (l.error)
                return false;

        return true;
    }

    public String toString() {
        if (sentences != null || move != null) { // ggp
            return "IF: " + sentences + " THEN: " + move;
        }
        String cStr, aStr;
        if (preconditions != null && action != null) {
            cStr = preconditions.getFormattedString();
            aStr = action.getFormattedString();
        } else {
            cStr = preconStr;
            aStr = actionStr;
        }
        if (ENABLE_GGP_PARSER) {
            if (cStr.isEmpty())
                cStr = "∅";
            return "IF: [" + cStr + "] THEN: [" +
                    action.addClause.getFormattedString() + " " + action.remClause.getFormattedString() + "]";
        }

        return "IF [" + cStr + "] THEN [" + aStr + "]";
    }

    String getPreconStr() {
        if (preconditions != null)
            return preconditions.getFormattedString();
        else
            return preconStr;
    }

    String getActionStr() {
        if (action != null)
            return action.getFormattedString();
        else
            return actionStr;
    }

    public HashSet<SymmetryRule> getTransformedRules() {

        if (CURRENT_GAME != SIM) {
            return Transform.getSymmetryRules(FFTManager.gameSymmetries, this);
        }
        else {
            return Transform.findAutomorphisms(this);
        }
    }

    public FFTMove apply(FFTState state) {
        HashSet<Literal> stLiterals = state.getLiterals();
        FFTMove m = match(this, state, stLiterals);
        if (m != null || !SYMMETRY_DETECTION) {
            return m;
        }
        for (Rule rule : symmetryRules) {
            m = match(rule, state, stLiterals);
            if (m != null) {
                return m;
            }
        }
        return null;
    }

    private FFTMove match(Rule rule, FFTState state, HashSet<Literal> stLiterals) {
        boolean match = true;
        for (Literal l : rule.preconditions.literals) {
            if (l.pieceOcc == PIECEOCC_ANY) {
                Literal temp = new Literal(l);
                temp.pieceOcc = PIECEOCC_PLAYER;
                temp.format();
                match = matchLiteral(temp, stLiterals);
                if (!match)
                    break;

                temp.pieceOcc = PIECEOCC_ENEMY;
                temp.format();
                match = matchLiteral(temp, stLiterals);
            } else {
                match = matchLiteral(l, stLiterals);
            }
            if (!match)
                break;

        }
        if (match) {
            Action a = rule.action;
            FFTMove move = a.getMove(state.getTurn());
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
            temp.format();
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
