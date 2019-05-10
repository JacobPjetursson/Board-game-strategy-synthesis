package kulibrat.gui;


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
import kulibrat.game.*;
import kulibrat.gui.board.PlayBox.PlayBox;
import kulibrat.misc.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import static misc.Config.*;

public class ReviewPane extends VBox {
    private ListView<HBox> lw;
    private boolean connected;

    public ReviewPane(Stage primaryStage, Controller currCont) {
        try {
            if (Database.dbConnection == null || Database.dbConnection.isClosed()) {
                Database.connectwithVerification();
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
                        currCont), 500, 300));
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
            State n = new State(ps.getState());
            HashSet<Move> bestMoves = Database.bestMoves(n);
            PlayBox playBox = getPlayBox(currCont, ps, bestMoves);
            Label turnL = new Label("Turns Played: " + (ps.getTurnNo()));
            turnL.setFont(Font.font("Verdana", 20));
            turnL.setAlignment(Pos.TOP_CENTER);
            vBox.getChildren().add(turnL);

            String moveStr = String.format("Move from state (row, col):\n" +
                            "         (%d, %d) -> (%d, %d)",
                    ps.getMove().oldRow + 1, ps.getMove().oldCol + 1, ps.getMove().newRow + 1, ps.getMove().newCol + 1);
            Label moveL = new Label(moveStr);
            moveL.setFont(Font.font("Verdana", 18));
            vBox.getChildren().add(moveL);

            Label performance;
            if (bestMoves.contains(ps.getMove())) {
                h.setStyle("-fx-background-color: rgba(0, 255, 0, 0.5);");
                performance = new Label("Perfect move");
            } else {
                performance = new Label("Imperfect move");
                h.setStyle("-fx-background-color: rgba(255,0,0, 0.5);");
            }
            performance.setAlignment(Pos.CENTER);
            performance.setFont(Font.font("Verdana", 18));
            vBox.getChildren().add(performance);

            State nextState = n.getNextState(ps.getMove());
            String scoreStr;
            if (Logic.gameOver(nextState)) {
                scoreStr = "0";
            } else {
                scoreStr = Database.turnsToTerminal(currCont.getState().getTurn(), nextState);
            }
            int score;
            if (scoreStr.equals("∞")) score = 0;
            else score = Integer.parseInt(scoreStr);
            Label turnsToTerminal = new Label("Turns to " + ((score >= 0) ?
                    "win " : "loss ") + "\nafter move: " + scoreStr);
            turnsToTerminal.setAlignment(Pos.CENTER);
            turnsToTerminal.setFont(Font.font("Verdana", 18));
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
            Controller selectedCont = new Controller(primaryStage, currCont.getPlayerInstance(PLAYER1),
                    currCont.getPlayerInstance(PLAYER2), selected.getState(),
                    currCont.getTime(PLAYER1), currCont.getTime(PLAYER2), false);
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

    private PlayBox getPlayBox(Controller cont, StateAndMove ps, HashSet<Move> bestMoves) {

        PlayBox pb = new PlayBox(50, CLICK_DISABLED, cont);
        pb.update(ps.getState());

        pb.addArrow(ps.getMove(), Color.BLUE);
        for (Move m : bestMoves) {
            if (m.equals(ps.getMove())) continue;
            pb.addArrow(m, Color.GREEN);
        }


        return pb;
    }
}
