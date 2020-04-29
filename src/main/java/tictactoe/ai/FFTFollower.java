package tictactoe.ai;

import fftlib.FFT_Follower;
import tictactoe.game.Move;
import tictactoe.game.Node;

// FIXME - dirty workaround
public class FFTFollower implements AI {
    int team;
    FFT_Follower fftFollower;

    public FFTFollower(int team) {
        this.team = team;
        fftFollower = new FFT_Follower(team);
    }

    @Override
    public Move makeMove(Node node) {
        return (Move) fftFollower.makeMove(node);
    }
}
