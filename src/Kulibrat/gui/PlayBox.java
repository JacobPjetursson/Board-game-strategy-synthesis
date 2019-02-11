package kulibrat.gui;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import kulibrat.FFT.InteractiveState;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.board.Board;
import kulibrat.gui.board.BoardTile;
import kulibrat.gui.board.Goal;
import kulibrat.gui.board.Player;
import kulibrat.gui.menu.Arrow;

import java.util.ArrayList;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class PlayBox extends Group {
    private Board board;
    private Goal goalRed;
    private Goal goalBlack;
    private Player playerRed;
    private Player playerBlack;
    private VBox playBox;
    private Controller cont;

    private ArrayList<Arrow> arrows;

    // fft stuff
    private boolean mandatoryScoreLimit;
    private boolean mandatoryScoreP1;
    private boolean mandatoryScoreP2;

    public PlayBox(int tilesize, int CLICK_MODE, Controller cont) {
        this.cont = cont;
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: rgb(255, 255, 255);");
        int pieceRadius = tilesize / 3;
        int goalHeight = (tilesize / 6) * 5;

        board = new Board(tilesize, pieceRadius, CLICK_MODE, cont);
        playerRed = new Player(PLAYER1, cont, tilesize, pieceRadius, CLICK_MODE);
        playerBlack = new Player(PLAYER2, cont, tilesize, pieceRadius, CLICK_MODE);
        goalRed = new Goal(3 * tilesize, goalHeight, CLICK_MODE, cont);
        goalBlack = new Goal(3 * tilesize, goalHeight, CLICK_MODE, cont);
        vbox.getChildren().addAll(playerBlack, goalRed, board, goalBlack, playerRed);
        getChildren().add(vbox);
        this.playBox = vbox;
        arrows = new ArrayList<>();
        updateBounds();
    }

    public void update(State s) {
        board.update(cont, s);
        playerRed.update(cont, s);
        playerBlack.update(cont, s);
    }

    protected void layoutChildren() {
        super.layoutChildren();
    }

    public Board getBoard() {
        return board;
    }

    public Goal getGoal(int team) {
        if (team == PLAYER1)
            return goalRed;
        else return goalBlack;
    }

    public Player getPlayer(int team) {
        if (team == PLAYER1)
            return playerRed;
        else return playerBlack;
    }

    public void addScore(int scoreLimit, int player1Score, int player2Score, boolean interactive) {
        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(7);
        infoBox.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        String slStr = interactive ? "" : " " + scoreLimit;
        Label scoreLimitLabel = new Label("Score limit:" + slStr);
        scoreLimitLabel.setFont(Font.font("Verdana", 10));
        TextField scoreLimitField = new TextField(Integer.toString(scoreLimit));
        scoreLimitField.setPrefWidth(40);
        CheckBox scoreLimitBox = new CheckBox();

        String p1Str = interactive ? "" : " " + player1Score;
        Label p1ScoreLabel = new Label("Red points:" + p1Str);
        p1ScoreLabel.setFont(Font.font("Verdana", 10));
        TextField p1ScoreField = new TextField(Integer.toString(player1Score));
        p1ScoreField.setPrefWidth(30);
        CheckBox p1ScoreBox = new CheckBox();

        String p2Str = interactive ? "" : " " + player2Score;
        Label p2ScoreLabel = new Label("Black points:" + p2Str);
        p2ScoreLabel.setFont(Font.font("Verdana", 10));
        TextField p2ScoreField = new TextField(Integer.toString(player2Score));
        p2ScoreField.setPrefWidth(30);
        CheckBox p2ScoreBox = new CheckBox();
        if (interactive)
            infoBox.getChildren().addAll(scoreLimitLabel, scoreLimitField, scoreLimitBox,
                    p1ScoreLabel, p1ScoreField, p1ScoreBox, p2ScoreLabel, p2ScoreField, p2ScoreBox);
        else {
            infoBox.getChildren().addAll(scoreLimitLabel, p1ScoreLabel, p2ScoreLabel);
        }
        // FFT listeners
        scoreLimitBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            mandatoryScoreLimit = newValue;
            cont.getInteractiveState().setScoreLimit(
                    Integer.parseInt(scoreLimitField.getText()), mandatoryScoreLimit);
        });
        p1ScoreBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            mandatoryScoreP1 = newValue;
            cont.getInteractiveState().setScore(
                    PLAYER1, Integer.parseInt(p1ScoreField.getText()), mandatoryScoreP1);
        });
        p2ScoreBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            mandatoryScoreP2 = newValue;
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
    }

    public void addArrow(Move m, Color color) {
        double startX, startY, endX, endY;
        Bounds b;
        if (m.oldCol == -1) {
            if (m.team == PLAYER1)
                b = playerRed.localToParent(playerRed.getBoundsInLocal());
            else
                b = playerBlack.localToParent(playerBlack.getBoundsInLocal());
        } else {
            BoardTile t = board.getTiles()[m.oldRow][m.oldCol];
            b = board.localToParent(t.localToParent(t.getBoundsInLocal()));
        }
        startX = (b.getMaxX() + b.getMinX()) / 2.0;
        startY = (b.getMaxY() + b.getMinY()) / 2.0;

        if (m.newCol == -1) {
            if (m.team == PLAYER1) b = goalRed.localToParent(goalRed.getBoundsInLocal());
            else b = goalBlack.localToParent(goalBlack.getBoundsInLocal());
        } else {
            BoardTile t = board.getTiles()[m.newRow][m.newCol];
            b = board.localToParent(t.localToParent(t.getBoundsInLocal()));
        }
        endX = (b.getMaxX() + b.getMinX()) / 2.0;
        endY = (b.getMaxY() + b.getMinY()) / 2.0;
        Arrow a = new Arrow(startX, startY, endX, endY, color);
        arrows.add(a);
        getChildren().add(a);
    }

    public void removeArrows() {
        getChildren().removeAll(arrows);
        arrows.clear();
    }
}
