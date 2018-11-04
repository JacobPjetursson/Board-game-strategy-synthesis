package gui;

import ai.Minimax.Node;
import game.Controller;
import game.Logic;
import game.Move;
import game.StateAndMove;
import gui.board.Board;
import gui.board.Goal;
import gui.board.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import misc.Database;

import java.sql.SQLException;
import java.util.ArrayList;

import static misc.Config.BLACK;
import static misc.Config.RED;

public class ReviewPane extends VBox {
    private ListView<HBox> lw;
    private boolean connected;

    public ReviewPane(Stage primaryStage, Controller currCont) {
        try {
            if (Database.dbConnection == null || Database.dbConnection.isClosed()) {
                Database.connectAndVerify();
                connected = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HBox bottomBox = new HBox(10);
        VBox.setMargin(bottomBox, new Insets(10));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        Button goToState = new Button("Go to State");
        goToState.setDisable(true);
        bottomBox.getChildren().add(goToState);

        Button cancel = new Button("Cancel");
        bottomBox.getChildren().add(cancel);
        cancel.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            if (connected) {
                try {
                    Database.dbConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (Logic.gameOver(currCont.getState())) {
                Stage newStage = new Stage();
                newStage.setScene(new Scene(new EndGamePane(primaryStage, Logic.getWinner(currCont.getState()),
                        currCont), 400, 150));
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(currCont.getWindow());
                newStage.setOnCloseRequest(Event::consume);
                newStage.show();
            }
        });

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        ObservableList<HBox> prevStateBoxes = FXCollections.observableArrayList();
        for (StateAndMove ps : currCont.getPreviousStates()) {
            HBox h = new HBox(35);
            VBox vBox = new VBox(18);
            vBox.setAlignment(Pos.CENTER);
            vBox.setFillWidth(true);
            Node n = new Node(ps.getState());
            ArrayList<Move> bestPlays = Database.bestPlays(n);
            PlayBox playBox = getPlayBox(currCont, ps, bestPlays);
            Label turnL = new Label("Turns Played: " + (ps.getTurnNo()));
            turnL.setFont(Font.font("Verdana", 14));
            turnL.setAlignment(Pos.TOP_CENTER);
            vBox.getChildren().add(turnL);

            String moveStr = String.format("Move from state (row, col):\n" +
                            "         (%d, %d) -> (%d, %d)",
                    ps.getMove().oldRow + 1, ps.getMove().oldCol + 1, ps.getMove().newRow + 1, ps.getMove().newCol + 1);
            Label moveL = new Label(moveStr);
            vBox.getChildren().add(moveL);

            Label performance;
            if (bestPlays.contains(ps.getMove())) {
                h.setStyle("-fx-background-color: rgba(0, 255, 0, 0.5);");
                performance = new Label("Perfect move");
            } else {
                performance = new Label("Imperfect move");
                h.setStyle("-fx-background-color: rgba(255,0,0, 0.5);");
            }
            performance.setAlignment(Pos.CENTER);
            vBox.getChildren().add(performance);

            Node nextNode = n.getNextNode(ps.getMove());
            String scoreStr;
            if (Logic.gameOver(nextNode.getState())) {
                scoreStr = "0";
            } else {
                scoreStr = Database.turnsToTerminal(currCont.getState().getTurn(), nextNode);
            }
            int score;
            if (scoreStr.equals("∞")) score = 0;
            else score = Integer.parseInt(scoreStr);
            Label turnsToTerminal = new Label("Turns to " + ((score >= 0) ?
                    "win " : "loss ") + "\nafter move: " + scoreStr);
            turnsToTerminal.setAlignment(Pos.CENTER);
            vBox.getChildren().add(turnsToTerminal);

            h.getChildren().addAll(playBox, vBox);
            prevStateBoxes.add(h);
        }
        lw.setItems(prevStateBoxes);
        lw.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                goToState.setDisable(false));

        goToState.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();

            int index = lw.getSelectionModel().getSelectedIndex();
            StateAndMove selected = currCont.getPreviousStates().get(index);
            Controller selectedCont = new Controller(primaryStage, currCont.getPlayerInstance(RED),
                    currCont.getPlayerInstance(BLACK), selected.getState(),
                    currCont.getTime(RED), currCont.getTime(BLACK), false);
            selectedCont.setTurnNo(selected.getTurnNo());
            selectedCont.getPlayArea().update(selectedCont);

            ArrayList<StateAndMove> stateAndMoves = new ArrayList<>();
            for (StateAndMove ps : currCont.getPreviousStates()) {
                if (ps.getTurnNo() < selectedCont.getTurnNo()) {
                    stateAndMoves.add(ps);
                }
            }
            selectedCont.setPreviousStates(stateAndMoves);
        });
        setVgrow(lw, Priority.ALWAYS);

        getChildren().addAll(lw, bottomBox);
    }

    private PlayBox getPlayBox(Controller cont, StateAndMove ps, ArrayList<Move> bestPlays) {
        Board b = new Board(20, 7, false);
        Player playerBlack = new Player(BLACK, cont, 20, 7, false);
        Goal goalRed = new Goal(3 * b.getTileSize(), 17);
        Goal goalBlack = new Goal(3 * b.getTileSize(), 17);
        Player playerRed = new Player(RED, cont, 20, 7, false);

        PlayBox pb = new PlayBox(playerBlack, goalRed, b, goalBlack, playerRed);
        pb.update(cont, ps.getState());
        pb.addArrow(ps.getMove(), Color.BLUE);
        for (Move m : bestPlays) {
            if (m.equals(ps.getMove())) continue;
            pb.addArrow(m, Color.GREEN);
        }
        return pb;
    }
}
