package tictactoe.main;

import fftlib.FFTManager;
import fftlib.game.FFTSolver;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import misc.Globals;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.State;
import tictactoe.gui.menu.MenuPane;

import static misc.Config.TIC_TAC_TOE_SHOW_GUI;

public class Main extends Application {

    public static void main(String[] args) {
        Globals.CURRENT_GAME = Globals.TICTACTOE;
        if (TIC_TAC_TOE_SHOW_GUI)
            launch(args);
        else {
            GameSpecifics specs = new GameSpecifics();
            FFTManager.initialize(specs);
            FFTSolver.solveGame(new State());
            FFTManager.autogenFFT();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tic Tac Toe");
        Scene menuScene = new Scene(new MenuPane(this), Globals.WIDTH, Globals.HEIGHT);
        primaryStage.setScene(menuScene);

        primaryStage.show();
    }
}
