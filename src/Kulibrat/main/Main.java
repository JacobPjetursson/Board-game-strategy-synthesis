package kulibrat.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kulibrat.ai.Minimax.Zobrist;
import kulibrat.gui.menu.MenuPane;
import misc.Config;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Kulibrat");
        Config.CURRENT_GAME = Config.KULIBRAT;
        Scene menuScene = new Scene(new MenuPane(this), Config.WIDTH, Config.HEIGHT);
        primaryStage.setScene(menuScene);
        Zobrist.initialize();

        primaryStage.show();
    }
}
