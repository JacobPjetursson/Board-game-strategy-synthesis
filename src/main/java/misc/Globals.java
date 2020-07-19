package misc;

public class Globals {
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

    // GAMES
    public static final int TICTACTOE = 0;
    public static final int SIM = 1;

    // AUTOGEN RULE ORDERINGS
    public static final int RULE_ORDERING_TERMINAL_FIRST = 0;
    public static final int RULE_ORDERING_TERMINAL_LAST = 1;
    public static final int RULE_ORDERING_FEWEST_PRECONS_FIRST = 2;
    public static final int RULE_ORDERING_FEWEST_PRECONS_LAST = 3;

    // TIC TAC TOE RULE MODES
    public static final int TIC_TAC_TOE_NORMAL_RULES = 0;
    public static final int TIC_TAC_TOE_SIMPLE_RULES = 1;
    public static final int TIC_TAC_TOE_STUPID_RULES = 2;

    // MISC VARIABLES
    public static final int CLICK_INTERACTIVE = 0;
    public static final int CLICK_DISABLED = 1;
    public static final int CLICK_DEFAULT = 2;

    // Changed by program
    public static int CURRENT_GAME;
    public static int SCORELIMIT;
    public static boolean ENABLE_AUTOGEN = true;


}
