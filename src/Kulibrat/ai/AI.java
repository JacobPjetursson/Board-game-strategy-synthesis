package ai;

import game.Move;
import game.State;

public abstract class AI {
    public int team;

    protected AI(int team) {
        this.team = team;
    }

    public abstract Move makeMove(State state);

    public void update(State state) {
    }
}
