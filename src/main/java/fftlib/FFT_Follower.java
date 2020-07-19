package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.logic.rule.Rule;
import fftlib.logic.rule.RuleEntity;
import fftlib.logic.rule.RuleGroup;

import java.util.HashSet;

public class FFT_Follower {
    public int team;

    public FFT_Follower(int team) {
        this.team = team;
    }

    public FFTMove makeMove(FFTNode node) {
        if (FFTManager.currFFT == null)
            return null;

        HashSet<FFTMove> moves = FFTManager.currFFT.apply(node);
        if (!moves.isEmpty())
            return moves.iterator().next();

        System.out.print("No rules could be applied with a legal move. ");
        return null;
    }
}
