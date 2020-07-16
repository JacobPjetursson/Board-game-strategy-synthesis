package fftlib.game;

import fftlib.logic.rule.Action;

public interface FFTMove {

    int getTeam();

    void setTeam(int team);

    Action convert();
}
