package fftlib.game;

import fftlib.logic.Action;

public interface FFTMove {

    int getTeam();

    void setTeam(int team);

    Action convert();
}
