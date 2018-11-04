package misc;

public class Config {
    // TEAMS
    public static final int RED = 1;
    public static final int BLACK = 2;

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
    public static final int WIDTH = 800;
    public static final int HEIGHT = 650;


    // PREFERENCES / CUSTOMIZATION

    // BOARD CONFIG AND RULES FOR KULIBRAT
    public static final int bWidth = 3;
    public static final int bHeight = 3;
    public static int SCORELIMIT = 0;

    // LEVEL OF SYMMETRY
    public static final int SYM_NONE = 0;
    public static final int SYM_HREF = 1;
    public static final int SYM_VREF = 2;
    public static final int SYM_HVREF = 3;
    public static final int SYM_ROT = 4;
    public static final int SYM_HREF_ROT = 5;
    public static final int SYM_VREF_ROT = 6;
    public static final int SYM_HVREF_ROT = 7;

    // MISC
    public static final int[] SYMMETRY = {SYM_NONE, SYM_HREF};
    public static final boolean CUSTOMIZABLE = false; // For debug
    public static final String DB_PATH = "jdbc:derby:altDB;create=true";
    public static final String FFT_PATH = "fft.txt";
}
