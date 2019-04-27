package misc;

public class Config {
    // TEAMS
    public static final int PLAYER_NONE = 0;
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    public static final int PLAYER_ANY = 3;

    // PLAYER INSTANCES
    public static final int HUMAN = 1;
    public static final int MINIMAX = 2;
    public static final int MONTE_CARLO = 3;
    public static final int LOOKUP_TABLE = 4;
    public static final int FFT = 5;

    // GAME MODES
    public static final int HUMAN_VS_HUMAN = 1;
    public static final int HUMAN_VS_AI = 2;
    public static final int AI_VS_AI = 3;

    // WINDOW DIMENSIONS
    public static final int WIDTH = 1250;
    public static final int HEIGHT = 700;

    // MISC STATIC VARIABLES
    public static final int CLICK_INTERACTIVE = 0;
    public static final int CLICK_DISABLED = 1;
    public static final int CLICK_DEFAULT = 2;

    // PREFERENCES / CUSTOMIZATION
    // GAMES
    public static final int KULIBRAT = 0;
    public static final int TICTACTOE = 1;
    public static final boolean simpleTicTacToe = false;
    public static int CURRENT_GAME;

    // BOARD CONFIG
    public static final int kuliBWidth = 3;
    public static final int kuliBHeight = 4;


    // TODO - gui for this configuration
    public static boolean USE_AUTOGEN = true;

    public static final boolean RANDOM_ZOBRIST_SEED = true;
    public static final int ZOBRIST_SEED = 0; // 0 is required for database

    public static final int AUTOGEN_PERSPECTIVE = PLAYER1;
    public static final boolean FFT_OVERWRITE = true;

    // MISC
    public static final String DB_PATH = "jdbc:derby:KulibratDB;create=true";
    public static int SCORELIMIT; // Set by player once (kulibrat)
}
