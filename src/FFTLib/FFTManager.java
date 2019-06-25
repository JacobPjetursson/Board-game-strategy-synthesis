package fftlib;

import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import misc.Config;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;

import static misc.Config.*;


public class FFTManager {
    public static ArrayList<FFT> ffts;
    public static FFTDatabase db;
    public static FFTLogic logic;
    static int [] gameSymmetries;
    static int gameBoardWidth, gameBoardHeight;
    private static String path;
    public static FFTState initialFFTState;
    public FFT currFFT;
    private static FFTFailState failState;
    public static InteractiveFFTState interactiveState;
    public static BiFunction<Action, Integer, FFTMove> actionToMove;
    public static BiFunction<HashSet<Literal>, Integer, FFTState> preconsToState;
    public static int gameWinner;
    public static int MAX_PRECONS;
    public static String[] playerNames;
    private static int fft_index = 0;

    // General game playing
    public static StateMachine sm;
    public static Role p1role;
    public static Role p2role;
    public static Move noop;

    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    public static final String blueBtnStyle = "-fx-border-color: #000000; -fx-background-color: #4444ff;";
    public static final String redBtnStyle = "-fx-border-color: #000000; -fx-background-color: #ff2222;";
    public static final String greenBtnStyle = "-fx-border-color: #000000; -fx-background-color: #22ff22;";
    public static final String orangeBtnStyle = "-fx-border-color: #000000; -fx-background-color: #ffa500;";

    // Most game-related classes are processed here
    public FFTManager(FFTGameSpecifics gameSpecifics) {
        ffts = new ArrayList<>();
        initialFFTState = gameSpecifics.getInitialState();
        logic = gameSpecifics.getLogic();
        db = gameSpecifics.getDatabase();
        path = gameSpecifics.getFFTFilePath();
        gameSymmetries = gameSpecifics.getAllowedTransformations();
        int[] dim = gameSpecifics.getBoardDim();
        gameBoardHeight = dim[0];
        gameBoardWidth = dim[1];
        actionToMove = gameSpecifics::actionToMove;
        preconsToState = gameSpecifics::preconsToState;
        failState = gameSpecifics.getFailState();
        interactiveState = gameSpecifics.getInteractiveState();
        gameWinner = gameSpecifics.getGameWinner();
        playerNames = gameSpecifics.getPlayerNames();
        MAX_PRECONS = gameSpecifics.getMaxPrecons();

        // Try loading ffts from file in working directory
        load();
        if (!ffts.isEmpty())
            currFFT = ffts.get(fft_index);

    }

    public static void initialize(List<Gdl> rules) {
        sm = new ProverStateMachine();
        sm.initialize(rules);
        p1role = FFTManager.sm.getRoles().get(0);
        p2role = FFTManager.sm.getRoles().get(1);
        noop = new Move(GdlPool.getConstant("noop"));
    }

    /* BELOW FUNCTIONS SHOULD ALL BE IN A SUBCLASS OF MACHINESTATE, OR SIMILAR! */
    public static MachineState getNextState(MachineState ms, Move move) throws MoveDefinitionException, TransitionDefinitionException {
        Role r = getStateRole(ms);
        int roleIdx = sm.getRoleIndices().get(r);
        Move[] moveList = new Move[] {noop, noop};
        moveList[roleIdx] = move;
        return sm.getNextState(ms, Arrays.asList(moveList));
    }

    public static Role getStateRole(MachineState ms) {
        try {
            List<Move> moves = FFTManager.sm.getLegalMoves(ms, p1role);
            return (moves.size() == 1 && moves.get(0).equals(noop)) ? p2role : p1role;
        } catch (MoveDefinitionException e) {
            return p2role;
        }
    }

    public static int roleToPlayer(Role r) {
        return r.equals(p1role) ? PLAYER1 : PLAYER2;
    }

    /** Return all roles if draw, otherwise return role that won the game. Assumes turn-based game **/
    public static List<Role> getWinners(MachineState ms) throws GoalDefinitionException {
        if (!sm.isTerminal(ms)) // TODO - catch exception and suppress output
            return null;

        List<Integer> goals = sm.getGoals(ms); // same order as getRoles()

        int max = Collections.max(goals);

        List<Role> roles = sm.getRoles();
        List<Role> winners = new ArrayList<>();
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i) == max) {
                winners.add(roles.get(i));
            }
        }
        return winners;
    }

    public static List<Integer> getPlayerWinners(MachineState ms) throws GoalDefinitionException {
        List<Role> roleWinners = getWinners(ms);
        if (roleWinners == null)
            return null;
        List<Integer> playerWinners = new ArrayList<>();
        for (Role r : roleWinners) {
            playerWinners.add(roleToPlayer(r));
        }
        return playerWinners;
    }
    /* ------------------------------------------------------------------ */

    public static void save() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            StringBuilder fft_file = new StringBuilder();
            for (FFT fft : ffts) {
                fft_file.append("{").append(fft.name).append("}\n");
                for (RuleGroup rg : fft.ruleGroups) {
                    fft_file.append("[").append(rg.name).append("]\n");
                    for (Rule r : rg.rules) {
                        fft_file.append(r.getPreconStr()).append(" -> ").append(r.getActionStr()).append("\n");
                    }
                }
            }
            writer.write(fft_file.toString());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void load() {
        List<String> lines;
        // Create new file if not exists
        File fftFile = new File(path);
        try {
            if (!fftFile.createNewFile()) {
                lines = Files.readAllLines(Paths.get(path));
                int fftIndex = -1;
                int rgIndex = -1;
                for (String line : lines) {
                    if (line.startsWith("{")) {
                        ffts.add(new FFT(line.substring(1, line.length() - 1)));
                        fftIndex++;
                        rgIndex = -1;
                    }
                    // Rulegroup name
                    else if (line.startsWith("[")) {
                        ffts.get(fftIndex).addRuleGroup(new RuleGroup(line.substring(1, line.length() - 1)));
                        rgIndex++;
                    } else {
                        String[] rule = line.split("->");
                        String clauseStr = rule[0].trim();
                        String actionStr = rule[1].trim();
                        ffts.get(fftIndex).ruleGroups.get(rgIndex).addRule(new Rule(clauseStr, actionStr));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrFFT(int index) {
        currFFT = ffts.get(index);
        fft_index = index;
    }

    public void addNewFFT(String name) {
        FFT newFFT = new FFT(name);
        ffts.add(newFFT);
        currFFT = newFFT;
        fft_index = ffts.size() - 1;

        save();
    }

    public void deleteCurrFFT() {
        ffts.remove(currFFT);
        if (!ffts.isEmpty()) {
            currFFT = ffts.get(0);
            fft_index = 0;
        }
        else
            currFFT = null;

        save();
    }

    public Node getFailState() {
        FFTStateAndMove ps = currFFT.failingPoint;
        FFTState s = ps.getState();
        ArrayList<? extends FFTMove> nonLosingMoves = FFTManager.db.nonLosingMoves(s);
        return failState.getFailState(ps, nonLosingMoves);
    }

    public void autogenFFT() throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        if (Config.USE_AUTOGEN) {
            FFT fft = FFTAutoGen.generateFFT(AUTOGEN_PERSPECTIVE, gameWinner);
            ffts.add(0, fft);
            currFFT = fft;
        }
        USE_AUTOGEN = false;
    }

}

