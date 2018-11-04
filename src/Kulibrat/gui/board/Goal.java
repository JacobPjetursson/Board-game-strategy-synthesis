package gui.board;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Goal extends StackPane {
    private boolean highlight;
    private boolean bestMove;
    private boolean help;
    private Label turnsToTerminalLabel;

    public Goal(int prefWidth, int prefHeight) {
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