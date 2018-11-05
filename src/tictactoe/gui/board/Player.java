package tictactoe.gui.board;


import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import tictactoe.game.Controller;

import java.net.URL;

import static misc.Config.*;


public class Player extends VBox {
    private int team;
    private int type;
    private Label typeLabel;

    public Player(int team, Controller cont, int tileWidth) {
        this.team = team;
        type = cont.getPlayerInstance(team);
        setAlignment(Pos.CENTER);
        setSpacing(tileWidth / 6);
        setStyle("-fx-background-color: rgb(255, 255, 255);");


        URL urlRed = this.getClass().getClassLoader().getResource("playerIconRed.png");
        URL urlBlack = this.getClass().getClassLoader().getResource("playerIconBlack.png");
        Image img = (team == PLAYER1) ? new Image(urlRed.toExternalForm()) :
                new Image(urlBlack.toExternalForm());
        ImageView imgView = new ImageView(img);

        imgView.setPreserveRatio(true);
        imgView.setFitHeight(tileWidth);
        imgView.setFitWidth(tileWidth);
        BorderPane imgPane = new BorderPane();
        imgPane.setCenter(imgView);

        GridPane gridPaneDisplay = new GridPane();
        gridPaneDisplay.setAlignment(Pos.CENTER);
        gridPaneDisplay.setPrefSize((tileWidth * 4) / 3, tileWidth);
        gridPaneDisplay.setMaxWidth((tileWidth * 4) / 3);

        typeLabel = new Label();
        setTypeLabelText(type);
        typeLabel.setFont(Font.font("Verdana", tileWidth / 4));
    }


    public int getTeam() {
        return team;
    }

    public void setTypeLabelText(int type) {
        typeLabel.setText((type == HUMAN) ? "Human" : (type == MINIMAX) ? "Minimax" :
                (type == LOOKUP_TABLE) ? "Lookup\n Table" :
                        (type == MONTE_CARLO) ? "MCTS" : "FFT");
    }
}
