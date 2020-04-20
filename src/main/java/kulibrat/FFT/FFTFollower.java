package kulibrat.FFT;

import fftlib.FFT_Follower;
import kulibrat.ai.AI;
import kulibrat.game.Move;
import kulibrat.game.State;

// FIXME - dirty workaround
public class FFTFollower extends AI {
    FFT_Follower fftFollower;

    public FFTFollower(int team) {
        super(team);
        fftFollower = new FFT_Follower(team);
    }

    @Override
    public Move makeMove(State state) {
        return (Move) fftFollower.makeMove(state);
    }
}
