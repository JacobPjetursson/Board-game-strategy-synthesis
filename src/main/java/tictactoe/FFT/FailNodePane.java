package tictactoe.FFT;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.FFTSolution;
import fftlib.game.NodeMapping;
import fftlib.gui.FFTFailNode;
import javafx.application.Platform;
import tictactoe.game.Controller;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.Node;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.PlayBox.PlayBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static misc.Globals.CLICK_DISABLED;
import static misc.Globals.PLAYER_NONE;

public class FailNodePane implements FFTFailNode {

    Controller cont;

    FailNodePane(Controller cont) {
        this.cont = cont;
    }

    private PlayBox getFailNodePane(Node n) {
        int tilesize = 60;
        PlayBox pb = new PlayBox(tilesize, CLICK_DISABLED, cont);
        pb.update(n);
        ArrayList<Move> legalMoves = Logic.legalMoves(n.getTurn(), n);
        Set<? extends FFTMove> chosenMoves = cont.getCurrFFT().apply(n).getMoves();
        BoardTile[][] tiles = pb.getBoard().getTiles();
        Platform.runLater(() -> {

            for (BoardTile[] tile : tiles) {
                for (BoardTile aTile : tile) {
                    for (Move m : legalMoves) {
                        if (m.col == aTile.getCol() && m.row == aTile.getRow()) {
                            Node next = n.getNextState(m);
                            NodeMapping nm = FFTSolution.queryNode(next);
                            if (nm == null) { // terminal state
                                if (Logic.getWinner(next) == n.getTurn())
                                    aTile.setGreen();
                                else if (Logic.getWinner(next) == PLAYER_NONE)
                                    aTile.setYellow();
                                else
                                    aTile.setRed();
                            } else {
                                if (nm.getWinner() == n.getTurn())
                                    aTile.setGreen();
                                else if (nm.getWinner() == PLAYER_NONE)
                                    aTile.setYellow();
                                else
                                    aTile.setRed();
                            }
                        }
                    }
                    for (FFTMove move : chosenMoves) {
                        Move m = (Move) move;
                        if (m.col == aTile.getCol() && m.row == aTile.getRow())
                            aTile.setFFTChosen(m.team);
                    }
                }
            }
        });
        return pb;
    }

    @Override
    public javafx.scene.Node getFailNode(FFTNode n) {
        return getFailNodePane((Node) n);
    }
}
