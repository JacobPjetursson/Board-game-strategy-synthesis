package kulibrat.ai;

import kulibrat.game.Move;
import kulibrat.game.Node;

public abstract class AI {
    public int team;

    protected AI(int team) {
        this.team = team;
    }

    public abstract Move makeMove(Node node);

    public void update(Node node) {
    }
}
