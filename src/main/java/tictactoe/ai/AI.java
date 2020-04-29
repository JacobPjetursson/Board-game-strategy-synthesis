package tictactoe.ai;


import tictactoe.game.Move;
import tictactoe.game.Node;

public interface AI {

    Move makeMove(Node node);
}
