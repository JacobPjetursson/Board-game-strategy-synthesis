package tictactoe.gui;

import fftlib.FFT;
import fftlib.game.FFTNode;
import fftlib.gui.ShowFFTPane;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import tictactoe.game.Controller;

import static misc.Globals.*;


public class InfoPane extends AnchorPane {
    private int mode;
    private Label skippedTurn;
    private Label turnNumberLabel;
    private Label turnLabel;
    private ShowFFTPane showFFTPane;

    private FFTNode node;
    private FFT currFFT;

    public InfoPane(int mode) {
        this.mode = mode;

        turnLabel = new Label("Turn: Cross");
        turnLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        turnLabel.setTextAlignment(TextAlignment.LEFT);
        turnNumberLabel = new Label("Turns Played: 0");
        turnNumberLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        turnNumberLabel.setTextAlignment(TextAlignment.LEFT);

        String modeText = (mode == HUMAN_VS_HUMAN) ? "Human vs. Human" :
                (mode == HUMAN_VS_AI) ? "Human vs. AI" : "AI vs. AI";
        Label modeLabel = new Label("Mode: " + modeText);
        modeLabel.setFont(Font.font("Verdana", 10));
        modeLabel.setTextAlignment(TextAlignment.LEFT);

        skippedTurn = new Label();
        skippedTurn.setFont(Font.font("Verdana", 10));
        VBox infoBox = new VBox(turnLabel, turnNumberLabel, modeLabel, skippedTurn);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setSpacing(8);

        getChildren().add(infoBox);
        AnchorPane.setBottomAnchor(infoBox, -10.0);
        AnchorPane.setRightAnchor(infoBox, 10.0);

        // ShowFFTPane
        showFFTPane = new ShowFFTPane();
        showFFTPane.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(showFFTPane, 0.0);
        AnchorPane.setBottomAnchor(showFFTPane, 0.0);
        getChildren().add(showFFTPane);
    }

    public void update(Controller cont) {
        currFFT = cont.getCurrFFT();
        node = cont.getNode();
        if (mode != AI_VS_AI)
            showFFTPane.update(currFFT, node);
        turnNumberLabel.setText("Turns Played: " + cont.getTurnNo());
        skippedTurn.setText("");
        if (node.getTurn() == PLAYER1)
            turnLabel.setText("Turn: Cross");
        else
            turnLabel.setText("Turn: Circle");
    }

    public void displaySkippedTurn(String skippedTeam) {
        skippedTurn.setText("Team " + skippedTeam + "'s turn \nhas been skipped!");
    }
}
