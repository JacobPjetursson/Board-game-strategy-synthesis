package kulibrat.gui.board;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import kulibrat.game.Controller;
import kulibrat.game.Move;

import static kulibrat.game.Logic.POS_NONBOARD;
import static misc.Globals.CLICK_INTERACTIVE;

public class Goal extends StackPane {
    private boolean highlight;
    private boolean bestMove;
    private boolean help;
    private Label turnsToTerminalLabel;
    private int clickMode;
    private Controller cont;

    public Goal(int prefWidth, int prefHeight, int clickMode, Controller cont) {
        this.clickMode = clickMode;
        this.cont = cont;
        setAlignment(Pos.CENTER);
        setPrefSize(prefWidth, prefHeight);
        setMaxWidth(prefWidth);
        setStyle("-fx-background-color: rgb(200, 200, 200);");
        turnsToTerminalLabel = new Label("");
        turnsToTerminalLabel.setFont(Font.font("Verdana", 15));
        turnsToTerminalLabel.setTextFill(Color.BLACK);

        setOnMouseEntered(me -> {
            if (highlight && bestMove) {
                setStyle("-fx-background-color: rgb(0, 225, 0);");
            } else if (highlight) {
                if (help) setStyle("-fx-background-color: rgb(255,0,0);");
                else setStyle("-fx-background-color: rgb(255,200,0);");
            }
        });

        setOnMouseExited(me -> {
            if (highlight && bestMove) {
                setStyle("-fx-background-color: rgb(0, 150, 0);");
            } else if (highlight) {
                if (help) setStyle("-fx-background-color: rgb(150,0,0);");
                else setStyle("-fx-background-color: rgb(200,150,0);");
            }
        });

        setOnMouseClicked(event -> {
            if (highlight) {
                if (clickMode == CLICK_INTERACTIVE) {
                    cont.getInteractiveState().setArrowEndpoint(POS_NONBOARD, POS_NONBOARD);
                } else {
                    BoardPiece piece = cont.getSelected();
                    cont.doHumanTurn(new Move(piece.getRow(), piece.getCol(), POS_NONBOARD, POS_NONBOARD, piece.getTeam()));
                }
            }
        });

    }


    public void setHighlight(boolean highlight, boolean help, boolean bestMove, String turns) {
        this.highlight = highlight;
        this.bestMove = bestMove;
        this.help = help;
        if (highlight && bestMove) {
            setStyle("-fx-background-color: rgb(0, 150, 0);");
        } else if (highlight) {
            if (help) setStyle("-fx-background-color: rgb(150, 0, 0);");
            else setStyle("-fx-background-color: rgb(200, 150, 0);");
        } else {
            setStyle("-fx-background-color: rgb(200, 200, 200);");
        }
        if (turns.isEmpty()) {
            getChildren().remove(turnsToTerminalLabel);
        } else {
            turnsToTerminalLabel.setText(turns);
            getChildren().add(turnsToTerminalLabel);
        }
    }
}