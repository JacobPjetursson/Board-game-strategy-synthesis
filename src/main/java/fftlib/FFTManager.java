package fftlib;

import fftlib.auxiliary.Position;
import fftlib.game.*;
import fftlib.gui.FFTFailNode;
import fftlib.gui.FFTRuleEditPane;
import fftlib.logic.*;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.rule.*;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;

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


public class FFTManager {
    // Misc
    public static int fftIndex = 0;
    public static ArrayList<FFT> ffts;
    private static String fftPath;
    public static FFT currFFT;
    // Domain specific
    public static int gameBoardWidth;
    public static int gameBoardHeight;
    public static ArrayList<Integer> legalIndices;
    public static FFTNode initialFFTNode;
    public static Function<PropRule, HashSet<SymmetryRule>> getSymmetryRules;
    public static int maxStateLiterals;
    public static int winner; // set by solver
    // Visual tool
    private static FFTFailNode failNode;
    public static FFTRuleEditPane fftRuleEditPane;
    public static String[] playerNames;
    // Interface between domain specific and logic
    public static Function<Action, FFTMove> actionToMove;
    public static Function<FFTMove, Action> moveToAction;
    public static BiFunction<String, String, PropRule> gdlToRule;
    public static Function<FFTNode, LiteralSet> nodeToLiterals;
    // Logic representation
    public static Supplier<ArrayList<Integer>> getGameAtoms;
    public static ArrayList<Integer> sortedGameAtoms;
    public static Function<String, Integer> getAtomId;
    public static Function<Integer, String> getAtomName;
    public static Function<Position, Integer> getIdFromPos;
    public static Function<Integer, Position> getPosFromId;
    public static Function<Action, LiteralSet> getActionPreconditions;

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
        initialFFTNode.setReachable(true);
        maxStateLiterals = gameSpecifics.getMaxStateLiterals();
        getSymmetryRules = gameSpecifics::getSymmetryRules;
        int[] dim = gameSpecifics.getBoardDim();
        gameBoardHeight = dim[0];
        gameBoardWidth = dim[1];
        legalIndices = gameSpecifics.legalIndices();
        // Visual Tool
        failNode = gameSpecifics.getFailNode();
        fftRuleEditPane = gameSpecifics.getInteractiveNode();
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


        // Try loading ffts from file in working directory
        //loadFFTs(fftPath);
    }

    public static void save() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fftPath));
            StringBuilder fft_file = new StringBuilder();
            for (FFT fft : ffts) {
                fft_file.append("{").append(fft.getName()).append("\n");
                for (Rule r : fft.getRules())
                    fft_file.append(r.getPreconditions()).append(" -> ").append(r.getAction()).append("\n");
                fft_file.append("}\n");
                for (RuleGroup rg : fft.getRuleGroups()) {
                    fft_file.append("[").append(rg.name);
                    if (rg.isLocked())
                        fft_file.append("*");
                    fft_file.append("\n");
                    fft_file.append(rg.startIdx).append("\n");
                    fft_file.append(rg.endIdx).append("\n");
                }
                fft_file.append("\n");
            }
            writer.write(fft_file.toString());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void loadFFTs() {
        List<String> lines;
        // Create new file if not exists
        File fftFile = new File(fftPath);
        ffts = new ArrayList<>();
        try {
            if (!fftFile.createNewFile()) {
                lines = Files.readAllLines(Paths.get(fftPath));

                FFT f = null;
                for (String line : lines) {
                    if (line.isBlank())
                        continue;
                    line = line.trim();
                    if (line.startsWith("{")) {
                        if (f != null)
                            ffts.add(f);
                        f = new FFT(line.substring(1, line.length()-1));
                    }
                    else if (line.startsWith("[")) {
                        boolean locked = line.endsWith("*");
                        int length = (locked) ? line.length() - 2 : line.length() - 1;
                        String trimmed = line.substring(1, length);
                        String[] parts = trimmed.split(",");
                        RuleGroup rg = new RuleGroup(f, parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                        f.addRuleGroup(rg);

                    }
                    else {
                        String[] rule = line.split("->");
                        String clauseStr = rule[0];
                        String actionStr = rule[1];
                        Rule r = new PropRule(clauseStr, actionStr);
                        f.append(r);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!ffts.isEmpty())
            currFFT = ffts.get(fftIndex);
    }

    public static void setCurrFFT(int index) {
        currFFT = ffts.get(index);
        fftIndex = index;
    }

    public static void addNewFFT(String name) {
        FFT newFFT = new FFT(name);
        ffts.add(newFFT);
        currFFT = newFFT;
        fftIndex = ffts.size() - 1;
    }

    public static void addNewFFT(FFT fft) {
        ffts.add(fft);
        currFFT = fft;
        fftIndex = ffts.size() - 1;
    }

    public static void deleteCurrFFT() {
        ffts.remove(currFFT);
        if (!ffts.isEmpty()) {
            currFFT = ffts.get(0);
            fftIndex = 0;
        }
        else
            addNewFFT("Synthesis");
    }

    public static String getPlayerName(int team) {
        return playerNames[team-1];
    }

    public static Node getFailNode() {
        FFTNodeAndMove ps = currFFT.getFailingPoint();
        FFTNode n = ps.getNode();
        ArrayList<? extends FFTMove> optimalMoves = FFTSolution.optimalMoves(n);
        return failNode.getFailNode(ps, optimalMoves);
    }

}

