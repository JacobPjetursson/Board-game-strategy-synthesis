package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;

public class FFT_Follower {
    public int team;
    private FFTManager fftManager;

    public FFT_Follower(int team, FFTManager fftManager) {
        this.team = team;
        this.fftManager = fftManager;
    }

    public FFTMove makeMove(FFTState state) {
        if (fftManager.currFFT == null)
            return null;
        for (RuleGroup ruleGroup : fftManager.currFFT.ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                FFTMove move = rule.apply(state);
                if (move != null) {
                    System.out.println("Applying rule: " + rule.print());
                    return move;
                }
            }
        }
        System.out.print("No rules could be applied with a legal move. ");
        return null;
    }
}
