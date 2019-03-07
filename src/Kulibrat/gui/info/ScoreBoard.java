package kulibrat.gui.info;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import kulibrat.game.State;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class ScoreBoard extends VBox {
    private Label redLabel;
    private Label blackLabel;

    public ScoreBoard() {
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(20));
        Label score = new Label("Score: ");
        score.setFont(Font.font("Verdana", FontWeight.BOLD, 28));

        blackLabel = new Label("Black: " + 0);
        blackLabel.setTextFill(Color.BLACK);
        blackLabel.setFont(Font.font("Verdana", 22));
        redLabel = new Label("Red: " + 0);
        redLabel.setTextFill(Color.RED);
        redLabel.setFont(Font.font("Verdana", 22));

        getChildren().addAll(score, blackLabel, redLabel);
    }

    void updateScore(State state) {
        redLabel.setText("Red: " + state.getScore(PLAYER1));
        blackLabel.setText("Black: " + state.getScore(PLAYER2));
    }
}
