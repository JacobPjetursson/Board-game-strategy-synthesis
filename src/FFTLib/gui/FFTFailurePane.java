package FFT;

import ai.Minimax.Node;
import game.Controller;
import game.Move;
import game.StateAndMove;
import gui.PlayBox;
import gui.board.Board;
import gui.board.Goal;
import gui.board.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import misc.Database;

import java.util.ArrayList;

import static misc.Config.BLACK;
import static misc.Config.RED;

public class FFTFailurePane extends BorderPane {
    private FFTManager fftManager;
    private ListView<VBox> lw;

    public FFTFailurePane(Scene prevScene, FFTManager fftManager, Controller cont) {
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.fftManager = fftManager;
        Label title = new Label("This is the first encountered state where the FFT failed");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinHeight(65);
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        StateAndMove ps = fftManager.currFFT.failingPoint;
        Node n = new Node(ps.getState());
        ArrayList<Move> nonLosingPlays = Database.nonLosingPlays(n);
        PlayBox playBox = getPlayBox(cont, ps, nonLosingPlays);
        setCenter(playBox);

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        lw.setPrefWidth(350);
        lw.setSelectionModel(new NoSelectionModel<VBox>());
        showRuleGroups();
        BorderPane.setMargin(lw, new Insets(15));
        setRight(lw);


        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label arrowInfoLabel = new Label("Green arrows are non-losing moves, the blue arrow is the chosen move");
        arrowInfoLabel.setFont(Font.font("Verdana", 15));

        String moveInfo;
        if (fftManager.currFFT.failingPoint.random)
            moveInfo = "The FFT did not apply to the above state, and a random, losing move existed";
        else
            moveInfo = "The FFT applied to the above state, but the move it chose was a losing move";
        Label moveInfoLabel = new Label(moveInfo);
        moveInfoLabel.setFont(Font.font("Verdana", 15));

        Button back = new Button("Back");
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(prevScene);
        });
        BorderPane.setAlignment(back, Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(arrowInfoLabel, moveInfoLabel, back);
        setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
    }


    private PlayBox getPlayBox(Controller cont, StateAndMove ps, ArrayList<Move> nonLosingPlays) {
        int tileW = 60;
        int pieceRad = 20;
        int goalH = 50;
        Board b = new Board(tileW, pieceRad, false);
        Player playerBlack = new Player(BLACK, cont, tileW, pieceRad, false);
        Goal goalRed = new Goal(3 * b.getTileSize(), goalH);
        Goal goalBlack = new Goal(3 * b.getTileSize(), goalH);
        Player playerRed = new Player(RED, cont, tileW, pieceRad, false);

        PlayBox pb = new PlayBox(playerBlack, goalRed, b, goalBlack, playerRed);
        pb.update(cont, ps.getState());
        pb.addArrow(ps.getMove(), Color.BLUE);
        for (Move m : nonLosingPlays) {
            if (m.equals(ps.getMove())) continue;
            pb.addArrow(m, Color.GREEN);
        }
        return pb;
    }
    private void showRuleGroups() {
        ObservableList<VBox> ruleGroups = FXCollections.observableArrayList();
        for (int i = 0; i < fftManager.currFFT.ruleGroups.size(); i++) {
            // Rule group
            RuleGroup rg = fftManager.currFFT.ruleGroups.get(i);
            VBox rgVBox = new VBox(10);
            rgVBox.setAlignment(Pos.CENTER);
            Label rgLabel = new Label((i + 1) + ": " + rg.name);
            rgLabel.setFont(Font.font("Verdana", 16));
            rgVBox.getChildren().add(rgLabel);
            for (int j = 0; j < rg.rules.size(); j++) {
                Rule r = rg.rules.get(j);
                Label rLabel = new Label((j + 1) + ": " + r.printRule());
                rLabel.setFont(Font.font("Verdana", 10));
                // TODO - below is hacky
                int tempTeam = fftManager.currFFT.failingPoint.getMove().team;
                fftManager.currFFT.failingPoint.getMove().team = -1;
                if (r.action.getMove().equals(fftManager.currFFT.failingPoint.getMove()))
                    rLabel.setTextFill(Color.BLUE);
                fftManager.currFFT.failingPoint.getMove().team = tempTeam;
                rgVBox.getChildren().add(rLabel);
            }
            ruleGroups.add(rgVBox);
        }
        lw.setItems(ruleGroups);
    }
}
