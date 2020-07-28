package fftlib.game;

import fftlib.FFTManager;
import fftlib.logic.rule.Action;

public abstract class FFTMove {

    public int team;

    public int getTeam() {
        return team;
    }

    public Action convert() {
        return FFTManager.moveToAction.apply(this);
    }

    public abstract FFTMove clone();

    public abstract boolean equals(Object o);

    public abstract int hashCode();


}
