package tictactoe.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import fftlib.gui.FFTFailState;
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

public class FailStatePane implements FFTFailState {

    Controller cont;

    public FailStatePane(Controller cont) {
        this.cont = cont;
    }

    public PlayBox getFailStatePane(State s, Move move, ArrayList<Move> nonLosingPlays) {
        int tileW = 60;
        Board b = new Board(tileW, false, cont);
        Player playerBlack = new Player(PLAYER2, cont, tileW, false);
        Player playerRed = new Player(PLAYER1, cont, tileW, false);

        PlayBox pb = new PlayBox(playerBlack, b, playerRed);
        pb.update(s);
        pb.addHighlight(move, Color.BLUE);
        for (Move m : nonLosingPlays) {
            if (m.equals(move))
                continue;
            pb.addHighlight(m, Color.GREEN);
        }
        return pb;
    }

    @Override
    public Node getFailState(FFTStateAndMove ps, ArrayList<? extends FFTMove> nonLosingPlays) {
        return getFailStatePane((State) ps.getState(), (Move) ps.getMove(), (ArrayList<Move>) nonLosingPlays);
    }
}
