package tictactoe.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import tictactoe.game.Controller;
import tictactoe.gui.board.PlayBox.PlayBox;

import static misc.Config.CLICK_DEFAULT;
import static misc.Config.WIDTH;


public class PlayArea extends GridPane {

    private InfoPane info;
    private PlayBox playBox;

    PlayArea(Controller cont) {
        setPadding(new Insets(10, 10, 10, 10));
        setAlignment(Pos.CENTER);

        info = new InfoPane(cont.getMode());

        ColumnConstraints column = new ColumnConstraints(WIDTH / 3);
        for (int i = 0; i < 2; i++)
            getColumnConstraints().add(column);

        playBox = new PlayBox(90, CLICK_DEFAULT, cont);
        add(playBox, 0, 0);
        add(info, 1, 0);
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

