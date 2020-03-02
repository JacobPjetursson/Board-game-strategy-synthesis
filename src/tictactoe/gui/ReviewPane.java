package tictactoe.gui;


import javafx.application.Platform;
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
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import misc.Config;
import tictactoe.ai.StateMapping;
import tictactoe.game.*;
import tictactoe.gui.board.PlayBox.PlayBox;
import tictactoe.misc.Database;

import java.util.ArrayList;

import static misc.Globals.*;
import static tictactoe.gui.board.BoardTile.blueStr;
import static tictactoe.gui.board.BoardTile.greenStr;

public class ReviewPane extends VBox {
    private ListView<HBox> lw;

    public ReviewPane(Stage primaryStage, Controller cont) {
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

            if (Logic.gameOver(cont.getState())) {
                Stage newStage = new Stage();
                newStage.setScene(new Scene(new EndGamePane(primaryStage, Logic.getWinner(cont.getState()),
                        cont), 500, 300));
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initOwner(cont.getWindow());
                newStage.setOnCloseRequest(Event::consume);
                newStage.show();
            }
        });

        lw = new ListView<>();
        lw.setPickOnBounds(false);
        ObservableList<HBox> prevStateBoxes = FXCollections.observableArrayList();
        for (StateAndMove ps : cont.getPreviousStates()) {
            State n = new State(ps.getState());
            Move m = ps.getMove();
            State next = n.getNextState(m);
            ArrayList<Move> nonLosingMoves = Database.nonLosingMoves(n);
            StateMapping sm = Database.queryState(next);
            int winner = (sm == null) ? Logic.getWinner(next) : sm.getWinner();
            String winnerStr = (winner == n.getTurn()) ? "win" : (winner == PLAYER_NONE) ? "draw" : "loss";

            PlayBox playBox = getPlayBox(cont, ps, nonLosingMoves);
            HBox h = new HBox(35);
            VBox vBox = new VBox(18);
            vBox.setAlignment(Pos.CENTER);
            vBox.setFillWidth(true);
            Label turnL = new Label("Turns Played: " + (ps.getTurnNo()));
            turnL.setFont(Font.font("Verdana", 14));
            turnL.setAlignment(Pos.TOP_CENTER);
            vBox.getChildren().add(turnL);

            String moveStr = String.format("Add piece at (row, col):\n" +
                            "         (%d, %d)", ps.getMove().row + 1, ps.getMove().col + 1);
            if (Config.ENABLE_GGP_PARSER)
                moveStr = String.format("Set mark at (x,y):\n" +
                            "        (%d, %d)", ps.getMove().col + 1, ps.getMove().row + 1);
            Label moveL = new Label(moveStr);
            vBox.getChildren().add(moveL);

            Label performance;
            if (nonLosingMoves.contains(ps.getMove())) {
                h.setStyle("-fx-background-color: rgba(0, 255, 0, 0.5);");
                performance = new Label("Perfect move");
            } else {
                performance = new Label("Imperfect move");
                h.setStyle("-fx-background-color: rgba(255,0,0, 0.5);");
            }
            performance.setAlignment(Pos.CENTER);
            vBox.getChildren().add(performance);

            String turnsToTerminalStr = Database.turnsToTerminal(cont.getState().getTurn(), next);
            if (turnsToTerminalStr.startsWith("-"))
                turnsToTerminalStr = turnsToTerminalStr.substring(1);
            Label turnsToTerminal = new Label("Turns to " + winnerStr +
                    "\nafter move: " + turnsToTerminalStr);
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
            StateAndMove selected = cont.getPreviousStates().get(index);
            Controller selectedCont = new Controller(primaryStage, cont.getPlayerInstance(PLAYER1),
                    cont.getPlayerInstance(PLAYER2), selected.getState());

            selectedCont.setTurnNo(selected.getTurnNo());
            selectedCont.getPlayArea().update(selectedCont);

            ArrayList<StateAndMove> stateAndMoves = new ArrayList<>();
            for (StateAndMove ps : cont.getPreviousStates()) {
                if (ps.getTurnNo() < selectedCont.getTurnNo()) {
                    stateAndMoves.add(ps);
                }
            }
            selectedCont.setPreviousStates(stateAndMoves);
        });
        setVgrow(lw, Priority.ALWAYS);

        getChildren().addAll(lw, bottomBox);
    }

    private PlayBox getPlayBox(Controller cont, StateAndMove ps, ArrayList<Move> bestMoves) {

        PlayBox pb = new PlayBox(20, CLICK_DISABLED, cont);
        pb.update(ps.getState());

        Platform.runLater(() -> {
            pb.addHighlight(ps.getMove().row, ps.getMove().col, ps.getMove().team, blueStr);
            for (Move m : bestMoves) {
                if (m.equals(ps.getMove()))
                    continue;
                pb.addHighlight(m.row, m.col, m.team, greenStr);
            }
        });

        return pb;
    }
}
