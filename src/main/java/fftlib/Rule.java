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
import static misc.Globals.CURRENT_GAME;
import static misc.Globals.SIM;


public class Rule {
    private static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "∧"));
    public Clause preconditions;
    public HashSet<Rule> symmetryRules;
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
        setTransformedRules();
        errors = !isValidRuleFormat(this);
    }

    public Rule(HashSet<Literal> precons, Action action) {
        this.multiRule = false;
        this.action = action;
        this.preconditions = new Clause(precons);
        this.symmetryRules = new HashSet<>();
    }

    public Rule(Clause precons, Action action) {
        this.multiRule = false;
        this.action = action;
        this.preconditions = precons;
        this.symmetryRules = new HashSet<>();
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
        this.symmetryRules = new HashSet<>();
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
        this.symmetryRules = new HashSet<>();

    }

    public void addPrecondition(Literal l) {
        if (!preconditions.literals.contains(l))
            preconditions.add(l);
        setTransformedRules();
    }

    public void addPrecondition(GdlSentence s) {
        this.sentences.add(s);
    }

    public void removePrecondition(Literal l) {
        this.preconditions.remove(l);
        setTransformedRules();
    }

    public void removePrecondition(GdlSentence s) {
        this.sentences.remove(s);
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

        setTransformedRules();
    }

    public void setAction(Action a) {
        if (a == null)
            this.action = new Action();
        else
            this.action = a;
    }

    public void setMove(Move m) {
        this.move = m;
    }

    public void setPreconditions(Clause c) {
        if (c == null)
            this.preconditions = new Clause();
        else
            this.preconditions = c;
        setTransformedRules();
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

    private static boolean isMultiRule(String preconStr, String actionStr) {
        ArrayList<String> prepLiteralStr = prepPreconditions(preconStr);
        ArrayList<String> prepActionStr = prepAction(actionStr);
        for (String lStr : prepLiteralStr) {
            String[] coords = Literal.getCoords(lStr);
            if (coords == null)
                return false;

            if (!Character.isDigit(coords[0].charAt(0)))
                return true;
            if (!Character.isDigit(coords[1].charAt(0)))
                return true;

        }
        for (String aStr : prepActionStr) {
            String[] coords = Literal.getCoords(aStr);
            if (coords == null)
                return false;

            if (!Character.isDigit(coords[0].charAt(0)))
                return true;
            if (!Character.isDigit(coords[1].charAt(0)))
                return true;
        }
        return false;
    }

    private static void replaceWildcards(String preconStr, String actionStr, HashSet<Rule> rules) {
        ArrayList<String> prepLiteralStr = prepPreconditions(preconStr);
        ArrayList<String> prepActionStr = prepAction(actionStr);
        HashSet<String> wildCardsRow = new HashSet<>();
        HashSet<String> wildCardsCol = new HashSet<>();
        for (String lStr : prepLiteralStr) {
            String[] coords = Literal.getCoords(lStr);
            if (coords == null)
                continue;
            if (!Character.isDigit(coords[0].charAt(0)))
                wildCardsRow.add(coords[0]);
            if (!Character.isDigit(coords[1].charAt(0)))
                wildCardsCol.add(coords[1]);

        }
        for (String aStr : prepActionStr) {
            String[] coords = Literal.getCoords(aStr);
            if (coords == null)
                continue;
            if (!Character.isDigit(coords[0].charAt(0)))
                wildCardsRow.add(coords[0]);
            if (!Character.isDigit(coords[1].charAt(0)))
                wildCardsCol.add(coords[1]);
        }
        if (wildCardsCol.isEmpty() && wildCardsRow.isEmpty()) {
            rules.add(new Rule(preconStr, actionStr));
            return;
        }

        for (String wc : wildCardsRow) {
            for (int i = 0; i < FFTManager.gameBoardHeight; i++) {
                String finalCStr = preconStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }
        }

        for (String wc : wildCardsCol) {
            for (int i = 0; i < FFTManager.gameBoardWidth; i++) {
                String finalCStr = preconStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }
        }
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

    public void setTransformedRules() {

        if (CURRENT_GAME != SIM) {
            //return Transform.applyAll(FFTManager.gameSymmetries, this);
        }
        else {
            symmetryRules = Transform.findAutomorphisms(this);
        }
    }


    public FFTMove apply(FFTState state) {
        HashSet<Literal> stLiterals = state.getLiterals();
        FFTMove m = match(this, state, stLiterals);
        if (m != null) return m;
        for (Rule rule : symmetryRules) { // TODO - possible to optimize here by only including unique clauses
            if (rule.preconditions.equals(preconditions))
                continue;
            m = match(rule, state, stLiterals);
            if (m != null) return m;
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

        return this.preconditions.equals(rule.preconditions) && this.action.equals(rule.action);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(preconditions, action);
    }
}
