package tictactoe.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import misc.Globals;
import tictactoe.gui.menu.MenuPane;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tic Tac Toe");
        Globals.CURRENT_GAME = Globals.TICTACTOE;
        Scene menuScene = new Scene(new MenuPane(this), Globals.WIDTH, Globals.HEIGHT);
        primaryStage.setScene(menuScene);

        primaryStage.show();
    }
}
