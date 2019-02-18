package tictactoe.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import tictactoe.game.Controller;
import tictactoe.gui.board.Board;
import tictactoe.gui.board.Player;

import static misc.Config.*;


public class PlayArea extends GridPane {

    private Player playerCross;
    private Player playerCircle;
    private Board board;
    private InfoPane info;
    private PlayBox playBox;

    PlayArea(Controller cont) {
        setPadding(new Insets(10, 10, 10, 10));
        setAlignment(Pos.CENTER);

        info = new InfoPane(cont.getMode());

        ColumnConstraints column = new ColumnConstraints(WIDTH / 3);
        for (int i = 0; i < 2; i++)
            getColumnConstraints().add(column);

        playBox = new PlayBox(60, CLICK_DEFAULT, cont);
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
