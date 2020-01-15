package fftlib;

import java.util.ArrayList;

import static misc.Config.USE_GGP;

public class RuleGroup {
    public ArrayList<Rule> rules;
    public String name;

    public RuleGroup(String name) {
        rules = new ArrayList<>();
        this.name = name;
    }

    public RuleGroup(String name, ArrayList<Rule> rules) {
        this.name = name;
        this.rules = rules;
    }

    public RuleGroup(RuleGroup copy) {
        this.rules = new ArrayList<>();
        for (Rule r : copy.rules)
            rules.add(new Rule(r));
        this.name = copy.name;
    }

    public void addRule(Rule r) {
        rules.add(r);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Printing rules in rg:\n");
        sb.append("-----------------\n");
        for (Rule r : rules)
            sb.append(r).append("\n");
        sb.append("-----------------\n");
        return sb.toString();
    }

    int getAmountOfPreconditions() {
        int precons = 0;
        for (Rule r : rules) {
            if (r.multiRule) continue; // TODO - fuck this shit
            precons += (USE_GGP) ? r.sentences.size() : r.preconditions.size();
        }
        return precons;
    }
}
