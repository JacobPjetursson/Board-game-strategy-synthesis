package fftlib.logic.rule;

import fftlib.logic.FFT;

public class MetaRule {
    public int startIdx, endIdx;
    public String name;
    private boolean locked; // Are we allowed to modify this metarule (e.g. minimize?)
    private FFT fft;

    public MetaRule(FFT f, String name) {
        startIdx = -1;
        endIdx = -1;
        this.name = name;
        this.fft = f;
    }

    public MetaRule(FFT f, String name, int startIdx, int endIdx) {
        this(f, name);
        this.startIdx = startIdx;
        this.endIdx = endIdx;

    }

    public MetaRule(MetaRule copy) {
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
        if (idx < startIdx) { // before this metarule
            startIdx += changedIdx;
            endIdx += changedIdx;
        } else if (idx < endIdx) { // in this metarule
            endIdx += changedIdx;
        }
    }

    // used for moving rules
    public void updateIntervals(int oldIdx, int newIdx) {
        // moved from below this metarule into this metarule
        if (below(oldIdx) && inside(newIdx)) {
            startIdx--;
        }
        // moved from above this metarule into this metarule
        else if (above(oldIdx) && inside(newIdx)) {
            endIdx++;
        }
        // moved from this metarule to below this metarule
        if (inside(oldIdx) && below(newIdx)) {
            startIdx++;
        }
        // moved from this metarule to above this metarule
        if (inside(oldIdx) && above(newIdx)) {
            endIdx--;
        }
        // moved from below to above this metarule
        else if (below(oldIdx) && above(newIdx)) {
            startIdx--;
            endIdx--;
        }
        // moved from above to below this metarule
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

    public MetaRule clone() {
        return new MetaRule(this);
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
