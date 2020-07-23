package fftlib.logic.rule;

import fftlib.logic.FFT;

public class RuleGroup {
    public int startIdx, endIdx;
    public String name;
    private boolean locked; // Are we allowed to modify this rulegroup (e.g. minimize?)
    private FFT fft;

    public RuleGroup(FFT f, String name) {
        startIdx = -1;
        endIdx = -1;
        this.name = name;
        this.fft = f;
    }

    public RuleGroup(FFT f, String name, int startIdx, int endIdx) {
        this(f, name);
        this.startIdx = startIdx;
        this.endIdx = endIdx;

    }

    public RuleGroup(RuleGroup copy) {
        this.name = copy.name;
        this.fft = copy.fft;
        this.locked = copy.locked;
        this.startIdx = copy.startIdx;
        this.endIdx = copy.endIdx;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        for (Rule r : fft.getRules().subList(startIdx, endIdx))
            r.setLocked(locked);
    }

    public void updateIntervals(boolean removing, int idx) {
        int changedIdx = (removing) ? -1 : 1;
        if (idx < startIdx) { // before this rulegroup
            startIdx += changedIdx;
            endIdx += changedIdx;
        } else if (idx < endIdx) { // in this rulegroup
            endIdx += changedIdx;
        }
    }

    // used for moving rules
    public void updateIntervals(int oldIdx, int newIdx) {
        // moved from below this rulegroup into this rulegroup
        if (below(oldIdx) && inside(newIdx)) {
            startIdx--;
        }
        // moved from above this rulegroup into this rulegroup
        else if (above(oldIdx) && inside(newIdx)) {
            endIdx++;
        }
        // moved from this rulegroup to below this rulegroup
        if (inside(oldIdx) && below(newIdx)) {
            startIdx++;
        }
        // moved from this rulegroup to above this rulegroup
        if (inside(oldIdx) && above(newIdx)) {
            endIdx--;
        }
        // moved from below to above this rulegroup
        else if (below(oldIdx) && above(newIdx)) {
            startIdx--;
            endIdx--;
        }
        // moved from above to below this rulegroup
        else if (above(oldIdx) && below(newIdx)) {
            startIdx++;
            endIdx++;
        }
    }

    private boolean below(int idx) {
        return (idx < startIdx);
    }

    private boolean above(int idx) {
        return idx >= endIdx;
    }

    private boolean inside(int idx) {
        return idx >= startIdx && idx < endIdx;
    }

    public RuleGroup clone() {
        return new RuleGroup(this);
    }

    public int size() {
        return endIdx - startIdx;
    }

    public boolean isLocked() {
        return locked;
    }

    public String toString() {
        return "[" + name + "," + startIdx + "," + endIdx + "]" + ", LOCKED: " + locked;
    }
}
