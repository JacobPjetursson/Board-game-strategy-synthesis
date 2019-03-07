package tictactoe.game;

import fftlib.FFTManager;
import fftlib.gui.EditFFTInteractive;
import fftlib.gui.EditFFTScene;
import fftlib.gui.ShowFFTPane;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import misc.Config;
import tictactoe.FFT.GameSpecifics;
import tictactoe.FFT.InteractiveState;
import tictactoe.ai.AI;
import tictactoe.ai.FFTFollower;
import tictactoe.ai.LookupTableMinimax;
import tictactoe.gui.*;
import tictactoe.gui.board.BoardTile;
import tictactoe.misc.Database;

import java.util.ArrayList;
import java.util.Random;

import static misc.Config.*;


public class Controller {
    public Stage primaryStage;
    private int mode;
    private int player1Instance;
    private int player2Instance;
    private int player1Time;
    private int player2Time;
    private int turnNo;
    private AI aiCross;
    private AI aiCircle;
    private Button startAIButton;
    private Button stopAIButton;
    private Button editFFTButton;
    private Button showFFTButton;
    private Button reviewButton;
    private Button addRuleFFTButton;
    private CheckBox automaticFFTBox;
    private Thread aiThread;
    private NavPane navPane;
    private tictactoe.game.State state;
    private PlayArea playArea;
    private boolean endGamePopup;
    private ArrayList<StateAndMove> previousStates;
    private Window window;
    private FFTManager fftManager;
    private boolean fftAutomaticMode;
    private boolean fftUserInteraction;
    private ShowFFTPane shownFFT;
    private GameSpecifics gameSpecifics;
    private CheckBox helpHumanBox;

    public Controller(Stage primaryStage, int player1Instance, int player2Instance,
                      State state, int player1Time, int player2Time) {
        this.mode = setMode(player1Instance, player2Instance);
        this.player1Instance = player1Instance;
        this.player2Instance = player2Instance;
        this.player1Time = player1Time;
        this.player2Time = player2Time;
        this.turnNo = 0;
        this.state = state;
        this.primaryStage = primaryStage;
        this.endGamePopup = false;
        this.previousStates = new ArrayList<>();
        // fills up the "database"
        new LookupTableMinimax(PLAYER1, state);

        gameSpecifics = new GameSpecifics(this);
        this.fftManager = new FFTManager(gameSpecifics);

        PlayPane playPane = new PlayPane(this);
        primaryStage.setScene(new Scene(playPane,
                Config.WIDTH, Config.HEIGHT));
        navPane = playPane.getNavPane();
        playArea = playPane.getPlayArea();
        window = playArea.getScene().getWindow();

        instantiateAI(PLAYER1);
        instantiateAI(PLAYER2);

        // Fetch all gui elements that invoke something game-related
        startAIButton = navPane.getStartAIButton();
        stopAIButton = navPane.getStopAIButton();
        showFFTButton = navPane.getShowFFTButton();
        editFFTButton = navPane.getEditFFTButton();
        addRuleFFTButton = navPane.getAddRuleFFTButton();
        reviewButton = navPane.getReviewButton();
        automaticFFTBox = navPane.getAutomaticFFTBox();
        helpHumanBox = navPane.getHelpHumanBox();

        showNavButtons();

        // Set event handlers for gui elements

        startAIButton.setOnAction(event -> {
            startAI();
            stopAIButton.setDisable(false);
        });
        // Stop AI button
        stopAIButton.setDisable(true);
        stopAIButton.setOnAction(event -> {
            aiThread.interrupt();
            stopAIButton.setDisable(true);
        });

        // help human checkbox
        helpHumanBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            helpHumanBox.setDisable(true);
            if (newValue) {
                helpHumanBox.setSelected(true);
                highlightHelp(true);
            } else {
                helpHumanBox.setSelected(false);
                highlightHelp(false);
            }
            helpHumanBox.setDisable(false);
        });
        // Review button
        reviewButton.setOnAction(event -> reviewGame());

        // FFTManager LISTENERS
        // edit fftManager button
        editFFTButton.setOnAction(event -> {
            Scene scene = primaryStage.getScene();
            primaryStage.setScene(new Scene(new EditFFTScene(primaryStage, scene, fftManager), Config.WIDTH, Config.HEIGHT));
        });

        // add rule to FFT button
        addRuleFFTButton.setOnAction(event -> {
            Scene scene = primaryStage.getScene();
            EditFFTInteractive interactive = new EditFFTInteractive(scene, this.fftManager);
            primaryStage.setScene(new Scene(interactive, Config.WIDTH, Config.HEIGHT));
            interactive.update(this.state);
        });

        // automatic mode
        fftAutomaticMode = false;
        automaticFFTBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            fftAutomaticMode = newValue;
        });

        // Show FFT button
        showFFTButton.setOnAction(event -> {
            Stage newStage = new Stage();
            newStage.setOnHiding(otherevent -> shownFFT = null);
            shownFFT = new ShowFFTPane(fftManager, state);
            newStage.setScene(new Scene(shownFFT, 700, 700));
            newStage.show();


        });

        if (mode == HUMAN_VS_AI && player1Instance != HUMAN && state.getTurn() == PLAYER1) {
            aiThread = new Thread(this::doAITurn);
            aiThread.setDaemon(true);
            aiThread.start();
        }
    }

    private void showNavButtons() {
        navPane.removeWidgets();
        if (mode == AI_VS_AI && !navPane.containsAIWidgets())
            navPane.addAIWidgets();
        if (mode == HUMAN_VS_AI && !navPane.containsReviewButton())
            navPane.addReviewButton();
        if ((mode != AI_VS_AI || player1Instance == FFT || player2Instance == FFT)) {
            if (!navPane.containsHelpBox())
                navPane.addHelpHumanBox();
        }
        if ((player2Instance == FFT || player1Instance == FFT) && !navPane.containsFFTWidgets())
            navPane.addFFTWidgets();

    }

    private void instantiateAI(int team) {
        if (team == PLAYER1) {
            if (player1Instance == LOOKUP_TABLE) {
                aiCross = new LookupTableMinimax(PLAYER1, state);
            } else if (player1Instance == FFT) {
                aiCross = new FFTFollower(PLAYER1, fftManager);
            }
        } else {
            if (player2Instance == LOOKUP_TABLE) {
                aiCircle = new LookupTableMinimax(PLAYER2, state);
            } else if (player2Instance == FFT) {
                aiCircle = new FFTFollower(PLAYER2, fftManager);
            }
        }
    }

    // Is called when a tile is pressed by the user. If vs. the AI, it calls the doAITurn after. This function also highlights
    // the best pieces for the opponent, if it is human vs human.
    public void doHumanTurn(int row, int col) {
        Move move = new Move(row, col, state.getTurn());
        previousStates.add(new StateAndMove(state, move, turnNo));
        state = state.getNextState(move);
        updatePostHumanTurn();
        if (Logic.gameOver(state)) return;
        if (state.getTurn() == move.team) {
            String skipped = (state.getTurn() == PLAYER1) ? "Circle" : "Cross";
            System.out.println(skipped + "'s turn has been skipped!");
            playArea.getInfoPane().displaySkippedTurn(skipped);
            if (player1Instance == FFT && state.getTurn() == PLAYER1 ||
                    player2Instance == FFT && state.getTurn() == PLAYER2)
                doAITurn();
            else if (helpHumanBox.isSelected()) {
                highlightHelp(true);
            }
            return;
        }
        // FFT vs AI
        if (mode == AI_VS_AI && ((player1Instance == FFT && state.getTurn() == PLAYER2) ||
                (player2Instance == FFT && state.getTurn() == PLAYER1))) {
            startAIButton.fire();
        // Human vs. AI
        } else if ((player2Instance != HUMAN && state.getTurn() == PLAYER2) ||
                (player1Instance != HUMAN && state.getTurn() == PLAYER1)) {
            doAITurn();
        }
        // Human/FFT vs. Human
        else if (helpHumanBox.isSelected()) {
            highlightHelp(true);
        }
    }

    private void updatePostHumanTurn() {
        turnNo++;
        playArea.update(this);
        checkGameOver();
        if (shownFFT != null) shownFFT.showRuleGroups();
    }

    private void updatePostAITurn() {
        turnNo++;
        // Update gui elements on another thread
        Platform.runLater(() -> {
            playArea.update(this);
            checkGameOver();
        });
        if (shownFFT != null) shownFFT.showRuleGroups();
    }

    // This function is called when two AI's are matched against each other. It can be interrupted by the user.
    // For the lookup table, a delay can be set
    private void startAI() {
        // For the AI vs. AI mode. New thread is needed to updateRuleFromTile the gui while running the AI
        navPane.getRestartButton().setDisable(true);
        navPane.getMenuButton().setDisable(true);
        startAIButton.setDisable(true);

        aiThread = new Thread(() -> {
            try {
                while (!Logic.gameOver(state)) {
                    doAITurn();
                    if (fftUserInteraction) {
                        stopAIButton.fire();
                    }
                    if (player1Instance == LOOKUP_TABLE && player2Instance == LOOKUP_TABLE) {
                        Thread.sleep(player1Time);
                    } else if (state.getTurn() == PLAYER1 && player1Instance == FFT && !fftUserInteraction)
                        Thread.sleep(player1Time);
                    else if (state.getTurn() == PLAYER2 && player2Instance == FFT && !fftUserInteraction)
                        Thread.sleep(player2Time);
                    else {
                        Thread.sleep(0); // To allow thread interruption
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                startAIButton.setDisable(false);
                navPane.getMenuButton().setDisable(false);
                navPane.getRestartButton().setDisable(false);
            }
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }

    // The AI makes its turn, and the GUI is updated while doing so
    private void doAITurn() {
        fftUserInteraction = false;
        boolean makeDefaultMove = false;
        int turn = state.getTurn();
        Move move;
        if (aiCross != null && turn == PLAYER1) {
            move = aiCross.makeMove(state);
            if (player1Instance == FFT && move == null)
                makeDefaultMove = true;
        } else {
            move = aiCircle.makeMove(state);
            if (player2Instance == FFT && move == null)
                makeDefaultMove = true;
        }
        if (makeDefaultMove) {
            if (fftAutomaticMode) {
                System.out.println("Defaulting to random move");
                move = getDefaultFFTMove();
            } else {
                System.out.println("Defaulting to user interaction");
                fftUserInteraction = true;
                if (helpHumanBox.isSelected())
                    highlightHelp(true);
                return;
            }
        }
        state = state.getNextState(move);
        updatePostAITurn();
        if (Logic.gameOver(state)) return;
        if (mode == HUMAN_VS_AI) {
            if (turn == state.getTurn()) {
                String skipped = (turn == PLAYER1) ? "Circle" : "Cross";
                System.out.println(skipped + "'s turn has been skipped!");
                playArea.getInfoPane().displaySkippedTurn(skipped);
                doAITurn();
            } else if (helpHumanBox.isSelected()) {
                highlightHelp(true);
            }
        } else if (((state.getTurn() == PLAYER1 && player1Instance == FFT) ||
                (state.getTurn() == PLAYER2 && player2Instance == FFT)) &&
                helpHumanBox.isSelected()) {
            highlightHelp(true);
        }
    }

    private void highlightHelp(boolean highlight) {
        State n = new State(state);
        ArrayList<Move> bestPlays = null;
        if (highlight)
            bestPlays = Database.bestPlays(n);
        BoardTile[][] tiles = playArea.getPlayBox().getBoard().getTiles();

        for (BoardTile[] tile : tiles) {
            for (BoardTile aTile : tile) {
                aTile.setBest(false);
                aTile.setTurnsToTerminal("");
                if (!highlight)
                    continue;
                for (Move m : bestPlays) {
                    if (m.col == aTile.getCol() && m.row == aTile.getRow())
                        aTile.setBest(true);
                }
            }
        }

        // Turns to terminal
        ArrayList<Move> moves = Logic.legalMoves(state.getTurn(), state);
        ArrayList<String> turnsToTerminalList = getScores(moves);

        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            String turns = "";
            if (highlight) {
                turns = turnsToTerminalList.get(i);
            }
            tiles[m.row][m.col].setTurnsToTerminal(turns);
        }
    }

    private ArrayList<String> getScores(ArrayList<Move> moves) {
        ArrayList<String> turnsToTerminalList = new ArrayList<>();
        for (Move m : moves) {
            State s = new State(state).getNextState(m);
            if (Logic.gameOver(s)) {
                turnsToTerminalList.add("0");
            } else turnsToTerminalList.add(Database.turnsToTerminal(state.getTurn(), s));
        }
        return turnsToTerminalList;
    }

    private Move getDefaultFFTMove() {
        Random r = new Random();
        int moveSize = state.getLegalMoves().size();
        int index = r.nextInt(moveSize);
        return state.getLegalMoves().get(index);
    }

    // Checks if the game is over and shows a popup. Popup allows a restart, go to menu, or review game
    private void checkGameOver() {
        if (Logic.gameOver(state) && !endGamePopup) {
            endGamePopup = true;
            Stage newStage = new Stage();
            int winner = Logic.getWinner(state);
            if (player1Instance == HUMAN) state.setTurn(PLAYER1);
            else if (player2Instance == HUMAN) state.setTurn(PLAYER2);

            newStage.setScene(new Scene(new EndGamePane(primaryStage, winner,
                    this), 400, 150));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(window);
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        }
    }

    public State getState() {
        return state;
    }

    public int getPlayerInstance(int team) {
        if (team == PLAYER1) {
            return player1Instance;
        } else {
            return player2Instance;
        }
    }

    // Opens the review pane
    private void reviewGame() {
        Stage newStage = new Stage();
        newStage.setScene(new Scene(new ReviewPane(primaryStage, this), 325, Config.HEIGHT - 50));
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);
        newStage.setOnCloseRequest(Event::consume);
        newStage.show();
    }

    // Sets the mode based on player types
    private int setMode(int player1Instance, int player2Instance) {
        if (player1Instance == HUMAN && player2Instance == HUMAN) {
            return HUMAN_VS_HUMAN;
        } else if (player1Instance != HUMAN ^ player2Instance != HUMAN) {
            return HUMAN_VS_AI;
        } else {
            return AI_VS_AI;
        }
    }

    public int getTurnNo() {
        return turnNo;
    }

    public void setTurnNo(int turnNo) {
        this.turnNo = turnNo;
    }

    public int getMode() {
        return mode;
    }

    public int getTime(int team) {
        if (team == PLAYER1) return player1Time;
        else return player2Time;
    }
    public Window getWindow() {
        return window;
    }

    public boolean getFFTAllowInteraction() {
        return fftUserInteraction;
    }

    public ArrayList<StateAndMove> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(ArrayList<StateAndMove> stateAndMoves) {
        this.previousStates = stateAndMoves;
    }

    public InteractiveState getInteractiveState() {
        return gameSpecifics.interactiveState;
    }

    public PlayArea getPlayArea() {
        return playArea;
    }

}