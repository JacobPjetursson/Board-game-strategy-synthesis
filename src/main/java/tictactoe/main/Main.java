package tictactoe.main;

import fftlib.FFTAutoGen;
import fftlib.FFTManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import misc.Globals;
import tictactoe.FFT.GameSpecifics;
import tictactoe.gui.menu.MenuPane;

import static misc.Config.SHOW_GUI;

public class Main extends Application {

    public static void main(String[] args) {
        Globals.CURRENT_GAME = Globals.TICTACTOE;
        if (SHOW_GUI)
            launch(args);
        else {
            GameSpecifics specs = new GameSpecifics();
            FFTManager.initialize(specs);
            FFTAutoGen.synthesize();
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
