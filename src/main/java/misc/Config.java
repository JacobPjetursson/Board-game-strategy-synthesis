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
    public static boolean SHOW_GUI;
    public static int RULE_ORDERING;
    public static boolean MINIMIZE_PRECONDITIONS;
    public static boolean SYMMETRY_DETECTION;
    public static boolean NAIVE_RULE_GENERATION;
    public static boolean MINIMIZE_RULE_BY_RULE;
    public static boolean REMOVE_DEAD_RULES;
    public static boolean SINGLE_THREAD;
    public static boolean SHOW_RULE_GROUPS;
    public static boolean USE_DEBUG_FILE;
    public static String DEBUG_FILENAME;
    public static boolean BENCHMARK_MODE;
    public static int BENCHMARK_NUMBER;
    public static boolean USE_BITSTRING_SORT_OPT;
    public static boolean USE_INVERTED_LIST_NODES_OPT;
    public static boolean USE_INVERTED_LIST_RULES_OPT;
    public static boolean USE_APPLY_OPT;
    public static boolean USE_MINIMIZE_OPT;
    public static boolean USE_RULE_ORDERING;
    public static boolean SIMPLIFY_ITERATIVELY;
    public static boolean USE_LIFTING;
    public static boolean MINIMIZE;
    public static boolean LIFT_BEFORE_SIMPLIFY;
    public static boolean LIFT_WHEN_MINIMIZING;
    public static boolean SAVE_FFT;

    // KULIBRAT PROPERTIES
    public static int BWIDTH;
    public static int BHEIGHT;
    public static boolean USE_DB;
    public static String DB_PATH;

    // TIC TAC TOE PROPERTIES
    public static int TIC_TAC_TOE_RULES;

    // SIM
    public static boolean SIM_SIMPLE_RULES;

    // MENS MORRIS
    public static boolean THREE_MENS;

    // GGP-FFT PROPERTIES
    public static boolean DETAILED_DEBUG;
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
                RULE_ORDERING = RULE_ORDERING_TERMINAL_FIRST;
        }

        USE_RULE_ORDERING = Boolean.parseBoolean(global.getProperty("use_rule_ordering"));
        MINIMIZE = Boolean.parseBoolean(global.getProperty("minimize"));
        USE_LIFTING = Boolean.parseBoolean(global.getProperty("use_lifting"));
        LIFT_BEFORE_SIMPLIFY = Boolean.parseBoolean(global.getProperty("lift_before_simplify"));
        LIFT_WHEN_MINIMIZING = Boolean.parseBoolean(global.getProperty("lift_when_minimizing"));
        GREEDY_AUTOGEN = Boolean.parseBoolean(global.getProperty("greedy_autogen"));
        RANDOM_SEED = Boolean.parseBoolean(global.getProperty("random_seed"));
        SEED = Integer.parseInt(global.getProperty("seed"));
        ENABLE_GGP = Boolean.parseBoolean(global.getProperty("enable_ggp"));
        ENABLE_GGP_PARSER = Boolean.parseBoolean(global.getProperty("enable_ggp_parser"));
        SHOW_GUI = Boolean.parseBoolean(global.getProperty("show_gui"));
        MINIMIZE_PRECONDITIONS = Boolean.parseBoolean(global.getProperty("minimize_preconditions"));
        MINIMIZE_RULE_BY_RULE = Boolean.parseBoolean(global.getProperty("minimize_rule_by_rule"));
        REMOVE_DEAD_RULES = Boolean.parseBoolean(global.getProperty("remove_dead_rules"));
        SIMPLIFY_ITERATIVELY = Boolean.parseBoolean(global.getProperty("simplify_iteratively"));
        SYMMETRY_DETECTION = Boolean.parseBoolean(global.getProperty("symmetry_detection"));
        NAIVE_RULE_GENERATION = Boolean.parseBoolean(global.getProperty("naive_generation"));
        SHOW_RULE_GROUPS = Boolean.parseBoolean(global.getProperty("show_rule_groups"));
        // DEBUG AND TEST
        DETAILED_DEBUG = Boolean.parseBoolean(global.getProperty("detailedDebug"));
        BENCHMARK_MODE = Boolean.parseBoolean(global.getProperty("benchmark_mode"));
        BENCHMARK_NUMBER = Integer.parseInt(global.getProperty("no_of_benchmarks"));
        USE_DEBUG_FILE = Boolean.parseBoolean(global.getProperty("use_debug_file"));
        DEBUG_FILENAME = global.getProperty("debug_filename");
        SAVE_FFT = Boolean.parseBoolean(global.getProperty("save_fft"));
        SINGLE_THREAD = Boolean.parseBoolean(global.getProperty("single_thread"));
        USE_BITSTRING_SORT_OPT = Boolean.parseBoolean(global.getProperty("use_bitstring_sorting_opt"));
        USE_INVERTED_LIST_NODES_OPT = Boolean.parseBoolean(global.getProperty("use_inverted_list_nodes_opt"));
        USE_INVERTED_LIST_RULES_OPT = Boolean.parseBoolean(global.getProperty("use_inverted_list_rules_opt"));
        USE_APPLY_OPT = Boolean.parseBoolean(global.getProperty("use_apply_opt"));
        USE_MINIMIZE_OPT = Boolean.parseBoolean(global.getProperty("use_minimize_opt"));

        // KULIBRAT
        BWIDTH = Integer.parseInt(kulibrat.getProperty("boardWidth"));
        BHEIGHT = Integer.parseInt(kulibrat.getProperty("boardHeight"));
        USE_DB = Boolean.parseBoolean(kulibrat.getProperty("use_db"));
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

        // SIM
        SIM_SIMPLE_RULES = Boolean.parseBoolean(sim.getProperty("simple_rules"));

        // MENS MORRIS
        THREE_MENS = Boolean.parseBoolean(morris.getProperty("three_mens"));

        // GGP
        GGP_GAME = ggp.getProperty("ggp_game");

        // Tweaking configurations (certain configs can't overlap)
        // todo - make inverted list opt work with symmetric states (and lifting)
        if (SYMMETRY_DETECTION || USE_LIFTING) {
            USE_BITSTRING_SORT_OPT = false;
            USE_INVERTED_LIST_NODES_OPT = false;
        }

        if (USE_INVERTED_LIST_RULES_OPT)
            USE_APPLY_OPT = false;

        if (!USE_LIFTING) {
            LIFT_WHEN_MINIMIZING = false;
            LIFT_BEFORE_SIMPLIFY = false;
        }

        if (!MINIMIZE) {
            MINIMIZE_PRECONDITIONS = false;
            MINIMIZE_RULE_BY_RULE = false;
        }

        // todo - disable/enable some stuff based on whether we have naive rule generation or not
    }

    private static void printSettings() {
        System.out.println("PRINTING PROPERTIES BELOW:");
        System.out.println("--------------------------");
        String perspectiveStr = (AUTOGEN_TEAM == PLAYER1) ? "Player 1" :
                (AUTOGEN_TEAM == PLAYER2) ? "Player 2" : "Both";
        String ruleOrderingStr = (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_FIRST) ? "Fewest preconditions first" :
                        (RULE_ORDERING == RULE_ORDERING_FEWEST_PRECONS_LAST) ? "Fewest preconditions last" :
                                (RULE_ORDERING == RULE_ORDERING_TERMINAL_FIRST) ? "Close to terminal first" :
                                        "Close to terminal last";

        System.out.printf("%-40.50s %-40.50s\n", "Autogen perspective:", perspectiveStr);
        System.out.printf("%-40.50s %-40.50s\n", "Symmetry detection:", SYMMETRY_DETECTION);
        System.out.printf("%-40.50s %-40.50s\n", "Naive rule generation:", NAIVE_RULE_GENERATION);
        System.out.printf("%-40.50s %-40.50s\n", "Single thread:", SINGLE_THREAD);
        System.out.printf("%-40.50s %-40.50s\n", "Greedy Autogeneration:", GREEDY_AUTOGEN);
        System.out.printf("%-40.50s %-40.50s\n", "Simplify iteratively:", SIMPLIFY_ITERATIVELY);
        System.out.printf("%-40.50s %-40.50s\n", "Minimize:", MINIMIZE);
        if (MINIMIZE) {
            System.out.printf("%-40.50s %-40.50s\n", "Minimize rule by rule:", MINIMIZE_RULE_BY_RULE);
            System.out.printf("%-40.50s %-40.50s\n", "Minimize preconditions:", MINIMIZE_PRECONDITIONS);
            System.out.printf("%-40.50s %-40.50s\n", "Remove dead rules:", REMOVE_DEAD_RULES);
            System.out.printf("%-40.50s %-40.50s\n", "Use minimize optimization:", USE_MINIMIZE_OPT);

        }
        System.out.printf("%-40.50s %-40.50s\n", "Use rule ordering:", USE_RULE_ORDERING);
        if (USE_RULE_ORDERING) {
            System.out.printf("%-40.50s %-40.50s\n", "Rule ordering:", ruleOrderingStr);
        }
        System.out.printf("%-40.50s %-40.50s\n", "Use lifting:", USE_LIFTING);
        if (USE_LIFTING) {
            System.out.printf("%-40.50s %-40.50s\n", "Lift before simplify:", LIFT_BEFORE_SIMPLIFY);
            System.out.printf("%-40.50s %-40.50s\n", "Lift when minimizing:", LIFT_WHEN_MINIMIZING);

        }
        System.out.printf("%-40.50s %-40.50s\n", "Use apply optimization:", USE_APPLY_OPT);
        if (!SYMMETRY_DETECTION && !USE_LIFTING) {
            System.out.printf("%-40.50s %-40.50s\n", "Use bitstring sorting optimization:", USE_BITSTRING_SORT_OPT);
            System.out.printf("%-40.50s %-40.50s\n", "Use inverted list optimization for nodes:", USE_INVERTED_LIST_NODES_OPT);
        }

        System.out.printf("%-40.50s %-40.50s\n", "Detailed debug messages:", DETAILED_DEBUG);
        System.out.printf("%-40.50s %-40.50s\n", "Using debug file:", USE_DEBUG_FILE);
        System.out.printf("%-40.50s %-40.50s\n", "Random seed:", RANDOM_SEED);
        System.out.printf("%-40.50s %-40.50s\n", "Seed value:",SEED);
        System.out.printf("%-40.50s %-40.50s\n", "Save fft to disk:", SAVE_FFT);
        System.out.printf("%-40.50s %-40.50s\n", "Benchmark mode:", BENCHMARK_MODE);
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
