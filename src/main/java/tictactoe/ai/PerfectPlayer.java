package tictactoe.ai;

import fftlib.game.FFTSolution;
import fftlib.game.StateMapping;
import tictactoe.game.Move;
import tictactoe.game.State;

import static misc.Globals.PLAYER2;

public class PerfectPlayer implements AI {
    int team;

    public PerfectPlayer(int team) {
        this.team = team;
    }
    // This function fetches the best move from the DB, if it exists
    @Override
    public Move makeMove(State state) {
        String teamstr = (team == PLAYER2) ? "Nought" : "Cross";
        System.out.println("Finding best play for " + teamstr);
        if (state.getLegalMoves().size() == 1) {
            return state.getLegalMoves().get(0);
        }
        // table lookup
        StateMapping mapping = FFTSolution.queryState(state);

        return (Move)mapping.move;
    }
}
