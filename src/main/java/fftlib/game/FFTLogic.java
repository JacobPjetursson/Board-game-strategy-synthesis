package fftlib.game;

public interface FFTLogic {

    boolean gameOver(FFTState state);

    int getWinner(FFTState state);

    boolean isLegalMove(FFTState state, FFTMove move);
}
