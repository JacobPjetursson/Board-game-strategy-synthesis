package fftlib.logic;

import fftlib.FFTManager;
import fftlib.GGPAutogen.GGPManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

import java.lang.reflect.Array;
import java.util.*;

import static misc.Config.ENABLE_GGP_PARSER;
import static misc.Config.SYMMETRY_DETECTION;


public class Rule {
    private static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "∧"));
    protected LiteralSet preconditions;
    protected Action action;
    private HashSet<SymmetryRule> symmetryRules;

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
        removeActionPrecons();
        this.symmetryRules = getSymmetryRules();
    }

    public Rule(LiteralSet precons, Action action) {
        this.action = action;
        this.preconditions = precons;
        removeActionPrecons();
        this.symmetryRules = getSymmetryRules();
    }

    // GDL Constructor
    public Rule(Set<GdlSentence> sentences, Move move) {
        this.sentences = sentences;
        this.move = move;
    }

    // Empty constructor to allow rule buildup
    public Rule() {
        this.preconditions = new LiteralSet();
        this.action = new Action();
    }

    public Rule(Set<GdlSentence> sentences) {
        this.sentences = sentences;
    }

    // Duplicate constructor
    public Rule(Rule duplicate) {
        this.action = new Action(duplicate.action);
        this.preconditions = new LiteralSet(duplicate.preconditions);
        this.symmetryRules = new HashSet<>(duplicate.symmetryRules);

    }

    public void addPrecondition(Literal l) {
        if (!action.getPreconditions().contains(l)) // do not include precondition from action
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

    public LiteralSet getPreconditions() {
        return preconditions;
    }

    public LiteralSet getAllPreconditions() {
        LiteralSet precons = new LiteralSet(preconditions);
        precons.addAll(action.getPreconditions());
        return precons;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action a) {
        if (a == null)
            this.action = new Action();
        else
            this.action = a;
        removeActionPrecons();
        this.symmetryRules = getSymmetryRules();
    }

    // Gdl
    public void setMove(Move m) {
        this.move = m;
        this.symmetryRules = getSymmetryRules();
    }

    public void setPreconditions(LiteralSet precons) {
        if (precons == null)
            this.preconditions = new LiteralSet();
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

    private void removeActionPrecons() {
        for (Literal l : action.getPreconditions())
            preconditions.remove(l);
    }

    // This is an upper bound due to how symmetry works (may lead to duplicate states
    // that are hard to detect)
    public long getNumberOfCoveredStates() {
        int number = 0;
        if (!SYMMETRY_DETECTION)
            return FFTManager.getNumberOfCoveredStates.apply(this);

        for (Rule r : symmetryRules) {
            number += FFTManager.getNumberOfCoveredStates.apply(r);
        }
        return number;
    }

    public HashSet<LiteralSet> getCoveredStates() {
        if (!SYMMETRY_DETECTION)
            return FFTManager.getCoveredStates.apply(this);

        HashSet<LiteralSet> states = new HashSet<>();
        for (Rule r : symmetryRules) {
            states.addAll(FFTManager.getCoveredStates.apply(r));
        }
        return states;
    }

    private static LiteralSet getPreconditions(String preconStr) {
        LiteralSet literals = new LiteralSet();
        for (String precons : prepPreconditions(preconStr)) {
            literals.add(new Literal(precons));
        }
       return literals;
    }

    // Lifts a single propositional symbol to a predicate symbol
    // Takes as arg the index to lift
    public PredRule liftAll(int prop) {
        LiteralSet newPrecs = new LiteralSet();
        for (Literal l : preconditions) {
            newPrecs.add(l.liftAll(prop));
        }
        LiteralSet addSet = new LiteralSet();
        LiteralSet remSet = new LiteralSet();
        for (Literal l : action.adds)
            addSet.add(l.liftAll(prop));
        for (Literal l : action.rems)
            remSet.add(l.liftAll(prop));
        Action a = new Action(addSet, remSet);
        return new PredRule(newPrecs, a);
    }

    // returns sorted list based on most reoccurring index (which we will prioritize when lifting)
    public ArrayList<Integer> getSortedProps() {
        TreeMap<Integer, Integer> occMap = new TreeMap<>();
        // map idx -> occ and then reverse map
        addToOccMap(occMap, preconditions);
        addToOccMap(occMap, action.adds);
        addToOccMap(occMap, action.rems);
        TreeMap<Integer, ArrayList<Integer>> reverseMap = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<Integer, Integer> entry : occMap.entrySet()) {
            int occ = entry.getValue();
            int idx = entry.getKey();
            if (reverseMap.containsKey(occ))
                reverseMap.get(occ).add(idx);
            else {
                ArrayList<Integer> newList = new ArrayList<>();
                newList.add(idx);
                reverseMap.put(occ, newList);
            }
        }
        ArrayList<Integer> props = new ArrayList<>();
        for (ArrayList<Integer> vals : reverseMap.values())
            props.addAll(vals);
        return props;
    }

    private void addToOccMap(TreeMap<Integer, Integer> occMap, LiteralSet litSet) {
        for (Literal l : litSet) {
            for (int i : l.getIndices()) {
                if (occMap.containsKey(i))
                    occMap.put(i, occMap.get(i)+1);
                else
                    occMap.put(i, 1);
            }
        }
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

    public HashSet<FFTMove> apply(FFTNode node) {
        LiteralSet stLiterals = node.convert();
        HashSet<FFTMove> moves = new HashSet<>();

        FFTMove m = match(this, stLiterals);
        if (m != null)
            moves.add(m);
        if (/*m != null || */!SYMMETRY_DETECTION) {
            return moves;
        }



        for (Rule rule : symmetryRules) {
            m = match(rule, stLiterals);
            // returns the first symmetry with a legal move
            // We want to ensure that rule is optimal in all symmetric states, but since all these will be explored
            // by the algorithm, we are good. This is the case since we always start with checking the default state
            // (no applied symmetries), meaning every symmetric states will inevitably be checked.
            // Alternative is to check that all non-null moves here are legal, but that is significantly slower
            if (m != null) {
                moves.add(m);
            }
        }
        return moves;
    }

    private FFTMove match(Rule rule, LiteralSet stLiterals) {
        boolean match = true;
        for (Literal l : rule.preconditions) {
            match = matchLiteral(l, stLiterals);
            if (!match)
                break;

        }
        if (match && rule.action.isLegal(stLiterals))
            return rule.action.convert();

        return null;
    }

    private boolean matchLiteral(Literal lit, LiteralSet stLiterals) {
        if (lit.negated) {
            Literal l = new Literal(lit.id);
            return !stLiterals.contains(l);
        }
        return stLiterals.contains(lit);
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
        if (SYMMETRY_DETECTION)
            return this.symmetryRules.equals(rule.symmetryRules);
        return this.preconditions.equals(rule.preconditions) && this.action.equals(rule.action);
    }

    @Override
    public int hashCode() {
        if (SYMMETRY_DETECTION)
            return 31 * Objects.hashCode(symmetryRules);
        return 31 * Objects.hash(preconditions, action);
    }

}
