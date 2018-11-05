package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import misc.Config;

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
                for (int symmetry : Config.SYMMETRIES) {
                    if (rule.applies(state, symmetry)) {
                        Action action = rule.action.applySymmetry(symmetry);
                        FFTMove move = action.getMove();
                        move.setTeam(state.getTurn());
                        if (FFTManager.logic.isLegalMove(state, move)) {
                            System.out.println("Applying rule: " + rule.printRule());
                            return move;
                        }
                    }
                }
            }
        }
        System.out.print("No rules could be applied with a legal move. ");
        return null;
    }
}
