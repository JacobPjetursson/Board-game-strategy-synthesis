package fftlib.game;

import fftlib.Action;

public interface FFTMove {

    int getTeam();

    Action getAction();

    void setTeam(int team);

    String print();
}
