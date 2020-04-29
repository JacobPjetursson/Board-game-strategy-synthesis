package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;

public class FFT_Follower {
    public int team;

    public FFT_Follower(int team) {
        this.team = team;
    }

    public FFTMove makeMove(FFTNode node) {
        if (FFTManager.currFFT == null)
            return null;
        for (RuleGroup ruleGroup : FFTManager.currFFT.ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                FFTMove move = rule.apply(node.getState()).getMove();
                if (move != null) {
                    System.out.println("Applying rule: " + rule);
                    return move;
                }
            }
        }
        System.out.print("No rules could be applied with a legal move. ");
        return null;
    }
}
