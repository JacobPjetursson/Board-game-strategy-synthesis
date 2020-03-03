package tictactoe.ai;

import fftlib.FFTManager;
import fftlib.FFT_Follower;
import tictactoe.game.Move;
import tictactoe.game.State;

// FIXME - dirty workaround
public class FFTFollower implements AI {
    int team;
    FFT_Follower fftFollower;

    public FFTFollower(int team, FFTManager fftManager) {
        this.team = team;
        fftFollower = new FFT_Follower(team, fftManager);
    }

    @Override
    public Move makeMove(State state) {
        return (Move) fftFollower.makeMove(state);
    }
}
