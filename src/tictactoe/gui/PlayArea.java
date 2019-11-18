package tictactoe.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import tictactoe.game.Controller;
import tictactoe.gui.board.PlayBox.PlayBox;

import static misc.Config.CLICK_DEFAULT;
import static misc.Config.WIDTH;


public class PlayArea extends HBox {

    private InfoPane info;
    private PlayBox playBox;

    PlayArea(Controller cont) {
        setPadding(new Insets(10, 10, 10, 10));
        setSpacing(20);
        setAlignment(Pos.CENTER);

        info = new InfoPane(cont.getMode());
        playBox = new PlayBox(90, CLICK_DEFAULT, cont);
        getChildren().addAll(playBox, info);
    }

    public void update(Controller cont) {
        playBox.update(cont.getState());
        info.update(cont);
    }

    public InfoPane getInfoPane() {
        return info;
    }

    public PlayBox getPlayBox() {
        return playBox;
    }
}

