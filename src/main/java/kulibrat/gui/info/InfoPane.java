package kulibrat.gui.info;

import fftlib.FFT;
import fftlib.gui.ShowFFTPane;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import kulibrat.game.Controller;
import kulibrat.game.Node;

import static misc.Globals.*;


public class InfoPane extends AnchorPane {
    private ScoreBoard scoreBoard;
    private Circle turnCircle;
    private Label skippedTurn;
    private Label turnNumberLabel;
    private ShowFFTPane showFFTPane;
    private FFT fft;
    private int mode;

    public InfoPane(int scoreLimit, int mode) {
        this.mode = mode;
        scoreBoard = new ScoreBoard();
        Label turnLabel = new Label("Turn: ");
        turnLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
        turnCircle = new Circle(8);
        turnCircle.setFill(Color.RED);

        HBox turn = new HBox(turnLabel, turnCircle);
        turn.setAlignment(Pos.CENTER);
        turnNumberLabel = new Label("Turns Played: 0");
        turnNumberLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));

        Label scoreLimitLabel = new Label("Score limit: " + scoreLimit);
        scoreLimitLabel.setFont(Font.font("Verdana", 11));

        String modeText = (mode == HUMAN_VS_HUMAN) ? "Human vs. Human" :
                (mode == HUMAN_VS_AI) ? "Human vs. AI" : "AI vs. AI";
        Label modeLabel = new Label("Mode: " + modeText);
        modeLabel.setFont(Font.font("Verdana", 11));

        skippedTurn = new Label();
        skippedTurn.setFont(Font.font("Verdana", 11));
        VBox turnBox = new VBox(turn, turnNumberLabel);
        turnBox.setAlignment(Pos.CENTER);

        VBox gameInfoBox = new VBox(scoreLimitLabel, modeLabel, skippedTurn);
        gameInfoBox.setAlignment(Pos.CENTER);

        VBox infoBox = new VBox(scoreBoard, turnBox, gameInfoBox);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(8);

        getChildren().add(infoBox);
        AnchorPane.setBottomAnchor(infoBox, -10.0);
        AnchorPane.setRightAnchor(infoBox, 10.0);

        // ShowFFTPane
        if (mode == HUMAN_VS_AI) {
            showFFTPane = new ShowFFTPane();
            showFFTPane.setAlignment(Pos.CENTER);
            getChildren().add(showFFTPane);
        }
    }

    public void update(Controller cont) {
        Node node = cont.getNode();
        fft = cont.getCurrFFT();
        if (mode == HUMAN_VS_AI)
            showFFTPane.update(fft, node);

        scoreBoard.updateScore(node);

        if (node.getTurn() == PLAYER1)
            turnCircle.setFill(Color.RED);
        else
            turnCircle.setFill(Color.BLACK);

        turnNumberLabel.setText("Turns Played: " + cont.getTurnNo());
        skippedTurn.setText("");
    }

    public void displaySkippedTurn(String skippedTeam) {
        skippedTurn.setText("Team " + skippedTeam + "'s turn \nhas been skipped!");
    }
}
