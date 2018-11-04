package kulibrat.ai;

import kulibrat.game.Move;
import kulibrat.game.State;

public abstract class AI {
    public int team;

    protected AI(int team) {
        this.team = team;
    }

    public abstract Move makeMove(State state);

    public void update(State state) {
    }
}
