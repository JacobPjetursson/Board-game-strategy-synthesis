package fftlib;

import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import misc.Config;
import misc.Globals;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;


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
    public static BiFunction<String, String, Rule> gdlToRule;
    public static int gameWinner;
    public static int MAX_PRECONS;
    public static String[] playerNames;
    private static int fft_index = 0;

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
        gdlToRule = gameSpecifics::gdlToRule;
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
        if (Config.ENABLE_AUTOGEN) {
            FFT fft = FFTAutoGen.generateFFT(Config.AUTOGEN_PERSPECTIVE, gameWinner);
            ffts.add(0, fft);
            currFFT = fft;
        }
        Config.ENABLE_AUTOGEN = false;
    }

}

