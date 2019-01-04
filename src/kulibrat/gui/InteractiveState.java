package kulibrat.gui;

import fftlib.game.FFTState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.board.Board;
import kulibrat.gui.board.Goal;
import kulibrat.gui.board.Player;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


public class InteractiveState implements InteractiveFFTState {
    Controller cont;

    public InteractiveState(Controller cont) {
        this.cont = cont;
    }

    public PlayBox getInteractiveFFTState(State s) {
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
        pb.addScore(s.getScoreLimit(), s.getScore(PLAYER1), s.getScore(PLAYER2));

        return pb;
    }

    @Override
    public Node getInteractiveFFTState(FFTState fftState) {
        return getInteractiveFFTState((State) fftState);
    }
}
