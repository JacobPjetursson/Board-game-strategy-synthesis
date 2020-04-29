package fftlib.game;

// TODO move into node class
public interface FFTLogic {

    boolean gameOver(FFTNode node);

    int getWinner(FFTNode node);

    boolean isLegalMove(FFTNode node, FFTMove move);
}
