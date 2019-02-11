package fftlib;

import java.util.Objects;

public class Literal {
    static final int PIECEOCC_NONE = -1;
    public static final int PIECEOCC_ANY = 0;
    private static final int PIECEOCC_PLAYER = 1;
    private static final int PIECEOCC_ENEMY = 2;
    public int row = -1;
    public int col = -1;
    public String name;
    public boolean boardPlacement;
    public int pieceOcc = PIECEOCC_NONE;
    boolean negation;
    boolean error;


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
        this.boardPlacement = isBoardPlacement();
        if (this.boardPlacement) {
            if (name.startsWith("P") || name.startsWith("p")) {
                this.pieceOcc = PIECEOCC_PLAYER;
            }
            else if (name.startsWith("E") || name.startsWith("e")) {
                this.pieceOcc = PIECEOCC_ENEMY;
            }
            else if (!Character.isDigit(name.charAt(0))) {
                System.err.println("Board position clause must be specified with either:\n" +
                        "P (Player), E (Enemy), or '', followed by a row and column specification");
                error = true;
                return;
            }
            // Parsing
            String[] pos = name.split("_");
            if (pos.length < 2) {
                System.err.println("Failed to specify row and/or column for this clause");
                error = true;
                return;
            }
            if (this.pieceOcc == PIECEOCC_NONE) {
                this.row = Integer.parseInt(pos[0]);
                this.col = Integer.parseInt(pos[1]);
            } else {
                this.row = Integer.parseInt(pos[1]);
                this.col = Integer.parseInt(pos[2]);
            }
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

    private boolean isBoardPlacement() {
        String nameCopy = name;
        if (nameCopy.startsWith("!"))
            nameCopy = nameCopy.substring(1);
        if (nameCopy.startsWith("E") || nameCopy.startsWith("e") ||
                nameCopy.startsWith("P") || nameCopy.startsWith("p")) {

            nameCopy = nameCopy.substring(1);
            if (nameCopy.charAt(0) != '_')
                return false;
            nameCopy = nameCopy.substring(1);
        }
        return Character.isDigit(nameCopy.charAt(0));
    }

    private void formatClause() {
        // Check for + and - in case of action
        this.name = "";
        if (negation)
            this.name += "!";

        String teamStr = (pieceOcc == PIECEOCC_PLAYER) ? "P_" :
                (pieceOcc == PIECEOCC_ENEMY) ? "E_" : "";
        this.name += String.format("%s%d_%d", teamStr, row, col);
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