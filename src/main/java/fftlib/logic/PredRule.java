package fftlib.logic;

import fftlib.FFTManager;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;

import java.util.HashSet;

// TODO - make common super class for Rule and PredRule
public class PredRule extends Rule{
    public HashSet<Rule> groundedRules;

    public PredRule(LiteralSet preconditions, Action action) {
        this.preconditions = preconditions;
        this.action = action;
        groundedRules = getGroundedRules();
    }

    public void addPrecondition(Literal l) {
        if (action == null)
            System.out.println("wuut?");
        if (l == null)
            System.out.println("literal null?");
        if (!action.getPreconditions().contains(l)) // do not include precondition from action
            preconditions.add(l);
        groundedRules = getGroundedRules();
    }

    public void removePrecondition(Literal l) {
        this.preconditions.remove(l);
        groundedRules = getGroundedRules();
    }

    public HashSet<Rule> getGroundedRules() {
        HashSet<Rule> rules = new HashSet<>();
        for (int prop : FFTManager.legalIndices) {
            boolean illegalGrounding = false;
            LiteralSet litset = new LiteralSet();
            for (Literal l : preconditions) {
                if (l instanceof PredLiteral) {
                    PredLiteral pl = (PredLiteral) l;
                    Literal groundedLit = pl.groundAll(prop);
                    if (groundedLit == null) {
                        illegalGrounding = true;
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
                        illegalGrounding = true;
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
                        illegalGrounding = true;
                        break;
                    }
                    remSet.add(groundedLit);
                } else {
                    remSet.add(rem.clone());
                }
            }
            Action a = new Action(addSet, remSet);
            if (!illegalGrounding)
                rules.add(new Rule(litset, a));
        }
        return rules;
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
