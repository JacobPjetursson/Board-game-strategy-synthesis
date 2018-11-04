package fftlib;

import kulibrat.ai.AI;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.misc.Config;

public class FFT_Follower extends AI {
    private FFTManager fftManager;
    public int team;

    public FFT_Follower(int team, FFTManager fftManager) {
        super(team);
        this.fftManager = fftManager;
    }

    public Move makeMove(State state) {
        if (fftManager.currFFT == null)
            return null;
        for (RuleGroup ruleGroup : fftManager.currFFT.ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                for(int symmetry : Config.SYMMETRY) {
                    if (rule.applies(state, symmetry)) {
                        Action action = rule.action.applySymmetry(symmetry);
                        Move move = action.getMove();
                        move.team = state.getTurn();
                        if (Logic.isLegalMove(state, move)) {
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
