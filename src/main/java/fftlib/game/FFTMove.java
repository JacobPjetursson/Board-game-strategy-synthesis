package fftlib.game;

import fftlib.Action;

public interface FFTMove {

    int getTeam();

    void setTeam(int team);

    Action convert();
}
