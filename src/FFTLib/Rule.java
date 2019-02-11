package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import misc.Config;

import java.util.*;


public class Rule {
    private static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "&&", "∧", ","));
    public ArrayList<Clause> symmetryClauses;
    public Clause clause;
    public Action action;
    public String actionStr, clauseStr;


    // If multirule, the rule class instead contains a list of rules
    public boolean multiRule;
    public HashSet<Rule> rules;
    public boolean errors;

    // parsing constructor
    public Rule(String clauseStr, String actionStr) {
        this.multiRule = isMultiRule(clauseStr, actionStr);
        if (multiRule) {
            rules = new HashSet<>();
            replaceWildcards(clauseStr, actionStr, rules);
            // TODO - format this shit
            this.clauseStr = clauseStr;
            this.actionStr = actionStr;
        } else {
            this.action = getAction(actionStr);
            this.clause = getClause(clauseStr);
            this.symmetryClauses = getSymmetryClauses();
        }
        errors = !isValidRuleFormat(this);
    }

    // Empty constructor to allow rule buildup
    public Rule() {
        this.clause = new Clause();
        this.action = new Action();
        clauseStr = "";
        actionStr = "";
    }

    public void addLiteral(Literal l) {
        if (!clause.literals.contains(l))
            clause.add(l);
        this.symmetryClauses = getSymmetryClauses();
    }

    public void removeLiteral(Literal l) {
        this.clause.remove(l);
        this.symmetryClauses = getSymmetryClauses();
    }

    public void removeLiterals(int row, int col) {
        ArrayList<Literal> removeList = new ArrayList<>();
        for (Literal l : clause.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        clause.literals.removeAll(removeList);
        this.symmetryClauses = getSymmetryClauses();
    }

    public void setAction(Action a) {
        this.action = a;
    }

    public void setClause(Clause c) {
        this.clause = c;
        this.symmetryClauses = getSymmetryClauses();
    }

    private static ArrayList<String> prepClause(String clauseStr) {
        ArrayList<String> clause = new ArrayList<>();
        String[] parts = clauseStr.split(" ");
        for (String part : parts) {
            if (separators.contains(part)) {
                continue;
            }
            for (String sep : separators) {
                if (part.contains(sep)) {
                    part = part.replace(sep, "");
                }
            }
            if (part.length() < 2)
                continue;
            // Both E and P
            if ((part.charAt(0) == '!' && Character.isDigit(part.charAt(1)))) {
                String newpart = "!E_" + part.substring(1);
                clause.add(newpart);
                newpart = "!P_" + part.substring(1);
                clause.add(newpart);
            } else if (Character.isDigit(part.charAt(0))) {
                clause.add("E_" + part);
                clause.add("P_" + part);
            } else
                clause.add(part);
        }
        return clause;
    }

    private static ArrayList<String> prepAction(String actionStr) {
        String[] parts = actionStr.split(" ");
        ArrayList<String> corrected_parts = new ArrayList<>();
        for (String part : parts) {
            if (separators.contains(part))
                continue;
            for (String sep : separators) {
                if (part.contains(sep))
                    part = part.replace(sep, "");
            }
            corrected_parts.add(part);

        }

        return corrected_parts;
    }

    private static Clause getClause(String clauseStr) {
        ArrayList<Literal> literals = new ArrayList<>();
        for (String c : prepClause(clauseStr)) {
            Literal l = new Literal(c);
            literals.add(new Literal(c));
        }
       return new Clause(literals);
    }

    private static Action getAction(String actionStr) {
        return new Action(prepAction(actionStr));
    }

    public static boolean isValidRuleFormat(Rule r) {
        if (r.multiRule) {
            for (Rule rule : r.rules)
                if (!isValidRuleFormat(rule))
                    return false;
            return true;
        }
        if (r.clause.isEmpty())
            return false;
        for (Literal l : r.clause.literals)
            if (l.error)
                return false;
        if (r.action.actionErr)
            return false;
        for (Literal l : r.action.addClause.literals)
            if (l.error)
                return false;
        for (Literal l : r.action.remClause.literals)
            if (l.error)
                return false;

        return true;
    }

    private static boolean isMultiRule(String clauseStr, String actionStr) {
        ArrayList<String> prepLiteralStr = prepClause(clauseStr);
        ArrayList<String> prepActionStr = prepAction(actionStr);
        for (String lStr : prepLiteralStr) {
            String[] pos = lStr.split("_");
            if (pos.length < 2 || pos.length > 3)
                return false;
            // If P or E is prefixed, index is different
            int idx = pos.length == 3 ? 1 : 0;
            if (!Character.isDigit(pos[idx].charAt(0))) {
                return true;
            } if (!Character.isDigit(pos[idx + 1].charAt(0))) {
                return true;
            }
        }
        for (String aStr : prepActionStr) {
            String[] pos = aStr.split("_");
            if (pos.length < 2 || pos.length > 3)
                return false;
            // Char after + or -
            if (!Character.isDigit(pos[0].charAt(1))) {
                return true;
            } if (!Character.isDigit(pos[1].charAt(0))) {
                return true;
            }
        }
        return false;
    }

    private static void replaceWildcards(String clauseStr, String actionStr, HashSet<Rule> rules) {
        ArrayList<String> prepLiteralStr = prepClause(clauseStr);
        ArrayList<String> prepActionStr = prepAction(actionStr);
        HashSet<String> wildCardsRow = new HashSet<>();
        HashSet<String> wildCardsCol = new HashSet<>();
        for (String lStr : prepLiteralStr) {
            String[] pos = lStr.split("_");
            // If P or E is prefixed, index is different
            int idx = pos.length == 3 ? 1 : 0;
            if (!Character.isDigit(pos[idx].charAt(0))) {
                wildCardsRow.add(pos[idx]);
            } if (!Character.isDigit(pos[idx + 1].charAt(0))) {
                wildCardsCol.add(pos[idx + 1]);
            }
        }
        for (String aStr : prepActionStr) {
            String[] pos = aStr.split("_");
            // Char after + or -
            if (!Character.isDigit(pos[0].charAt(1))) {
                wildCardsRow.add(pos[0].substring(1));
            } if (!Character.isDigit(pos[1].charAt(0))) {
                wildCardsCol.add(pos[1]);
            }
        }
        if (wildCardsCol.isEmpty() && wildCardsRow.isEmpty()) {
            rules.add(new Rule(clauseStr, actionStr));
            return;
        }

        for (String wc : wildCardsRow) {
            for (int i = 0; i < FFTManager.gameBoardHeight; i++) {
                String finalCStr = clauseStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }

        }

        for (String wc : wildCardsCol) {
            for (int i = 0; i < FFTManager.gameBoardWidth; i++) {
                String finalCStr = clauseStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }

        }
    }

    public String printRule() {
        String cStr, aStr;
        if (clause != null && action != null) {
            cStr = clause.getFormattedString();
            aStr = action.getFormattedString();
        } else {
            cStr = clauseStr;
            aStr = actionStr;
        }
        return "IF (" + cStr + ") THEN (" + aStr + ")";
    }

    public ArrayList<Clause> getSymmetryClauses() {
        ArrayList<Clause> symmetryClauses = new ArrayList<>();
        // TODO - make this for all available symmetries
        for (int symmetry : FFTManager.gameSymmetries) {
            if (symmetry == Config.SYM_NONE)
                symmetryClauses.add(new Clause(Config.SYM_NONE, clause));
            else if (symmetry == Config.SYM_HREF)
                symmetryClauses.add(new Clause(Config.SYM_HREF, reflectH(clause)));
        }
        return symmetryClauses;
    }


    public FFTMove apply(FFTState state) {
        if (multiRule) {
            // If first rule applies, it doesn't matter if rest is incorrect, since it's a prioritized list
            for (Rule rule : rules) {
                FFTMove move = rule.apply(state);
                if (move != null)
                    return move;
            }
            return null;
        }
        HashSet<Literal> stLiterals = state.getLiterals();

        for (int symmetry : FFTManager.gameSymmetries) {
            for (Clause clause : symmetryClauses) {
                if (clause.symmetry != symmetry)
                    continue;

                boolean match = true;
                for (Literal l : clause.literals) {
                    if (l.negation) {
                        Literal temp = new Literal(l);
                        temp.name = temp.name.replace("!", "");
                        if (stLiterals.contains(temp)) {
                            match = false;
                            break;
                        }
                    } else if (!stLiterals.contains(l)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    FFTMove move = action.applySymmetry(symmetry).getMove();
                    move.setTeam(state.getTurn());
                    if (FFTManager.logic.isLegalMove(state, move)) {
                        return move;
                    }
                }
            }
        }
        return null;
    }

    private int[][] makeClauseBoard(Clause clause) {
        int[][] clauseBoard = new int[FFTManager.gameBoardHeight][FFTManager.gameBoardWidth];
        // These literals will be reflected/rotated
        ArrayList<Literal> changeLiterals = new ArrayList<>();

        for (Literal l : clause.literals) {
            if (l.boardPlacement && l.row != -1) {
                changeLiterals.add(l);
                if (l.negation)
                    clauseBoard[l.row][l.col] = -l.pieceOcc;
                else
                    clauseBoard[l.row][l.col] = l.pieceOcc;
            }
        }
        clause.literals.removeAll(changeLiterals);
        return clauseBoard;
    }

    private void addClauseBoardToList(int[][] cb, Clause clause) {
        // Add back to list
        for (int i = 0; i < cb.length; i++) {
            for (int j = 0; j < cb[i].length; j++) {
                int val = cb[i][j];
                if (val < 0)
                    clause.add(new Literal(i, j, -val, true));
                else if (val > 0)
                    clause.add(new Literal(i, j, val, false));
            }
        }
    }

    private Clause reflectH(Clause clause) {
        Clause rotClause = new Clause(clause);
        int[][] cBoard = makeClauseBoard(rotClause);
        int[][] refH = new int[FFTManager.gameBoardHeight][FFTManager.gameBoardWidth];

        // Reflect
        for (int i = 0; i < cBoard.length; i++) {
            for (int j = 0; j < cBoard[i].length; j++) {
                refH[i][j] = cBoard[i][cBoard[i].length - 1 - j];
            }
        }

        addClauseBoardToList(refH, rotClause);
        return rotClause;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rule)) return false;

        Rule rule = (Rule) obj;
        if (this == rule)
            return true;
        if (this.multiRule != rule.multiRule)
            return false;
        if (this.multiRule)
            return this.rules.equals(rule.rules);
        return (this.symmetryClauses.equals(rule.symmetryClauses) &&
                        (this.action.equals(rule.action)));
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(this.symmetryClauses, this.action);
        return 31 * hash;
    }
}
