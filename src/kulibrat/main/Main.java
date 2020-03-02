package kulibrat.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kulibrat.gui.menu.MenuPane;
import misc.Globals;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
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
