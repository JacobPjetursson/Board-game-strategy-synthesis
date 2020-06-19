package fftlib;

import fftlib.auxiliary.Position;
import fftlib.game.*;
import fftlib.gui.FFTFailNode;
import fftlib.gui.interactiveFFTNode;
import fftlib.logic.*;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static misc.Config.SAVE_FFT;


public class FFTManager {
    // Misc
    private static int fft_index = 0;
    public static ArrayList<FFT> ffts;
    private static String fftPath;
    public static FFT currFFT;
    // Domain specific
    public static int gameBoardWidth;
    public static int gameBoardHeight;
    public static ArrayList<Integer> legalIndices;
    public static FFTNode initialFFTNode;
    public static Function<Rule, HashSet<SymmetryRule>> getSymmetryRules;
    public static int maxStateLiterals;
    public static int winner; // set by solver
    // Visual tool
    private static FFTFailNode failNode;
    public static interactiveFFTNode interactiveNode;
    public static String[] playerNames;
    // Interface between domain specific and logic
    public static Function<Action, FFTMove> actionToMove;
    public static Function<FFTMove, Action> moveToAction;
    public static BiFunction<String, String, Rule> gdlToRule;
    public static Function<FFTNode, LiteralSet> nodeToLiterals;
    // Logic representation
    public static Supplier<ArrayList<Integer>> getGameAtoms;
    public static ArrayList<Integer> sortedGameAtoms;
    public static Function<String, Integer> getAtomId;
    public static Function<Integer, String> getAtomName;
    public static Function<Position, Integer> getIdFromPos;
    public static Function<Integer, Position> getPosFromId;
    public static Function<Action, LiteralSet> getActionPreconditions;
    public static Function<Rule, Long> getNumberOfCoveredStates;
    public static Function<Rule, HashSet<LiteralSet>> getCoveredStates;

    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    public static final String blueBtnStyle = "-fx-border-color: #000000; -fx-background-color: #4444ff;";
    public static final String redBtnStyle = "-fx-border-color: #000000; -fx-background-color: #ff2222;";
    public static final String greenBtnStyle = "-fx-border-color: #000000; -fx-background-color: #22ff22;";
    public static final String orangeBtnStyle = "-fx-border-color: #000000; -fx-background-color: #ffa500;";

    // Most game-related classes are processed here
    public static void initialize(FFTGameSpecifics gameSpecifics) {
        // Misc
        ffts = new ArrayList<>();
        fftPath = gameSpecifics.getFFTFilePath();
        // Interface
        actionToMove = gameSpecifics::actionToMove;
        moveToAction = gameSpecifics::moveToAction;
        gdlToRule = gameSpecifics::gdlToRule;
        nodeToLiterals = gameSpecifics::nodeToLiterals;
        // Domain Specific
        initialFFTNode = gameSpecifics.getInitialNode();
        maxStateLiterals = gameSpecifics.getMaxStateLiterals();
        getSymmetryRules = gameSpecifics::getSymmetryRules;
        int[] dim = gameSpecifics.getBoardDim();
        gameBoardHeight = dim[0];
        gameBoardWidth = dim[1];
        legalIndices = gameSpecifics.legalIndices();
        // Visual Tool
        failNode = gameSpecifics.getFailNode();
        interactiveNode = gameSpecifics.getInteractiveNode();
        playerNames = gameSpecifics.getPlayerNames();
        // Logic representation
        getGameAtoms = gameSpecifics::getGameAtoms;
        getAtomId = gameSpecifics::getAtomId;
        getAtomName = gameSpecifics::getAtomName;
        getPosFromId = gameSpecifics::idToPos;
        getIdFromPos = gameSpecifics::posToId;
        getActionPreconditions = gameSpecifics::getActionPreconditions;
        sortedGameAtoms = getGameAtoms.get();
        sortedGameAtoms.sort(Collections.reverseOrder());
        getNumberOfCoveredStates = gameSpecifics::getNumberOfCoveredStates;
        getCoveredStates = gameSpecifics::getCoveredStates;


        // Try loading ffts from file in working directory
        ffts = load(fftPath);
        if (!ffts.isEmpty())
            currFFT = ffts.get(fft_index);
    }

    public static void save() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fftPath));
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
                        ffts.get(fftIndex).append(new Rule(clauseStr, actionStr));
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

    public static Node getFailNode() {
        FFTNodeAndMove ps = currFFT.failingPoint;
        FFTNode n = ps.getNode();
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
        return failNode.getFailNode(ps, optimalMoves);
    }

    public static FFT autogenFFT() {
        FFT fft = FFTAutoGen.generateFFT(Config.AUTOGEN_TEAM);
        ffts.add(0, fft);
        currFFT = fft;
        if (SAVE_FFT) {
            System.out.println("Saving FFT");
            save();
        }
        return currFFT;
    }

    public static FFT autogenFFT(FFT fft) { // autogen using a current fft
        FFT newFFT = FFTAutoGen.generateFFT(Config.AUTOGEN_TEAM, fft);
        ffts.add(0, newFFT);
        currFFT = newFFT;
        return currFFT;
    }

}

