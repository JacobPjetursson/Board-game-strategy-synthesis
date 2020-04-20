package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;

public class FFT_Follower {
    public int team;

    public FFT_Follower(int team) {
        this.team = team;
    }

    public FFTMove makeMove(FFTState state) {
        if (FFTManager.currFFT == null)
            return null;
        for (RuleGroup ruleGroup : FFTManager.currFFT.ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                FFTMove move = rule.apply(state);
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
