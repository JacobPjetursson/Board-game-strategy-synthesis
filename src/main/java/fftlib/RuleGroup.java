package fftlib;

import misc.Config;

import java.util.ArrayList;

public class RuleGroup {
    public ArrayList<Rule> rules;
    public String name;
    public boolean locked; // Are we allowed to modify this rulegroup (e.g. minimize?)

    public RuleGroup(String name) {
        rules = new ArrayList<>();
        this.name = name;
    }

    public RuleGroup(String name, boolean locked) {
        rules = new ArrayList<>();
        this.name = name;
        this.locked = locked;
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
        this.locked = copy.locked;
    }

    public void addRule(Rule r) {
        rules.add(r);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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
            precons += (Config.ENABLE_GGP) ? r.sentences.size() : r.preconditions.size();
        }
        return precons;
    }
}
