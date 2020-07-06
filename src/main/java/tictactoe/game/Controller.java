package tictactoe.game;

import fftlib.game.FFTSolution;
import fftlib.game.NodeMapping;
import fftlib.logic.FFT;
import fftlib.FFTManager;
import fftlib.game.FFTSolver;
import fftlib.gui.FFTInteractivePane;
import fftlib.gui.FFTOverviewPane;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import misc.Globals;
import tictactoe.FFT.GameSpecifics;
import tictactoe.FFT.InteractiveNode;
import tictactoe.ai.AI;
import tictactoe.ai.FFTFollower;
import tictactoe.ai.PerfectPlayer;
import tictactoe.gui.*;
import tictactoe.gui.board.BoardTile;

import java.util.ArrayList;
import java.util.Random;

import static misc.Globals.*;


public class Controller {
    public Stage primaryStage;
    private int mode;
    private int player1Instance;
    private int player2Instance;
    private int turnNo;
    private AI aiCross;
    private AI aiCircle;
    private Button startAIButton;
    private Button stopAIButton;
    private Button editFFTButton;
    private Button reviewButton;
    private Button addRuleFFTButton;
    private CheckBox automaticFFTBox;
    private Thread aiThread;
    private NavPane navPane;
    private Node node;
    private PlayArea playArea;
    private boolean endGamePopup;
    private ArrayList<NodeAndMove> previousStates;
    private Window window;
    private boolean fftAutomaticMode;
    private boolean fftUserInteraction;
    private GameSpecifics gameSpecifics;
    private CheckBox helpHumanBox;


    public Controller(Stage primaryStage, int player1Instance, int player2Instance,
                      Node node) {
        this.mode = setMode(player1Instance, player2Instance);
        this.player1Instance = player1Instance;
        this.player2Instance = player2Instance;
        this.turnNo = 0;
        this.node = node;
        this.primaryStage = primaryStage;
        this.endGamePopup = false;
        this.previousStates = new ArrayList<>();

        gameSpecifics = new GameSpecifics(this);
        FFTManager.initialize(gameSpecifics);
        FFTSolver.solveGame();
        // Autogenerate
        if (ENABLE_AUTOGEN)
            FFTManager.autogenFFT();


        PlayPane playPane = new PlayPane(this);
        primaryStage.setScene(new Scene(playPane,
                Globals.WIDTH, Globals.HEIGHT));
        navPane = playPane.getNavPane();
        playArea = playPane.getPlayArea();
        window = playArea.getScene().getWindow();

        instantiateAI(PLAYER1);
        instantiateAI(PLAYER2);

        // Fetch all gui elements that invoke something game-related
        startAIButton = navPane.getStartAIButton();
        stopAIButton = navPane.getStopAIButton();
        editFFTButton = navPane.getEditFFTButton();
        addRuleFFTButton = navPane.getAddRuleFFTButton();
        reviewButton = navPane.getReviewButton();
        automaticFFTBox = navPane.getAutomaticFFTBox();
        helpHumanBox = navPane.getHelpHumanBox();

        FFTInteractivePane fftInteractivePane = new FFTInteractivePane();
        new Scene(fftInteractivePane, Globals.WIDTH, Globals.HEIGHT);
        FFTOverviewPane fftOverviewPane = new FFTOverviewPane(primaryStage, fftInteractivePane);
        new Scene(fftOverviewPane, Globals.WIDTH, Globals.HEIGHT);
        
        playArea.update(this);

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
            primaryStage.setScene(fftOverviewPane.getScene());
        });

        // add rule to FFT button
        addRuleFFTButton.setOnAction(event -> {
            Scene scene = primaryStage.getScene();
            primaryStage.setScene(fftInteractivePane.getScene());
            fftInteractivePane.update(this.node);
            fftInteractivePane.setPrevScene(scene);
        });

        // automatic mode
        fftAutomaticMode = true;
        automaticFFTBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            fftAutomaticMode = newValue;
            if (fftAutomaticMode && (node.getTurn() == PLAYER2 && player2Instance == FFT) ||
                    (node.getTurn() == PLAYER1 && player1Instance == FFT))
                doAITurn();
        });

        if (mode == HUMAN_VS_AI && player1Instance != HUMAN && node.getTurn() == PLAYER1) {
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
        if ((mode != AI_VS_AI)) {
            if (!navPane.containsHelpBox())
                navPane.addHelpHumanBox();
        }
        if (!navPane.containsFFTWidgets())
            navPane.addFFTWidgets();

    }

    private void instantiateAI(int team) {
        if (team == PLAYER1) {
            if (player1Instance == LOOKUP_TABLE) {
                aiCross = new PerfectPlayer(PLAYER1);
            } else if (player1Instance == FFT) {
                aiCross = new FFTFollower(PLAYER1);
            }
        } else {
            if (player2Instance == LOOKUP_TABLE) {
                aiCircle = new PerfectPlayer(PLAYER2);
            } else if (player2Instance == FFT) {
                aiCircle = new FFTFollower(PLAYER2);
            }
        }
    }

    // Is called when a tile is pressed by the user. If vs. the AI, it calls the doAITurn after. This function also highlights
    // the best pieces for the opponent, if it is human vs human.
    public void doHumanTurn(int row, int col) {
        Move move = new Move(row, col, node.getTurn());
        previousStates.add(new NodeAndMove(node, move, turnNo));
        node = node.getNextState(move);
        updatePostHumanTurn();
        if (Logic.gameOver(node)) return;
        if (node.getTurn() == move.team) {
            String skipped = (node.getTurn() == PLAYER1) ? "Nought" : "Cross";
            System.out.println(skipped + "'s turn has been skipped!");
            playArea.getInfoPane().displaySkippedTurn(skipped);
            if (player1Instance == FFT && node.getTurn() == PLAYER1 ||
                    player2Instance == FFT && node.getTurn() == PLAYER2)
                doAITurn();
            else if (helpHumanBox.isSelected()) {
                highlightHelp(true);
            }
            return;
        }
        // FFT vs AI
        if (mode == AI_VS_AI && ((player1Instance == FFT && node.getTurn() == PLAYER2) ||
                (player2Instance == FFT && node.getTurn() == PLAYER1))) {
            startAIButton.fire();
        // Human vs. AI
        } else if ((player2Instance != HUMAN && node.getTurn() == PLAYER2) ||
                (player1Instance != HUMAN && node.getTurn() == PLAYER1)) {
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
    }

    private void updatePostAITurn() {
        turnNo++;
        // Update gui elements on another thread
        Platform.runLater(() -> {
            playArea.update(this);
            checkGameOver();
        });
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
                while (!Logic.gameOver(node)) {
                    doAITurn();
                    if (fftUserInteraction) {
                        stopAIButton.fire();
                    }
                    Thread.sleep(0); // To allow thread interruption

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
        int turn = node.getTurn();
        Move move;
        if (aiCross != null && turn == PLAYER1) {
            move = aiCross.makeMove(node);
            if (player1Instance == FFT && move == null)
                makeDefaultMove = true;
        } else {
            move = aiCircle.makeMove(node);
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
        node = node.getNextState(move);
        updatePostAITurn();
        if (Logic.gameOver(node)) return;
        if (mode == HUMAN_VS_AI) {
            if (turn == node.getTurn()) {
                String skipped = (turn == PLAYER1) ? "Nought" : "Cross";
                System.out.println(skipped + "'s turn has been skipped!");
                playArea.getInfoPane().displaySkippedTurn(skipped);
                doAITurn();
            } else if (helpHumanBox.isSelected()) {
                highlightHelp(true);
            }
        } else if (((node.getTurn() == PLAYER1 && player1Instance == FFT) ||
                (node.getTurn() == PLAYER2 && player2Instance == FFT)) &&
                helpHumanBox.isSelected()) {
            highlightHelp(true);
        }
    }

    private void highlightHelp(boolean highlight) {

        Node n = new Node(node);
        Move fftChosenMove = null;
        ArrayList<Move> moves = Logic.legalMoves(node.getTurn(), node);
        if (highlight) {
            if (FFTManager.currFFT != null)
                fftChosenMove = (Move) FFTManager.currFFT.apply(node).iterator().next(); // TODO
        }
        BoardTile[][] tiles = playArea.getPlayBox().getBoard().getTiles();

        for (BoardTile[] tile : tiles) {
            for (BoardTile aTile : tile) {
                aTile.removeColor();
                aTile.setTurnsToTerminal("");
                if (!highlight)
                    continue;
                for (Move m : moves) {
                    if (m.col == aTile.getCol() && m.row == aTile.getRow()) {
                        Node next = n.getNextState(m);
                        NodeMapping nm = FFTSolution.queryNode(next);
                        if (nm == null) {
                            if (Logic.getWinner(next) == n.getTurn())
                                aTile.setGreen();
                            else if (Logic.getWinner(next) == PLAYER_NONE)
                                aTile.setYellow();
                            else
                                aTile.setRed();
                        } else {
                            if (nm.getWinner() == n.getTurn())
                                aTile.setGreen();
                            else if (nm.getWinner() == PLAYER_NONE)
                                aTile.setYellow();
                            else
                                aTile.setRed();
                        }
                    }
                }
                if (fftChosenMove != null && fftChosenMove.col == aTile.getCol() && fftChosenMove.row == aTile.getRow())
                    aTile.setFFTChosen();
            }
        }

        // Turns to terminal
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
            Node n = new Node(node).getNextState(m);
            if (Logic.gameOver(n)) {
                turnsToTerminalList.add("0");
            } else {
                turnsToTerminalList.add(FFTSolution.turnsToTerminal(node.getTurn(), n));
            }
        }
        return turnsToTerminalList;
    }

    private Move getDefaultFFTMove() {
        Random r = new Random();
        int moveSize = node.getLegalMoves().size();
        int index = r.nextInt(moveSize);
        return node.getLegalMoves().get(index);
    }

    // Checks if the game is over and shows a popup. Popup allows a restart, go to menu, or review game
    private void checkGameOver() {
        if (Logic.gameOver(node) && !endGamePopup) {
            endGamePopup = true;
            Stage newStage = new Stage();
            int winner = Logic.getWinner(node);
            if (player1Instance == HUMAN) node.setTurn(PLAYER1);
            else if (player2Instance == HUMAN) node.setTurn(PLAYER2);

            newStage.setScene(new Scene(new EndGamePane(primaryStage, winner,
                    this), 500, 300));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(window);
            newStage.setOnCloseRequest(Event::consume);
            newStage.show();
        }
    }

    public Node getNode() {
        return node;
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
        newStage.setScene(new Scene(new ReviewPane(primaryStage, this), 325, Globals.HEIGHT - 50));
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

    public Window getWindow() {
        return window;
    }

    public boolean getFFTAllowInteraction() {
        return fftUserInteraction;
    }

    public ArrayList<NodeAndMove> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(ArrayList<NodeAndMove> nodeAndMoves) {
        this.previousStates = nodeAndMoves;
    }

    public InteractiveNode getInteractiveNode() {
        return gameSpecifics.interactiveNode;
    }

    public PlayArea getPlayArea() {
        return playArea;
    }

    public FFT getCurrFFT() {
        return FFTManager.currFFT;
    }

}
