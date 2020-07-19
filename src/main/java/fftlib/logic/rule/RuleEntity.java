package fftlib.logic.rule;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;

import java.util.HashSet;

public interface RuleEntity {

    HashSet<FFTMove> apply(FFTNode n);

    HashSet<FFTMove> apply(FFTNode n, boolean safe);

    RuleEntity clone();

    int size();

    boolean isLocked();

    int getAmountOfPreconditions();
}
