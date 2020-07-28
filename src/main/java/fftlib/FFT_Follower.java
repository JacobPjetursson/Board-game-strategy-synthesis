package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;

import java.util.HashSet;
import java.util.Set;

public class FFT_Follower {
    public int team;

    public FFT_Follower(int team) {
        this.team = team;
    }

    public FFTMove makeMove(FFTNode node) {
        if (FFTManager.currFFT == null)
            return null;

        Set<FFTMove> moves = FFTManager.currFFT.apply(node).getMoves();
        if (!moves.isEmpty())
            return moves.iterator().next();

        System.out.print("No rules could be applied with a legal move. ");
        return null;
    }
}
