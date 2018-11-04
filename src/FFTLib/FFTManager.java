package FFT;
import FFT.RuleGroup;
import misc.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FFTManager {
    static String path = Config.FFT_PATH;
    static ArrayList<FFT> ffts;
    public FFT currFFT;

    public FFTManager() {
        ffts = new ArrayList<>();
        // Try loading ffts from file in working directory
        load();
        if (!ffts.isEmpty())
            currFFT = ffts.get(0);

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

    public static void save() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            String fft_file = "";
            for (FFT fft : ffts) {
                fft_file += "{" + fft.name + "}\n";
                for (RuleGroup rg : fft.ruleGroups) {
                    fft_file += "[" + rg.name + "]\n";
                    for (Rule r : rg.rules) {
                        fft_file += r.getClauseStr() + " -> " + r.getActionStr() + "\n";
                    }
                }
            }
            writer.write(fft_file);
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
                        if (rg != null) {
                            fft.ruleGroups.add(rg);
                        }
                        rg = new RuleGroup(line.substring(1, line.length() - 1));
                    } else {
                        String[] rule = line.split("->");
                        String clausesStr = rule[0].trim();
                        String actionStr = rule[1].trim();
                        if (rg != null) {
                            rg.rules.add(new Rule(clausesStr, actionStr));
                        }
                    }
                }
                // In case of at least 1 FFT
                if (fft != null) {
                    if(rg != null)
                        fft.ruleGroups.add(rg);

                    ffts.add(fft);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

