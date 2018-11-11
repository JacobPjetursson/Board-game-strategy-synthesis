package tictactoe.game;

import fftlib.FFTManager;
import fftlib.FFT_Follower;
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
    private AI aiRed;
    private AI aiBlack;
    private Button startAIButton;
    private Button stopAIButton;
    private Button editFFTButton;
    private Button showFFTButton;
    private CheckBox interactiveFFTBox;
    private Thread aiThread;
    private NavPane navPane;
    private tictactoe.game.State state;
    private PlayArea playArea;
    private boolean endGamePopup;
    private ArrayList<StateAndMove> previousStates;
    private Window window;
    private FFTManager fftManager;
    private boolean fftInteractiveMode;
    private boolean fftAllowInteraction;
    private ShowFFTPane shownFFT;

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

        this.fftManager = new FFTManager(new State(state), new Logic(), new Database());

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
        interactiveFFTBox = navPane.getInteractiveFFTBox();

        showNavButtons();

        // Set event handlers for gui elements
        // Tiles
        BoardTile[][] tiles = playArea.getBoard().getTiles();
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                BoardTile tile = tiles[i][j];
                tile.setOnMouseClicked(event -> {
                    if (tile.isFree()) {
                        tile.getChildren().clear();
                        doHumanTurn(tile.getRow(), tile.getCol());
                        tile.setFree(false);
                    }
                });
            }
        }
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

        // FFTManager LISTENERS
        // edit fftManager button
        editFFTButton.setOnAction(event -> {
            Scene scene = primaryStage.getScene();
            primaryStage.setScene(new Scene(new EditFFTScene(primaryStage, scene, fftManager, new FailStatePane(this)), Config.WIDTH, Config.HEIGHT));
        });

        // interactive mode
        fftInteractiveMode = true;
        interactiveFFTBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            fftInteractiveMode = newValue;
        });

        // Show FFT button
        showFFTButton.setOnAction(event -> {
            Stage newStage = new Stage();
            shownFFT = new ShowFFTPane(fftManager, state);
            newStage.setScene(new Scene(shownFFT, 450, 550));
            newStage.show();
            newStage.setOnCloseRequest(otherevent -> shownFFT = null);

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
        if ((mode != AI_VS_AI || player2Instance == FFT || player1Instance == FFT)) {
            if (!navPane.containsShowFFTButton())
                navPane.addShowFFTButton();
        }
        if ((player2Instance == FFT || player1Instance == FFT) && !navPane.containsFFTWidgets())
            navPane.addFFTWidgets();
    }

    private void instantiateAI(int team) {
        if (team == PLAYER1) {
            if (player1Instance == LOOKUP_TABLE) {
                aiRed = new LookupTableMinimax(PLAYER1, state);
            } else if (player1Instance == FFT) {
                aiRed = new FFTFollower(PLAYER1, fftManager);
            }
        } else {
            if (player2Instance == LOOKUP_TABLE) {
                aiBlack = new LookupTableMinimax(PLAYER2, state);
            } else if (player2Instance == FFT) {
                aiBlack = new FFTFollower(PLAYER2, fftManager);
            }
        }
    }

    // Is called when a tile is pressed by the user. If vs. the AI, it calls the doAITurn after. This function also highlights
    // the best pieces for the opponent, if it is human vs human.
    private void doHumanTurn(int row, int col) {
        Move move = new Move(row, col, state.getTurn());
        previousStates.add(new StateAndMove(state, move, turnNo));
        state = state.getNextState(move);
        updatePostHumanTurn();
        if (Logic.gameOver(state)) return;
        if (state.getTurn() == move.team) {
            String skipped = (state.getTurn() == PLAYER1) ? "Black" : "Red";
            System.out.println("TEAM " + skipped + "'s turn has been skipped!");
            playArea.getInfoPane().displaySkippedTurn(skipped);
            return;
        }
        if ((player1Instance == FFT && state.getTurn() == PLAYER2) ||
                (player2Instance == FFT && state.getTurn() == PLAYER1)) {
            startAIButton.fire();
        } else if ((player2Instance != HUMAN && state.getTurn() == PLAYER2) ||
                (player1Instance != HUMAN && state.getTurn() == PLAYER1)) {
            doAITurn();
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
        // For the AI vs. AI mode. New thread is needed to update the gui while running the AI
        navPane.getRestartButton().setDisable(true);
        navPane.getMenuButton().setDisable(true);
        startAIButton.setDisable(true);

        aiThread = new Thread(() -> {
            try {
                while (!Logic.gameOver(state)) {
                    doAITurn();
                    if (fftAllowInteraction) {
                        stopAIButton.fire();
                    }
                    if (player1Instance == LOOKUP_TABLE && player2Instance == LOOKUP_TABLE) {
                        Thread.sleep(player1Time);
                    } else if (state.getTurn() == PLAYER1 && player1Instance == FFT && !fftAllowInteraction)
                        Thread.sleep(player1Time);
                    else if (state.getTurn() == PLAYER2 && player2Instance == FFT && !fftAllowInteraction)
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
        fftAllowInteraction = false;
        boolean makeDefaultMove = false;
        int turn = state.getTurn();
        Move move;
        if (aiRed != null && turn == PLAYER1) {
            move = aiRed.makeMove(state);
            if (player1Instance == FFT && move == null)
                makeDefaultMove = true;
        } else {
            move = aiBlack.makeMove(state);
            if (player2Instance == FFT && move == null)
                makeDefaultMove = true;
        }
        if (makeDefaultMove) {
            if (fftInteractiveMode) {
                System.out.println("Defaulting to user interaction");
                fftAllowInteraction = true;
                return;
            } else {
                System.out.println("Defaulting to random move");
                move = getDefaultFFTMove();
            }
        }
        state = state.getNextState(move);
        updatePostAITurn();
        if (Logic.gameOver(state)) return;
        if (mode == HUMAN_VS_AI) {
            if (turn == state.getTurn()) {
                String skipped = (turn == PLAYER1) ? "Black" : "Red";
                System.out.println("TEAM " + skipped + "'s turn has been skipped!");
                playArea.getInfoPane().displaySkippedTurn(skipped);
                doAITurn();
            }
        }
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

    public void setPlayerInstance(int team, int playerInstance) {
        if (team == PLAYER1) {
            player1Instance = playerInstance;
        } else {
            player2Instance = playerInstance;
        }
        int oldMode = mode;
        this.mode = setMode(player1Instance, player2Instance);
        instantiateAI(team);
        if (state.getTurn() == team && playerInstance != HUMAN && mode != AI_VS_AI) {
            doAITurn();
        } else if (state.getTurn() != team && oldMode == AI_VS_AI && mode != AI_VS_AI) {
            doAITurn();
        }
        showNavButtons();
        playArea.update(this);
    }

    // Sets the mode based on the red and black player types
    private int setMode(int playerRedInstance, int playerBlackInstance) {
        if (playerRedInstance == HUMAN && playerBlackInstance == HUMAN) {
            return HUMAN_VS_HUMAN;
        } else if (playerRedInstance != HUMAN ^ playerBlackInstance != HUMAN) {
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

    public void setPlayerCalcTime(int team, int time) {
        if (team == PLAYER1)
            player1Time = time;
        else
            player2Time = time;
    }

    public PlayArea getPlayArea() {
        return playArea;
    }

    public ArrayList<StateAndMove> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(ArrayList<StateAndMove> stateAndMoves) {
        this.previousStates = stateAndMoves;
    }

    public Window getWindow() {
        return window;
    }

    public boolean getFFTAllowInteraction() {
        return fftAllowInteraction;
    }

}
