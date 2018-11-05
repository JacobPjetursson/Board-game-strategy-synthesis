package tictactoe.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import misc.Config;
import tictactoe.game.Controller;
import tictactoe.game.State;

import static misc.Config.*;


public class InfoPane extends VBox {
    private Circle turnCircle;
    private Label skippedTurn;
    private Label turnNumberLabel;

    public InfoPane(int mode) {
        setPrefSize(Config.WIDTH / 3, Config.HEIGHT);
        setSpacing(40);
        setAlignment(Pos.CENTER);
        Label turnLabel = new Label("Turn: ");
        turnLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        turnCircle = new Circle(15);
        turnCircle.setFill(Color.RED);

        HBox turn = new HBox(turnLabel, turnCircle);
        turn.setAlignment(Pos.CENTER);
        turnNumberLabel = new Label("Turns Played: 0");
        turnNumberLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        String modeText = (mode == HUMAN_VS_HUMAN) ? "Human vs. Human" :
                (mode == HUMAN_VS_AI) ? "Human vs. AI" : "AI vs. AI";
        Label modeLabel = new Label("Mode: " + modeText);
        modeLabel.setFont(Font.font("Verdana", 15));

        skippedTurn = new Label();
        skippedTurn.setFont(Font.font("Verdana", 15));
        VBox turnBox = new VBox(turn, turnNumberLabel);
        turnBox.setAlignment(Pos.CENTER);
        turnBox.setSpacing(15);

        VBox infoBox = new VBox(modeLabel, skippedTurn);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(15);

        getChildren().addAll(turnBox, infoBox);
    }

    public void update(Controller cont) {
        State state = cont.getState();
        updateTurn(state);
        turnNumberLabel.setText("Turns Played: " + cont.getTurnNo());
        skippedTurn.setText("");
    }

    private void updateTurn(State state) {
        if (state.getTurn() == PLAYER1) {
            turnCircle.setFill(Color.RED);
        } else {
            turnCircle.setFill(Color.BLACK);
        }
    }

    public void displaySkippedTurn(String skippedTeam) {
        skippedTurn.setText("Team " + skippedTeam + "'s turn \nhas been skipped!");
    }
}
