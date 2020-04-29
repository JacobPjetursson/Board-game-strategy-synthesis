package kulibrat.gui.board.PlayBox;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import kulibrat.game.Controller;
import kulibrat.game.Node;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class StaticPlayBox extends PlayBox {

    // fft stuff
    private boolean mandatoryScoreLimit;
    private boolean mandatoryScoreP1;
    private boolean mandatoryScoreP2;

    private Label scoreLimitLabel;
    private Label p1ScoreLabel;
    private Label p2ScoreLabel;

    public StaticPlayBox(int tilesize, int CLICK_MODE, Controller cont) {
        super(tilesize, CLICK_MODE, cont);
        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(7);
        infoBox.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        scoreLimitLabel = new Label("Score limit: ");
        scoreLimitLabel.setFont(Font.font("Verdana", tilesize/5));

        p1ScoreLabel = new Label("Red points: ");
        p1ScoreLabel.setFont(Font.font("Verdana", tilesize/5));

        p2ScoreLabel = new Label("Black points: ");
        p2ScoreLabel.setFont(Font.font("Verdana", tilesize/5));

        infoBox.getChildren().addAll(scoreLimitLabel, p1ScoreLabel, p2ScoreLabel);

        playBox.getChildren().add(0,infoBox);
    }

    public void update(Node n) {
        board.update(cont, n);
        playerRed.update(cont, n);
        playerBlack.update(cont, n);

        int scoreLimit = n.getScoreLimit();
        int player1Score = n.getScore(PLAYER1);
        int player2Score = n.getScore(PLAYER2);

        String slStr = Integer.toString(scoreLimit);
        scoreLimitLabel.setText("Score limit: " + slStr);

        String p1Str = Integer.toString(player1Score);
        p1ScoreLabel.setText("Red points: " + p1Str);


        String p2Str = Integer.toString(player2Score);
        p2ScoreLabel.setText("Black points: " + p2Str);
    }
}
