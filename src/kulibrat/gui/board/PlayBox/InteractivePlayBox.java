package kulibrat.gui.board.PlayBox;

import fftlib.Literal;
import fftlib.Rule;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.board.BoardTile;

import java.util.HashSet;

import static fftlib.Literal.*;
import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class InteractivePlayBox extends PlayBox {

    // fft stuff
    private boolean mandatoryScoreLimit;
    private boolean mandatoryScoreP1;
    private boolean mandatoryScoreP2;

    private TextField scoreLimitField;
    private TextField p1ScoreField;
    private TextField p2ScoreField;

    private CheckBox scoreLimitBox;
    private CheckBox p1ScoreBox;
    private CheckBox p2ScoreBox;

    public InteractivePlayBox(int tilesize, int clickMode, Controller cont) {
        super(tilesize, clickMode, cont);
        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(3);
        infoBox.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));


        Label scoreLimitLabel = new Label("Score limit:");
        scoreLimitLabel.setFont(Font.font("Verdana", 9));
        scoreLimitField = new TextField();
        scoreLimitField.setPrefWidth(40);
        scoreLimitBox = new CheckBox();

        Label p1ScoreLabel = new Label("Red points:");
        p1ScoreLabel.setFont(Font.font("Verdana", 9));
        p1ScoreField = new TextField();
        p1ScoreField.setPrefWidth(26);
        p1ScoreBox = new CheckBox();

        Label p2ScoreLabel = new Label("Black points:");
        p2ScoreLabel.setFont(Font.font("Verdana", 9));
        p2ScoreField = new TextField();
        p2ScoreField.setPrefWidth(26);
        p2ScoreBox = new CheckBox();

        infoBox.getChildren().addAll(scoreLimitLabel, scoreLimitField, scoreLimitBox,
                p1ScoreLabel, p1ScoreField, p1ScoreBox, p2ScoreLabel, p2ScoreField, p2ScoreBox);

        // FFT listeners
        scoreLimitBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            mandatoryScoreLimit = newValue;
            if (scoreLimitField.getText().equals(""))
                scoreLimitField.setText("0");
            cont.getInteractiveState().setScoreLimit(
                    Integer.parseInt(scoreLimitField.getText()), mandatoryScoreLimit);
        });
        p1ScoreBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            mandatoryScoreP1 = newValue;
            if (p1ScoreField.getText().equals(""))
                p1ScoreField.setText("0");
            cont.getInteractiveState().setScore(
                    PLAYER1, Integer.parseInt(p1ScoreField.getText()), mandatoryScoreP1);
        });
        p2ScoreBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            mandatoryScoreP2 = newValue;
            if (p2ScoreField.getText().equals(""))
                p2ScoreField.setText("0");
            cont.getInteractiveState().setScore(
                    PLAYER2, Integer.parseInt(p2ScoreField.getText()), mandatoryScoreP2);
        });
        scoreLimitField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                scoreLimitField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (mandatoryScoreLimit) {
                int sl = 0;
                if (!scoreLimitField.getText().isEmpty())
                    sl = Integer.parseInt(scoreLimitField.getText());
                cont.getInteractiveState().setScoreLimit(sl, mandatoryScoreLimit);
            }
        });
        p1ScoreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                p1ScoreField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (mandatoryScoreP1) {
                int p1Score = 0;
                if (!p1ScoreField.getText().isEmpty())
                    p1Score = Integer.parseInt(p1ScoreField.getText());
                cont.getInteractiveState().setScore(PLAYER1, p1Score, mandatoryScoreP1);
            }
        });
        p2ScoreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                p2ScoreField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (mandatoryScoreP2) {
                int p2Score = 0;
                if (!p2ScoreField.getText().isEmpty())
                    p2Score = Integer.parseInt(p2ScoreField.getText());
                cont.getInteractiveState().setScore(PLAYER2, p2Score, mandatoryScoreP2);
            }
        });

        playBox.getChildren().add(0,infoBox);
        updateBounds();
    }

    public void update(State s) {
        board.update(cont, s);
        playerRed.update(cont, s);
        playerBlack.update(cont, s);

        int scoreLimit = s.getScoreLimit();
        int player1Score = s.getScore(PLAYER1);
        int player2Score = s.getScore(PLAYER2);

        setFields(scoreLimit, player1Score, player2Score);
    }

    private void setFields(int scoreLimit, int player1Score, int player2Score) {
        String slStr = Integer.toString(scoreLimit);
        scoreLimitField.setText(slStr);

        String p1Str = Integer.toString(player1Score);
        p1ScoreField.setText(p1Str);


        String p2Str = Integer.toString(player2Score);
        p2ScoreField.setText(p2Str);
    }



    public void update(Rule r, int perspective) {
        HashSet<Literal> nonBoardLits = r.preconditions.extractNonBoardPlacements();
        int[][] preconBoard = Rule.preconsToBoard(r.preconditions);
        Move m = (Move) r.action.getMove(perspective);


        int enemy = (perspective == PLAYER1) ? PLAYER2 : PLAYER1;
        BoardTile[][] tiles = board.getTiles();

        for (int i = 0; i < preconBoard.length; i++) {
            for (int j = 0; j < preconBoard[i].length; j++) {
                int occ = preconBoard[i][j];
                BoardTile tile = tiles[i][j];
                tile.setMandatory(occ != 0);
                if (Math.abs(occ) == PIECEOCC_PLAYER)
                    tile.addPiece(perspective, clickMode);
                else if (Math.abs(occ) == PIECEOCC_ENEMY)
                    tile.addPiece(enemy, clickMode);


                if (occ == -PIECEOCC_ANY)
                    tile.setNegated(false);
                else if (occ == PIECEOCC_ANY)
                    tile.setNegated(true);
                else
                    tile.setNegated(occ < 0);


            }
        }
        for (Literal l : nonBoardLits) {
            String[] slSplit = l.name.toLowerCase().split("sl=");
            String[] p1ScoreSplit = l.name.toLowerCase().split("p1score=");
            String[] p2ScoreSplit = l.name.toLowerCase().split("p2score=");
            if (slSplit.length == 2) {
                scoreLimitField.setText(slSplit[1]);
                scoreLimitBox.setSelected(true);
            } else if (p1ScoreSplit.length == 2) {
                p1ScoreField.setText(p1ScoreSplit[1]);
                p1ScoreBox.setSelected(true);
            } else if (p2ScoreSplit.length == 2) {
                p2ScoreField.setText(p2ScoreSplit[1]);
                p2ScoreBox.setSelected(true);
            }
        }
        removeArrows();
        if (m != null)
            addArrow(m, Color.BLUE);

    }
}
