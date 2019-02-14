package fftlib;

import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


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
    static Function<Action, FFTMove> actionToMove;
    public static Function<Clause, FFTState> clauseToState;

    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

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
        clauseToState = gameSpecifics::clauseToState;
        failState = gameSpecifics.getFailState();
        interactiveState = gameSpecifics.getInteractiveState();

        // Try loading ffts from file in working directory
        load();
        if (!ffts.isEmpty())
            currFFT = ffts.get(0);

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
                        fft_file.append(r.getClauseStr()).append(" -> ").append(r.getActionStr()).append("\n");
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
                RuleGroup rg = null;
                FFT fft = null;
                for (String line : lines) {
                    if (line.startsWith("{")) {
                        if (fft != null) {
                            ffts.add(fft);
                        }
                        fft = new FFT(line.substring(1, line.length() - 1));
                    }
                    // Rulegroup name
                    else if (line.startsWith("[")) {
                        if (rg != null && fft != null) {
                            fft.ruleGroups.add(rg);
                        }
                        rg = new RuleGroup(line.substring(1, line.length() - 1));
                    } else {
                        String[] rule = line.split("->");
                        String clauseStr = rule[0].trim();
                        String actionStr = rule[1].trim();
                        if (rg != null)
                            rg.rules.add(new Rule(clauseStr, actionStr));

                    }
                }
                // In case of at least 1 FFT
                if (fft != null) {
                    if (rg != null)
                        fft.ruleGroups.add(rg);

                    ffts.add(fft);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrFFT(int index) {
        currFFT = ffts.get(index);
    }

    public void addNewFFT(String name) {
        FFT newFFT = new FFT(name);
        ffts.add(newFFT);
        currFFT = newFFT;
    }

    public void deleteCurrFFT() {
        ffts.remove(currFFT);
        if (!ffts.isEmpty())
            currFFT = ffts.get(0);
        else
            currFFT = null;
    }

    public Node getFailState() {
        FFTStateAndMove ps = currFFT.failingPoint;
        FFTState s = ps.getState();
        ArrayList<? extends FFTMove> nonLosingPlays = FFTManager.db.nonLosingPlays(s);
        return failState.getFailState(ps, nonLosingPlays);
    }


}

