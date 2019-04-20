package fftlib;

import java.util.ArrayList;

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
}
