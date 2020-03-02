package tictactoe.gui;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import misc.Globals;
import tictactoe.game.Controller;

public class PlayPane extends HBox {
    private PlayArea playArea;
    private NavPane navPane;

    public PlayPane(Controller cont) {
        setup();
        playArea = new PlayArea(cont);
        navPane = new NavPane(cont);
        getChildren().addAll(navPane, playArea);
    }

    private void setup() {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPrefSize(Globals.WIDTH, Globals.HEIGHT);
        setStyle("-fx-background-color: rgb(255, 255, 255);");
    }

    public PlayArea getPlayArea() {
        return playArea;
    }

    public NavPane getNavPane() {
        return navPane;
    }
}
