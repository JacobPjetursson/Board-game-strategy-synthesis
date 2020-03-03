package tictactoe.ai;


import tictactoe.game.Move;
import tictactoe.game.State;

public interface AI {

    Move makeMove(State state);
}
