package tictactoe.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import fftlib.gui.FFTGameBoard;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.gui.board.Board;
import tictactoe.gui.board.Player;

import java.util.ArrayList;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class GameBoardPane implements FFTGameBoard {

    Controller cont;

    public GameBoardPane (Controller cont) {
        this.cont = cont;
    }

    public PlayBox getGameBoard(State s, Move move, ArrayList<Move> nonLosingPlays) {
        int tileW = 60;
        int pieceRad = 20;
        Board b = new Board(tileW, pieceRad);
        Player playerBlack = new Player(PLAYER2, cont, tileW);
        Player playerRed = new Player(PLAYER1, cont, tileW);

        PlayBox pb = new PlayBox(playerBlack, b, playerRed);
        pb.update(s);
        pb.addArrow(move, Color.BLUE);
        for (Move m : nonLosingPlays) {
            if (m.equals(move))
                continue;
            pb.addArrow(m, Color.GREEN);
        }
        return pb;
    }

    @Override
    public Node getGameBoard(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingPlays) {
        return getGameBoard((State) ps.getState(), (Move) ps.getMove(), (ArrayList<Move>) nonLosingPlays);
    }
}
