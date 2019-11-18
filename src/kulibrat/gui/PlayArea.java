package kulibrat.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import kulibrat.game.Controller;
import kulibrat.gui.board.PlayBox.PlayBox;
import kulibrat.gui.info.InfoPane;

import static misc.Config.CLICK_DEFAULT;
import static misc.Config.WIDTH;


public class PlayArea extends HBox {

    private PlayBox playBox;
    private InfoPane info;

    PlayArea(Controller cont) {
        setAlignment(Pos.CENTER);

        info = new InfoPane(cont.getScoreLimit(), cont.getMode());
        playBox = new PlayBox(60, CLICK_DEFAULT, cont);
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
