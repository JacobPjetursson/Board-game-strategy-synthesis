package gui.info;

import game.Controller;
import game.State;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import misc.Config;

import static misc.Config.*;

public class InfoPane extends VBox {
    private ScoreBoard scoreBoard;
    private Circle turnCircle;
    private Label skippedTurn;
    private Label turnNumberLabel;

    public InfoPane(int scoreLimit, int mode) {
        setPrefSize(Config.WIDTH / 3, Config.HEIGHT);
        setSpacing(40);
        setAlignment(Pos.CENTER);
        scoreBoard = new ScoreBoard();
        Label turnLabel = new Label("Turn: ");
        turnLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        turnCircle = new Circle(15);
        turnCircle.setFill(Color.RED);

        HBox turn = new HBox(turnLabel, turnCircle);
        turn.setAlignment(Pos.CENTER);
        turnNumberLabel = new Label("Turns Played: 0");
        turnNumberLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        Label scoreLimitLabel = new Label("Score limit: " + scoreLimit);
        scoreLimitLabel.setFont(Font.font("Verdana", 15));

        String modeText = (mode == HUMAN_VS_HUMAN) ? "Human vs. Human" :
                (mode == HUMAN_VS_AI) ? "Human vs. AI" : "AI vs. AI";
        Label modeLabel = new Label("Mode: " + modeText);
        modeLabel.setFont(Font.font("Verdana", 15));

        skippedTurn = new Label();
        skippedTurn.setFont(Font.font("Verdana", 15));
        VBox turnBox = new VBox(turn, turnNumberLabel);
        turnBox.setAlignment(Pos.CENTER);
        turnBox.setSpacing(15);

        VBox infoBox = new VBox(scoreLimitLabel, modeLabel, skippedTurn);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(15);

        getChildren().addAll(scoreBoard, turnBox, infoBox);
    }

    public void update(Controller cont) {
        State state = cont.getState();
        scoreBoard.updateScore(state);
        updateTurn(state);
        turnNumberLabel.setText("Turns Played: " + cont.getTurnNo());
        skippedTurn.setText("");
    }

    private void updateTurn(State state) {
        if (state.getTurn() == RED) {
            turnCircle.setFill(Color.RED);
        } else {
            turnCircle.setFill(Color.BLACK);
        }
    }

    public void displaySkippedTurn(String skippedTeam) {
        skippedTurn.setText("Team " + skippedTeam + "'s turn \nhas been skipped!");
    }
}
