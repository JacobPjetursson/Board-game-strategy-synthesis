package FFT;

import misc.Config;

import java.util.Objects;

public class Clause {
    public static final int PIECEOCC_NONE = -1;
    public static final int PIECEOCC_PLAYER = 1;
    public static final int PIECEOCC_ENEMY = 2;

    String name;
    boolean boardPlacement;
    public int row = -1; public int col = -1;
    int pieceOcc = PIECEOCC_NONE;
    boolean negation;
    boolean clauseErr;


    public Clause(int row, int col, int pieceOcc, boolean negation) {
        this.row = row;
        this.col = col;
        this.pieceOcc = pieceOcc;
        this.boardPlacement = true;
        this.negation = negation;
        formatClause();
    }

    public Clause(String name) {
        this.name = name;
        if (name.startsWith("!")) {
            this.negation = true;
            name = name.substring(1);
        }
        this.boardPlacement = isBoardPlacement();
        if (this.boardPlacement) {
            if (name.startsWith("P") || name.startsWith("p"))
                this.pieceOcc = PIECEOCC_PLAYER;
            else if (name.startsWith("E") || name.startsWith("e"))
                this.pieceOcc = PIECEOCC_ENEMY;
            else if (!Character.isDigit(name.charAt(0))) {
                System.err.println("Board position clause must be specified with either:\n" +
                        "P (Player), E (Enemy), or '' (Irrelevant), followed by a row and column specification");
                clauseErr = true;
                return;
            }
            // Parsing
            String info = (name.replaceAll("[\\D]", ""));
            if (info.length() < 2) {
                System.err.println("Failed to specify row and/or column for this clause");
                clauseErr = true;
                return;
            }
            this.row = Integer.parseInt(info.substring(0, 1));
            this.col = Integer.parseInt(info.substring(1, 2));
            if (row >= Config.bHeight || col >= Config.bWidth) {
                System.err.println("row and/or column numbers are out of bounds w.r.t. the board size");
                clauseErr = true;
                return;
            }
            // Ensure same format
            formatClause();
        }
    }

    protected Clause(Clause duplicate) {
        this.name = duplicate.name;
        this.boardPlacement = duplicate.boardPlacement;
        this.row = duplicate.row;
        this.col = duplicate.col;
        this.pieceOcc = duplicate.pieceOcc;
        this.negation = duplicate.negation;
    }

    private boolean isBoardPlacement() {
        String nameCopy = name;
        if (nameCopy.startsWith("!"))
            nameCopy = nameCopy.substring(1);
        if(nameCopy.startsWith("E") || nameCopy.startsWith("e") ||
                nameCopy.startsWith("P") || nameCopy.startsWith("p"))
            nameCopy = nameCopy.substring(1);

        return Character.isDigit(nameCopy.charAt(0)) || Character.isDigit(nameCopy.charAt(1));
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
        if (!(obj instanceof Clause)) return false;

        Clause clause = (Clause) obj;
        return this == clause ||
                (this.name.equals(clause.name));
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(name);
    }
}