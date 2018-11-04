package gui.board;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static misc.Config.BLACK;

public class BoardTile extends StackPane {
    private int row;
    private int col;
    private boolean highlight;
    private boolean bestMove;
    private boolean help;
    private Label turnsToTerminalLabel;

    BoardTile(int row, int col, int tilesize) {
        this.row = row;
        this.col = col;
        setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setAlignment(Pos.CENTER);
        setPrefSize(tilesize, tilesize);
        setStyle("-fx-background-color: rgb(255, 255, 255);");
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
    }

    public boolean getHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight, boolean help, boolean bestMove, String turns) {
        this.highlight = highlight;
        this.bestMove = bestMove;
        this.help = help;
        if (highlight && bestMove) {
            setStyle("-fx-background-color: rgb(0, 150, 0);");
        } else if (highlight) {
            if (help) setStyle("-fx-background-color: rgb(150,0,0);");
            else setStyle("-fx-background-color: rgb(200,150,0);");
        } else {
            setStyle("-fx-background-color: rgb(255, 255, 255);");
        }

        if (turns.isEmpty()) {
            getChildren().remove(turnsToTerminalLabel);
        } else {
            turnsToTerminalLabel.setText(turns);
            turnsToTerminalLabel.setTextFill(Color.BLACK);
            getChildren().add(turnsToTerminalLabel);
            if (getChildren().size() > 1) {
                BoardPiece bp = (BoardPiece) getChildren().get(0);
                if (bp.getTeam() == BLACK) {
                    turnsToTerminalLabel.setTextFill(Color.WHITE);
                }
            }
        }
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public BoardPiece getPiece() {
        if (!getChildren().isEmpty())
            return (BoardPiece) getChildren().get(0);
        return null;
    }
}

