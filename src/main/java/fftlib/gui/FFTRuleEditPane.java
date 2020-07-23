package fftlib.gui;

import fftlib.logic.rule.Rule;
import javafx.scene.Node;

public abstract class FFTRuleEditPane  {
    protected Node node;
    protected Rule rule;

    public Node getNode() {
        return node;
    }

    public abstract void update(Rule r);

    public abstract Rule getRule();

    public abstract void clear();
}
