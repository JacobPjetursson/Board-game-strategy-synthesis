package kulibrat.main;

import fftlib.FFTManager;
import fftlib.game.FFTSolver;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kulibrat.FFT.GameSpecifics;
import kulibrat.game.Node;
import kulibrat.gui.menu.MenuPane;
import misc.Globals;


import static misc.Config.KULIBRAT_SHOW_GUI;

public class Main extends Application {

    public static void main(String[] args) {
        Globals.CURRENT_GAME = Globals.KULIBRAT;
        if (KULIBRAT_SHOW_GUI)
            launch(args);
        else {
            GameSpecifics specs = new GameSpecifics();
            FFTManager.initialize(specs);
            FFTSolver.solveGame();
            FFTManager.autogenFFT();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("kulibrat");
        Globals.CURRENT_GAME = Globals.KULIBRAT;
        Scene menuScene = new Scene(new MenuPane(this), Globals.WIDTH, Globals.HEIGHT);
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }
}
