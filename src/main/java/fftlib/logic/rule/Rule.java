package fftlib.logic.rule;

import fftlib.GGPAutogen.GGPManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.logic.LiteralSet;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static misc.Config.ENABLE_GGP_PARSER;
import static misc.Config.SHOW_GUI;

public abstract class Rule {
    protected static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "∧"));
    // preconditions are the preconditions visible to the human
    protected LiteralSet preconditions;
    // allPreconditions are all the preconditions, including those from the action
    protected LiteralSet allPreconditions;
    protected Action action;

    // General game playing
    public Set<GdlSentence> sentences;
    protected Move move;

    // index in FFT
    protected int ruleIndex = -1;

    public abstract void setRuleIndex(int index);

    public int getRuleIndex() {
        return ruleIndex;
    }

    public LiteralSet getPreconditions() {
        return preconditions;
    }

    public LiteralSet getAllPreconditions() {
        return allPreconditions;
    }

    public Action getAction() {
        return action;
    }

    protected static ArrayList<String> prepPreconditions(String preconStr) {
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

    protected static ArrayList<String> prepAction(String actionStr) {
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

    static Action parseAction(String actionStr) {
        return new Action(prepAction(actionStr));
    }

    static LiteralSet parsePreconditions(String preconStr) {
        LiteralSet literals = new LiteralSet();
        for (String precons : prepPreconditions(preconStr)) {
            literals.add(new Literal(precons));
        }
        return literals;
    }

    protected void removeActionPrecons() {
        for (Literal l : action.getPreconditions())
            preconditions.remove(l);
    }

    public String toString() {
        if (sentences != null || move != null) { // ggp
            String str = "IF: " + sentences + " THEN: " + move;
            if (ruleIndex != -1)
                str += " , index: " + ruleIndex;
            return str;
        }

        if (ENABLE_GGP_PARSER) {
            // TODO
        }
        String str = "IF [" + preconditions + "] THEN [" + action + "]";
        if (!SHOW_GUI && ruleIndex != -1)
            str += " , index: " + ruleIndex;
        return str;
    }

    public static PropRule createRule(FFTNode n, FFTMove m) {
        return new PropRule(new LiteralSet(n.convert()), new Action(m.convert()));
    }

    public abstract Rule clone();

    public abstract void removePrecondition(Literal l);

    public abstract void addPrecondition(Literal l);

    public HashSet<FFTMove> apply(FFTNode n) {
        return apply(n.convert());
    }

    public abstract HashSet<FFTMove> apply(LiteralSet lSet);

    public Move apply(MachineState ms) throws MoveDefinitionException {
        Set<GdlSentence> stSentences = ms.getContents();
        // TODO - symmetry
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

    private boolean matchSentence(GdlSentence s, Set<GdlSentence> stSentences) {
        if (false) // todo - negation
            return false;
        return stSentences.contains(s);
    }



    public void addPrecondition(GdlSentence s) {
        this.sentences.add(s);
        //this.transformedSentences = getTransformedSentences();
    }

    public void removePrecondition(GdlSentence s) {
        this.sentences.remove(s);
        //this.transformedSentences = getTransformedSentences();

    }

    public void setAllPreconditions() {
        allPreconditions = new LiteralSet(preconditions);
        allPreconditions.addAll(action.getPreconditions());
    }

    public abstract void setAction(Action action);
}
