package fftlib;

import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import misc.Config;

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
import java.util.function.Function;
import java.util.function.Supplier;

import static misc.Config.USE_OLD_VERIFICATION;


public class FFTManager {
    public static ArrayList<FFT> ffts;
    public static FFTLogic logic;
    public static int gameBoardWidth;
    public static int gameBoardHeight;
    private static String path;
    public static FFTState initialFFTState;
    public static FFT currFFT;
    private static FFTFailState failState;
    public static InteractiveFFTState interactiveState;
    public static Function<Action, FFTMove> actionToMove;
    public static Function<FFTMove, Action> moveToAction;
    public static BiFunction<String, String, Rule> gdlToRule;
    public static Function<Rule, HashSet<SymmetryRule>> getSymmetryRules;
    public static Function<Position, Integer> getIdFromPos;
    public static Function<Integer, Position> getPosFromId;

    // Game atoms
    public static Supplier<ArrayList<Integer>> getGameAtoms;
    public static Function<String, Integer> getAtomId;
    public static Function<Integer, String> getAtomName;
    public static String[] playerNames;
    private static int fft_index = 0;

    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    public static final String blueBtnStyle = "-fx-border-color: #000000; -fx-background-color: #4444ff;";
    public static final String redBtnStyle = "-fx-border-color: #000000; -fx-background-color: #ff2222;";
    public static final String greenBtnStyle = "-fx-border-color: #000000; -fx-background-color: #22ff22;";
    public static final String orangeBtnStyle = "-fx-border-color: #000000; -fx-background-color: #ffa500;";

    // Most game-related classes are processed here
    public static void initialize(FFTGameSpecifics gameSpecifics) {
        ffts = new ArrayList<>();

        initialFFTState = gameSpecifics.getInitialState();
        logic = gameSpecifics.getLogic();
        path = gameSpecifics.getFFTFilePath();
        int[] dim = gameSpecifics.getBoardDim();
        gameBoardHeight = dim[0];
        gameBoardWidth = dim[1];
        actionToMove = gameSpecifics::actionToMove;
        moveToAction = gameSpecifics::moveToAction;
        gdlToRule = gameSpecifics::gdlToRule;
        failState = gameSpecifics.getFailState();
        interactiveState = gameSpecifics.getInteractiveState();
        playerNames = gameSpecifics.getPlayerNames();
        getGameAtoms = gameSpecifics::getGameAtoms;
        getAtomId = gameSpecifics::getAtomId;
        getAtomName = gameSpecifics::getAtomName;
        getPosFromId = gameSpecifics::idToPos;
        getIdFromPos = gameSpecifics::posToId;
        getSymmetryRules = gameSpecifics::getSymmetryRules;

        // Try loading ffts from file in working directory
        ffts = load(path);
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
                    fft_file.append("[").append(rg.name).append("]");
                    if (rg.locked)
                        fft_file.append("*");
                    fft_file.append("\n");
                    for (Rule r : rg.rules) {
                        fft_file.append(r.getPreconString()).append(" -> ").append(r.getActionString()).append("\n");
                    }
                }
            }
            writer.write(fft_file.toString());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<FFT> load(String filePath) {
        List<String> lines;
        // Create new file if not exists
        File fftFile = new File(filePath);
        ArrayList<FFT> ffts = new ArrayList<>();
        try {
            if (!fftFile.createNewFile()) {
                lines = Files.readAllLines(Paths.get(filePath));
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
                        int length = (line.endsWith("*")) ? line.length() - 2 : line.length() - 1;
                        RuleGroup rg = new RuleGroup(line.substring(1, length));
                        if (line.endsWith("*"))
                            rg.locked = true;
                        ffts.get(fftIndex).addRuleGroup(rg);
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
        return ffts;
    }

    public static void setCurrFFT(int index) {
        currFFT = ffts.get(index);
        fft_index = index;
    }

    public static void addNewFFT(String name) {
        FFT newFFT = new FFT(name);
        ffts.add(newFFT);
        currFFT = newFFT;
        fft_index = ffts.size() - 1;

        save();
    }

    public static void deleteCurrFFT() {
        ffts.remove(currFFT);
        if (!ffts.isEmpty()) {
            currFFT = ffts.get(0);
            fft_index = 0;
        }
        else
            currFFT = null;

        save();
    }

    public static Node getFailState() {
        FFTStateAndMove ps = currFFT.failingPoint;
        FFTState s = ps.getState();
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(s);
        return failState.getFailState(ps, optimalMoves);
    }

    public static FFT autogenFFT() {
        FFT fft;
        if (USE_OLD_VERIFICATION) {
            fft = FFTAutoGenOld.generateFFT(Config.AUTOGEN_TEAM);
        } else {
            fft = FFTAutoGen.generateFFT(Config.AUTOGEN_TEAM);
        }
        ffts.add(0, fft);
        currFFT = fft;
        return currFFT;
    }

    // TODO - support existing fft
    public static FFT autogenFFT(FFT fft) { // autogen using a current fft
        //FFT newFFT = FFTAutoGen.generateFFT(Config.AUTOGEN_PERSPECTIVE, fft);
        //ffts.add(0, newFFT);
        //currFFT = newFFT;
        return currFFT;
    }

}

