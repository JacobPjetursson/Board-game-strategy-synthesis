package misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    // GLOBAL PROPERTIES
    public static boolean ENABLE_AUTOGEN;
    public static int AUTOGEN_PERSPECTIVE;
    public static boolean GREEDY_AUTOGEN;
    public static boolean RANDOM_SEED;
    public static int SEED;
    public static boolean ENABLE_GGP;
    public static boolean ENABLE_GGP_PARSER;
    public static boolean RANDOM_RULE_ORDERING;
    public static boolean MINIMIZE_PRECONDITIONS;

    // KULIBRAT PROPERTIES
    public static int BWIDTH;
    public static int BHEIGHT;
    public static String DB_PATH;

    // TIC TAC TOE PROPERTIES
    public static boolean SIMPLE_RULES;

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
        Properties ggp = new Properties();

        global.load(new FileInputStream("global.properties"));
        tictactoe.load(new FileInputStream("tictactoe.properties"));
        kulibrat.load(new FileInputStream("kulibrat.properties"));
        ggp.load(new FileInputStream("ggp.properties"));

        // GLOBAL CONFIG
        String perspective = global.getProperty("autogen_perspective", "player1");
        switch(perspective) {
            case "player2":
                AUTOGEN_PERSPECTIVE = Globals.PLAYER2;
            case "both":
                AUTOGEN_PERSPECTIVE = Globals.PLAYER_ANY;
            default:
                AUTOGEN_PERSPECTIVE = Globals.PLAYER1;
        }

        ENABLE_AUTOGEN = Boolean.getBoolean(global.getProperty("enable_autogen"));
        GREEDY_AUTOGEN = Boolean.getBoolean(global.getProperty("greedy_autogen"));
        RANDOM_SEED = Boolean.getBoolean(global.getProperty("random_seed"));
        SEED = Integer.parseInt(global.getProperty("seed"));
        ENABLE_GGP = Boolean.getBoolean(global.getProperty("enable_ggp"));
        ENABLE_GGP_PARSER = Boolean.getBoolean(global.getProperty("enable_ggp_parser"));
        RANDOM_RULE_ORDERING = Boolean.getBoolean(global.getProperty("random_rule_ordering"));
        MINIMIZE_PRECONDITIONS = Boolean.getBoolean(global.getProperty("minimize_preconditions"));
        // DEBUG AND TEST
        DETAILED_DEBUG = Boolean.getBoolean(global.getProperty("detailedDebug"));
        FULL_RULES = Boolean.getBoolean(global.getProperty("fullRules"));
        VERIFY_SINGLE_STRATEGY = Boolean.getBoolean(global.getProperty("verify_single_strategy"));

        // KULIBRAT
        BWIDTH = Integer.parseInt(kulibrat.getProperty("boardWidth"));
        BHEIGHT = Integer.parseInt(kulibrat.getProperty("boardHeight"));
        DB_PATH = kulibrat.getProperty("db_path");

        // TIC TAC TOE
        SIMPLE_RULES = Boolean.getBoolean(tictactoe.getProperty("simple_rules"));

        // GGP
        GGP_GAME = ggp.getProperty("ggp_game");
    }
}
