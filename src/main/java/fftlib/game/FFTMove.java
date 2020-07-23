package fftlib.game;

import fftlib.FFTManager;
import fftlib.logic.rule.Action;

public abstract class FFTMove {

    public int team;

    public int getTeam() {
        return team;
    }

    void setTeam(int team) {
        this.team = team;
    }

    public Action convert() {
        return FFTManager.moveToAction.apply(this);
    }

    public abstract FFTMove clone();


}
