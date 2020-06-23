package fftlib.logic;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;

import java.util.HashSet;

// TODO - make common super class for Rule and PredRule
public class PredRule extends Rule{
    private HashSet<Rule> groundedRules;
    private boolean inconsistent;

    public PredRule(LiteralSet preconditions, Action action) {
        this.preconditions = preconditions;
        this.action = action;
        groundedRules = initializeGroundedRules();
    }

    public void addPrecondition(Literal l) {
        if (l instanceof PredLiteral || !action.getPreconditions().contains(l))
            preconditions.add(l);
        groundedRules = initializeGroundedRules();
    }

    public void removePrecondition(Literal l) {
        this.preconditions.remove(l);
        groundedRules = initializeGroundedRules();
    }

    public HashSet<Rule> getGroundedRules() {
        return groundedRules;
    }

    public boolean isInConsistent() {
        return inconsistent;
    }

    private HashSet<Rule> initializeGroundedRules() {
        HashSet<Rule> rules = new HashSet<>();
        for (int prop : FFTManager.legalIndices) {
            boolean legalGrounding = true;
            LiteralSet litset = new LiteralSet();
            for (Literal l : preconditions) {
                if (l instanceof PredLiteral) {
                    PredLiteral pl = (PredLiteral) l;
                    Literal groundedLit = pl.groundAll(prop);
                    if (groundedLit == null) {
                        legalGrounding = false;
                        break;
                    }
                    litset.add(groundedLit);
                } else {
                    litset.add(l.clone());
                }
            }
            LiteralSet addSet = new LiteralSet();
            LiteralSet remSet = new LiteralSet();
            for (Literal add : action.adds) {
                if (add instanceof PredLiteral) {
                    PredLiteral pr = (PredLiteral) add;
                    Literal groundedLit = pr.groundAll(prop);
                    if (groundedLit == null) {
                        legalGrounding = false;
                        break;
                    }
                    addSet.add(groundedLit);
                } else {
                    addSet.add(add.clone());
                }
            }
            for (Literal rem : action.rems) {
                if (rem instanceof PredLiteral) {
                    PredLiteral pr = (PredLiteral) rem;
                    Literal groundedLit = pr.groundAll(prop);
                    if (groundedLit == null) {
                        legalGrounding = false;
                        break;
                    }
                    remSet.add(groundedLit);
                } else {
                    remSet.add(rem.clone());
                }
            }
            // check for inconsistencies
            Action a = new Action(addSet, remSet);
            if (!a.isLegal(litset)) {
                inconsistent = true;
            }
            for (Literal l : litset) {
                Literal negLit = new Literal(l);
                if (litset.contains(negLit)) {
                    inconsistent = true;
                    break;
                }
            }
            if (inconsistent)
                return null;
            if (legalGrounding)
                rules.add(new Rule(litset, a));
        }
        return rules;
    }

    public void setRuleIndex(int index) {
        this.ruleIndex = index;
        for (Rule gr : groundedRules)
            gr.setRuleIndex(index);
    }


    /*
    // for manually specifying first order rules
    public HashSet<Rule> getRules(String preconStr, String actionStr) {
        HashSet<Rule> rules = new HashSet<>();
        // identify predicate (start with '?')
        String predicate = null;
        for (int i = 0; i < preconStr.length(); i++) {
            char c = preconStr.charAt(i);
            if (c == '?')
                predicate = preconStr.substring(i, i+1);
        }
        for (int i = 0; i < actionStr.length(); i++) {
            char c = actionStr.charAt(i);
            if (c == '?')
                predicate = actionStr.substring(i, i+1);
        }
        // If no predicates, then return hashset with single rule
        if (predicate == null) {
            rules.add(new Rule(preconStr, actionStr));
            return rules;
        }
        // replace predicates with all possible propositional symbols
        for (int index : FFTManager.legalIndices) {
            String precReplaced = preconStr.replace(predicate, String.valueOf(index));
            String actReplaced = actionStr.replace(predicate, String.valueOf(index));
            rules.add(new Rule(precReplaced, actReplaced)); // TODO - assumes only a single replacement

        }
        return rules;
    }
     */

    public HashSet<FFTMove> apply(FFTNode node) {
        HashSet<FFTMove> moves = new HashSet<>();
        for (Rule r : groundedRules) {
            moves.addAll(r.apply(node));
        }
        return moves;
    }

}
