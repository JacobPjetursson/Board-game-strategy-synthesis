package misc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import static misc.Globals.*;
import static misc.Globals.RULE_ORDERING_TERMINAL_FIRST;

public class Config {

    // GLOBAL PROPERTIES
    public static int AUTOGEN_TEAM;
    public static boolean GREEDY_AUTOGEN;
    public static boolean RANDOM_SEED;
    public static int SEED;
    public static boolean ENABLE_GGP;
    public static boolean ENABLE_GGP_PARSER;
    public static int RULE_ORDERING;
    public static boolean MINIMIZE_PRECONDITIONS;
    public static boolean SYMMETRY_DETECTION;
    public static boolean SAVE_STRAT;
    public static boolean GENERATE_ALL_RULES;
    public static boolean MINIMIZE_RULE_BY_RULE;
    public static boolean MINIMIZE_BOTTOMS_UP;
    public static boolean SINGLE_THREAD;
    public static boolean USE_FILTERING;
    public static boolean SHOW_RULE_GROUPS;
    public static boolean USE_DEBUG_FILE;
    public static String DEBUG_FILENAME;
    public static boolean BENCHMARK_MODE;
    public static int BENCHMARK_NUMBER;
    public static boolean USE_OLD_VERIFICATION;

    // KULIBRAT PROPERTIES
    public static int BWIDTH;
    public static int BHEIGHT;
    public static boolean USE_DB;
    public static boolean KULIBRAT_SHOW_GUI;
    public static String DB_PATH;

    // TIC TAC TOE PROPERTIES
    public static int TIC_TAC_TOE_RULES;
    public static boolean TIC_TAC_TOE_SHOW_GUI;

    // SIM
    public static boolean SIM_SIMPLE_RULES;

    // MENS MORRIS
    public static boolean THREE_MENS;

    // GGP-FFT PROPERTIES
    public static boolean DETAILED_DEBUG;
    public static boolean VERIFY_SINGLE_STRATEGY;
    public static String GGP_GAME;

    static {
        try {
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printSettings();
        setupDebugFile();
    }

    private static void loadProperties() throws IOException {
        Properties global = new Properties();
        Properties tictactoe = new Properties();
        Properties kulibrat = new Properties();
        Properties sim = new Properties();
        Properties morris = new Properties();
        Properties ggp = new Properties();

        global.load(new FileInputStream("properties/global.properties"));
        tictactoe.load(new FileInputStream("properties/tictactoe.properties"));
        kulibrat.load(new FileInputStream("properties/kulibrat.properties"));
        sim.load(new FileInputStream("properties/sim.properties"));
        morris.load(new FileInputStream("properties/mens_morris.properties"));
        ggp.load(new FileInputStream("properties/ggp.properties"));

        // GLOBAL CONFIG
        String perspective = global.getProperty("autogen_team", "player1");
        switch(perspective) {
            case "player2":
                AUTOGEN_TEAM = Globals.PLAYER2;
                break;
            case "both":
                AUTOGEN_TEAM = Globals.PLAYER_ANY;
                break;
            default:
                AUTOGEN_TEAM = Globals.PLAYER1;
        }

        String ordering = global.getProperty("rule_ordering");
        switch (ordering) {
            case "terminal_first":
                RULE_ORDERING = Globals.RULE_ORDERING_TERMINAL_FIRST;
                break;
            case "terminal_last":
                RULE_ORDERING = Globals.RULE_ORDERING_TERMINAL_LAST;
                break;
            case "fewest_precons_first":
                RULE_ORDERING = Globals.RULE_ORDERING_FEWEST_PRECONS_FIRST;
                break;
            case "fewest_precons_last":
                RULE_ORDERING = Globals.RULE_ORDERING_FEWEST_PRECONS_LAST;
                break;
            default:
                RULE_ORDERING = Globals.RULE_ORDERING_RANDOM;
        }

        GREEDY_AUTOGEN = Boolean.parseBoolean(global.getProperty("greedy_autogen"));
        RANDOM_SEED = Boolean.parseBoolean(global.getProperty("random_seed"));
        SEED = Integer.parseInt(global.getProperty("seed"));
        ENABLE_GGP = Boolean.parseBoolean(global.getProperty("enable_ggp"));
        ENABLE_GGP_PARSER = Boolean.parseBoolean(global.getProperty("enable_ggp_parser"));
        MINIMIZE_PRECONDITIONS = Boolean.parseBoolean(global.getProperty("minimize_preconditions"));
        GENERATE_ALL_RULES = Boolean.parseBoolean(global.getProperty("generate_all_rules"));
        MINIMIZE_RULE_BY_RULE = Boolean.parseBoolean(global.getProperty("minimize_rule_by_rule"));
        MINIMIZE_BOTTOMS_UP = Boolean.parseBoolean(global.getProperty("minimize_bottoms_up"));
        SYMMETRY_DETECTION = Boolean.parseBoolean(global.getProperty("symmetry_detection"));
        SAVE_STRAT = Boolean.parseBoolean(global.getProperty("save_intermediate_strategy"));
        SHOW_RULE_GROUPS = Boolean.parseBoolean(global.getProperty("show_rule_groups"));
        // DEBUG AND TEST
        DETAILED_DEBUG = Boolean.parseBoolean(global.getProperty("detailedDebug"));
        BENCHMARK_MODE = Boolean.parseBoolean(global.getProperty("benchmark_mode"));
        BENCHMARK_NUMBER = Integer.parseInt(global.getProperty("no_of_benchmarks"));
        USE_DEBUG_FILE = Boolean.parseBoolean(global.getProperty("use_debug_file"));
        DEBUG_FILENAME = global.getProperty("debug_filename");
        VERIFY_SINGLE_STRATEGY = Boolean.parseBoolean(global.getProperty("verify_single_strategy"));
        SINGLE_THREAD = Boolean.parseBoolean(global.getProperty("single_thread"));
        USE_FILTERING = Boolean.parseBoolean(global.getProperty("use_filtering"));
        USE_OLD_VERIFICATION = Boolean.parseBoolean(global.getProperty("use_old_verification"));

        // KULIBRAT
        BWIDTH = Integer.parseInt(kulibrat.getProperty("boardWidth"));
        BHEIGHT = Integer.parseInt(kulibrat.getProperty("boardHeight"));
        USE_DB = Boolean.parseBoolean(kulibrat.getProperty("use_db"));
        KULIBRAT_SHOW_GUI = Boolean.parseBoolean(kulibrat.getProperty("show_gui"));
        DB_PATH = kulibrat.getProperty("db_path");

        // TIC TAC TOE
        String rules = tictactoe.getProperty("rules");
        switch (rules) {
            case "simple":
                TIC_TAC_TOE_RULES = TIC_TAC_TOE_SIMPLE_RULES;
                break;
            case "stupid":
                TIC_TAC_TOE_RULES = TIC_TAC_TOE_STUPID_RULES;
                break;
            default:
                TIC_TAC_TOE_RULES = TIC_TAC_TOE_NORMAL_RULES;
        }
        TIC_TAC_TOE_SHOW_GUI = Boolean.parseBoolean(tictactoe.getProperty("show_gui"));

        // SIM
        SIM_SIMPLE_RULES = Boolean.parseBoolean(sim.getProperty("simple_rules"));

        // MENS MORRIS
        THREE_MENS = Boolean.parseBoolean(morris.getProperty("three_mens"));

        // GGP
        GGP_GAME = ggp.getProperty("ggp_game");
    }

    private static void printSettings() {
        System.out.println("PRINTING PROPERTIES BELOW:");
        System.out.println("--------------------------");
        String perspectiveStr = (AUTOGEN_TEAM == PLAYER1) ? "Player 1" :
                (AUTOGEN_TEAM == PLAYER2) ? "Player 2" : "Both";
        String ruleOrderingStr = (RULE_ORDERING == RULE_ORDERING_RANDOM) ? "Random" :
                (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST) ? "Fewest preconditions first" :
                        (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_LAST) ? "Fewest preconditions last" :
                                (RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST) ? "Close to terminal first" :
                                        "Close to terminal last";

        System.out.printf("%-30.40s %-30.40s\n", "Autogen perspective:", perspectiveStr);
        System.out.printf("%-30.40s %-30.40s\n", "Rule ordering:", ruleOrderingStr);
        System.out.printf("%-30.40s %-30.40s\n", "Minimize preconditions:", MINIMIZE_PRECONDITIONS);
        System.out.printf("%-30.40s %-30.40s\n", "Symmetry detection:", SYMMETRY_DETECTION);
        System.out.printf("%-30.40s %-30.40s\n", "Greedy Autogeneration:", GREEDY_AUTOGEN);
        System.out.printf("%-30.40s %-30.40s\n", "Generate all rules:", GENERATE_ALL_RULES);
        System.out.printf("%-30.40s %-30.40s\n", "Minimize rule by rule:", MINIMIZE_RULE_BY_RULE);
        System.out.printf("%-30.40s %-30.40s\n", "Minimize bottoms up:", MINIMIZE_BOTTOMS_UP);

        System.out.printf("%-30.40s %-30.40s\n", "Random seed:", RANDOM_SEED);
        System.out.printf("%-30.40s %-30.40s\n", "Seed value:",SEED);
        System.out.printf("%-30.40s %-30.40s\n", "Detailed debug messages:", DETAILED_DEBUG);
        System.out.printf("%-30.40s %-30.40s\n", "Using debug file:", USE_DEBUG_FILE);
        System.out.printf("%-30.40s %-30.40s\n", "Benchmark mode:", BENCHMARK_MODE);
        System.out.printf("%-30.40s %-30.40s\n", "Single thread:", SINGLE_THREAD);
        System.out.printf("%-30.40s %-30.40s\n", "Verify single strategy:", VERIFY_SINGLE_STRATEGY);
        System.out.printf("%-30.40s %-30.40s\n", "Use filtering:", USE_FILTERING);
        System.out.printf("%-30.40s %-30.40s\n", "Use old verification:", USE_OLD_VERIFICATION);
    }

    private static void setupDebugFile() {
        if (USE_DEBUG_FILE) {
            PrintStream debugFile = null;
            try {
                debugFile = new PrintStream(DEBUG_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.setOut(debugFile);
        }
    }
}
