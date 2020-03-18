package misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    // GLOBAL PROPERTIES
    public static int AUTOGEN_PERSPECTIVE;
    public static boolean GREEDY_AUTOGEN;
    public static boolean RANDOM_SEED;
    public static int SEED;
    public static boolean ENABLE_GGP;
    public static boolean ENABLE_GGP_PARSER;
    public static boolean RANDOM_RULE_ORDERING;
    public static boolean MINIMIZE_PRECONDITIONS;
    public static boolean SYMMETRY_DETECTION;
    public static boolean SINGLE_THREAD;

    // KULIBRAT PROPERTIES
    public static int BWIDTH;
    public static int BHEIGHT;
    public static boolean USE_DB;
    public static String DB_PATH;

    // TIC TAC TOE PROPERTIES
    public static boolean TIC_TAC_TOE_SIMPLE_RULES;

    // SIM
    public static boolean SIM_SIMPLE_RULES;

    // GGP-FFT PROPERTIES
    public static boolean DETAILED_DEBUG;
    public static boolean FULL_RULES;
    public static boolean VERIFY_SINGLE_STRATEGY;
    public static String GGP_GAME;

    static {
        try {
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties() throws IOException {
        Properties global = new Properties();
        Properties tictactoe = new Properties();
        Properties kulibrat = new Properties();
        Properties sim = new Properties();
        Properties ggp = new Properties();

        global.load(new FileInputStream("propertiers/global.properties"));
        tictactoe.load(new FileInputStream("propertiers/tictactoe.properties"));
        kulibrat.load(new FileInputStream("propertiers/kulibrat.properties"));
        sim.load(new FileInputStream("propertiers/sim.properties"));
        ggp.load(new FileInputStream("propertiers/ggp.properties"));

        // GLOBAL CONFIG
        String perspective = global.getProperty("autogen_perspective", "player1");
        switch(perspective) {
            case "player2":
                AUTOGEN_PERSPECTIVE = Globals.PLAYER2;
                break;
            case "both":
                AUTOGEN_PERSPECTIVE = Globals.PLAYER_ANY;
                break;
            default:
                AUTOGEN_PERSPECTIVE = Globals.PLAYER1;
        }

        GREEDY_AUTOGEN = Boolean.parseBoolean(global.getProperty("greedy_autogen"));
        RANDOM_SEED = Boolean.parseBoolean(global.getProperty("random_seed"));
        SEED = Integer.parseInt(global.getProperty("seed"));
        ENABLE_GGP = Boolean.parseBoolean(global.getProperty("enable_ggp"));
        ENABLE_GGP_PARSER = Boolean.parseBoolean(global.getProperty("enable_ggp_parser"));
        RANDOM_RULE_ORDERING = Boolean.parseBoolean(global.getProperty("random_rule_ordering"));
        MINIMIZE_PRECONDITIONS = Boolean.parseBoolean(global.getProperty("minimize_preconditions"));
        SYMMETRY_DETECTION = Boolean.parseBoolean(global.getProperty("symmetry_detection"));
        // DEBUG AND TEST
        DETAILED_DEBUG = Boolean.parseBoolean(global.getProperty("detailedDebug"));
        FULL_RULES = Boolean.parseBoolean(global.getProperty("fullRules"));
        VERIFY_SINGLE_STRATEGY = Boolean.parseBoolean(global.getProperty("verify_single_strategy"));
        SINGLE_THREAD = Boolean.parseBoolean(global.getProperty("single_thread"));

        // KULIBRAT
        BWIDTH = Integer.parseInt(kulibrat.getProperty("boardWidth"));
        BHEIGHT = Integer.parseInt(kulibrat.getProperty("boardHeight"));
        USE_DB = Boolean.parseBoolean(kulibrat.getProperty("use_db"));
        DB_PATH = kulibrat.getProperty("db_path");

        // TIC TAC TOE
        TIC_TAC_TOE_SIMPLE_RULES = Boolean.parseBoolean(tictactoe.getProperty("simple_rules"));

        // SIM
        SIM_SIMPLE_RULES = Boolean.parseBoolean(sim.getProperty("simple_rules"));

        // GGP
        GGP_GAME = ggp.getProperty("ggp_game");
    }
}
