package fftlib;

import java.util.Objects;

public class Literal {
    static final int PIECEOCC_NONE = -1;
    static final int PIECEOCC_PLAYER = 1;
    private static final int PIECEOCC_ENEMY = 2;
    public static final int PIECEOCC_ANY = 3;
    public int row = -1;
    public int col = -1;
    public String name;
    public boolean boardPlacement;
    public int pieceOcc = PIECEOCC_NONE;
    boolean negation;
    boolean error;

    private static String errorMsg = "Board position clause must be specified with either:\n" +
            "P(x, y), E(x, y) or PE(x, y) where P = Player, E = Enemy, and PE indicates the team is irrelevant.\n" +
            "'x' and 'y' can either be constant board positions or variables";


    public Literal(int row, int col, int pieceOcc, boolean negation) {
        this.row = row;
        this.col = col;
        this.pieceOcc = pieceOcc;
        this.boardPlacement = true;
        this.negation = negation;
        formatClause();
    }

    public Literal(String name) {
        this.name = name;
        if (name.startsWith("!")) {
            this.negation = true;
            name = name.substring(1);
        }
        this.boardPlacement = isBoardPlacement(this.name);
        if (this.boardPlacement) {
            if (name.toLowerCase().startsWith("pe"))
                this.pieceOcc = PIECEOCC_ANY;
            else if (name.toLowerCase().startsWith("p"))
                this.pieceOcc = PIECEOCC_PLAYER;
            else if (name.toLowerCase().startsWith("e"))
                this.pieceOcc = PIECEOCC_ENEMY;

            else {
                System.err.println(errorMsg);
                error = true;
                return;
            }
            String[] coords = getCoords(this.name);
            if (coords == null) {
                System.err.println(errorMsg);
                error = true;
                return;
            }

            this.row = Integer.parseInt(coords[0]);
            this.col = Integer.parseInt(coords[1]);

            if (row >= FFTManager.gameBoardHeight || col >= FFTManager.gameBoardWidth) {
                System.err.println("row and/or column numbers are out of bounds w.r.t. the board size");
                error = true;
                return;
            }
            // Ensure same format
            formatClause();
        }
    }

    protected Literal(Literal duplicate) {
        this.name = duplicate.name;
        this.boardPlacement = duplicate.boardPlacement;
        this.row = duplicate.row;
        this.col = duplicate.col;
        this.error = duplicate.error;
        this.pieceOcc = duplicate.pieceOcc;
        this.negation = duplicate.negation;
    }

    private static boolean isBoardPlacement(String name) {
        if (name.startsWith("!"))
            name = name.substring(1);
        if (name.toLowerCase().startsWith("e") ||
                name.toLowerCase().startsWith("p"))
            name = name.substring(1);
        else if (name.toLowerCase().startsWith("pe"))
            name = name.substring(2);
        else
            return false;

        if (name.charAt(0) != '(')
            return false;
        name = name.substring(1);

        return Character.isDigit(name.charAt(0));
    }

    static String[] getCoords(String name) {
        // Parsing
        String[] pos = name.split("[()]"); //
        if (pos.length < 2)
            return null;

        String[] coords = pos[1].split(",");
        if (coords.length < 2)
            return null;

        coords[0] = coords[0].trim();
        coords[1] = coords[1].trim();

        return coords;
    }

    private void formatClause() {
        // Check for + and - in case of action
        this.name = "";
        if (negation)
            this.name += "!";

        String teamStr = (pieceOcc == PIECEOCC_PLAYER) ? "P" :
                (pieceOcc == PIECEOCC_ENEMY) ? "E" : "PE";
        this.name += String.format("%s(%d, %d)", teamStr, row, col);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) return false;

        Literal literal = (Literal) obj;
        if (this == literal)
            return true;
        return this.name.toLowerCase().equals(literal.name.toLowerCase());
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(name);
    }
}