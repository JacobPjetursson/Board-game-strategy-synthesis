package tictactoe.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import misc.Config;
import tictactoe.gui.menu.MenuPane;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tic Tac Toe");
        Config.CURRENT_GAME = Config.TICTACTOE;
        Scene menuScene = new Scene(new MenuPane(this), Config.WIDTH, Config.HEIGHT);
        primaryStage.setScene(menuScene);

        primaryStage.show();
    }
}
