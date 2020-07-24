package fftlib.gui;

import fftlib.logic.rule.Rule;
import javafx.scene.Node;

public abstract class FFTRuleEditPane  {
    protected Node node;
    protected Rule rule;
    protected FFTEditPane fftEditPane;

    public Node getNode() {
        return node;
    }

    public abstract void update(Rule r);

    public abstract void disable();

    public abstract void enable();

    public abstract Rule getRule();

    public abstract void clear();

    public void setFftEditPane(FFTEditPane pane) {
        this.fftEditPane = pane;
    }
}
