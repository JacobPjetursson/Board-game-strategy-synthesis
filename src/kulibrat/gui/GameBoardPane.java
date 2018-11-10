package kulibrat.gui;

import fftlib.game.FFTMove;
import fftlib.game.FFTStateAndMove;
import fftlib.gui.FFTGameBoard;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.game.StateAndMove;
import kulibrat.gui.board.Board;
import kulibrat.gui.board.Goal;
import kulibrat.gui.board.Player;

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
        int goalH = 50;
        Board b = new Board(tileW, pieceRad, false);
        Player playerBlack = new Player(PLAYER2, cont, tileW, pieceRad, false);
        Goal goalRed = new Goal(3 * b.getTileSize(), goalH);
        Goal goalBlack = new Goal(3 * b.getTileSize(), goalH);
        Player playerRed = new Player(PLAYER1, cont, tileW, pieceRad, false);

        PlayBox pb = new PlayBox(playerBlack, goalRed, b, goalBlack, playerRed);
        pb.update(cont, s);
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
