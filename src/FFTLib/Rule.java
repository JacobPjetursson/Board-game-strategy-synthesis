package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import misc.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;


public class Rule {
    public static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "&&", "∧", ","));
    public ArrayList<ClauseList> symmetryClauses;
    public ArrayList<Clause> clauses;
    public Action action;
    public String clauseStr;
    public String actionStr;

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
            this.clauseStr = clauseStr;
            this.actionStr = actionStr;
        } else {
            symmetryClauses = new ArrayList<>();
            this.action = getAction(actionStr);
            this.clauses = getClauses(clauseStr);
            this.clauseStr = getFormattedClauseStr();
            this.actionStr = getFormattedActionStr();
            // TODO - make this for all available symmetries
            for (int symmetry : Config.getSymmetries()) {
                if (symmetry == Config.SYM_NONE)
                    symmetryClauses.add(new ClauseList(Config.SYM_NONE, clauses));
                else if (symmetry == Config.SYM_HREF)
                    symmetryClauses.add(new ClauseList(Config.SYM_HREF, reflectH(clauses)));
            }
        }
        errors = !isValidRuleFormat(this);
    }

    private static ArrayList<String> prepClauses(String clauseStr) {
        ArrayList<String> clauseStrs = new ArrayList<>();
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
                clauseStrs.add(newpart);
                newpart = "!P_" + part.substring(1);
                clauseStrs.add(newpart);
            } else if (Character.isDigit(part.charAt(0))) {
                clauseStrs.add("E_" + part);
                clauseStrs.add("P_" + part);
            } else
                clauseStrs.add(part);
        }
        return clauseStrs;
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

    private static ArrayList<Clause> getClauses(String clauseStr) {
        ArrayList<Clause> clauses = new ArrayList<>();
        for (String c : prepClauses(clauseStr)) {
            clauses.add(new Clause(c));
        }

        return clauses;
    }

    private static Action getAction(String actionStr) {
        return new Action(prepAction(actionStr));
    }

    public boolean isValidRuleFormat(Rule r) {
        if (r.multiRule) {
            for (Rule rule : r.rules)
                if (!isValidRuleFormat(rule))
                    return false;
            return true;
        }
        if (r.clauses.isEmpty())
            return false;
        for (Clause c : r.clauses)
            if (c.clauseErr)
                return false;
        if (r.action.actionErr)
            return false;
        for (Clause c : r.action.addClauses)
            if (c.clauseErr)
                return false;
        for (Clause c : r.action.remClauses)
            if (c.clauseErr)
                return false;

        return true;
    }

    private static boolean isMultiRule(String clauseStr, String actionStr) {
        ArrayList<String> prepClauseStr = prepClauses(clauseStr);
        ArrayList<String> prepActionStr = prepAction(actionStr);
        for (String cStr : prepClauseStr) {
            String[] pos = cStr.split("_");
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
        ArrayList<String> prepClauseStr = prepClauses(clauseStr);
        ArrayList<String> prepActionStr = prepAction(actionStr);
        HashSet<String> wildCardsRow = new HashSet<>();
        HashSet<String> wildCardsCol = new HashSet<>();
        for (String cStr : prepClauseStr) {
            String[] pos = cStr.split("_");
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
            for (int i = 0; i < Config.getBoardHeight(); i++) {
                String finalCStr = clauseStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }

        }

        for (String wc : wildCardsCol) {
            for (int i = 0; i < Config.getBoardWidth(); i++) {
                String finalCStr = clauseStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }

        }
    }

    public String printRule() {
        return "IF (" + this.clauseStr + ") THEN (" + this.actionStr + ")";
    }

    private String getFormattedClauseStr() {
        String clauseMsg = "";
        for (Clause clause : clauses) {
            if (!clauseMsg.isEmpty())
                clauseMsg += " ∧ ";
            clauseMsg += clause.name;
        }
        return clauseMsg;
    }

    private String getFormattedActionStr() {
        String actionMsg = "";
        for (Clause clause : action.addClauses) {
            if (!actionMsg.isEmpty())
                actionMsg += " ∧ ";
            actionMsg += "+" + clause.name;
        }
        for (Clause clause : action.remClauses) {
            if (!actionMsg.isEmpty())
                actionMsg += " ∧ ";
            actionMsg += "-" + clause.name;
        }
        return actionMsg;
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
        HashSet<Clause> stClauses = state.getClauses();
        for (int symmetry : Config.getSymmetries()) {
            for (ClauseList clauseList : symmetryClauses) {
                if (clauseList.symmetry != symmetry)
                    continue;

                boolean match = true;
                for (Clause c : clauseList.clauses) {
                    if (c.negation) {
                        Clause temp = new Clause(c);
                        temp.name = temp.name.replace("!", "");
                        if (stClauses.contains(temp)) {
                            match = false;
                            break;
                        }
                    } else if (!stClauses.contains(c)) {
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

    private int[][] makeClauseBoard(ArrayList<Clause> clauses) {
        int[][] clauseBoard = new int[Config.getBoardHeight()][Config.getBoardWidth()];
        // These clauses will be reflected/rotated
        ArrayList<Clause> changeClauses = new ArrayList<>();

        for (Clause c : clauses) {
            if (c.boardPlacement && c.row != -1) {
                changeClauses.add(c);
                if (c.negation)
                    clauseBoard[c.row][c.col] = -c.pieceOcc;
                else
                    clauseBoard[c.row][c.col] = c.pieceOcc;
            }
        }
        clauses.removeAll(changeClauses);
        return clauseBoard;
    }

    private void addClauseBoardToList(int[][] cb, ArrayList<Clause> clauses) {
        // Add back to list
        for (int i = 0; i < cb.length; i++) {
            for (int j = 0; j < cb[i].length; j++) {
                int val = cb[i][j];
                if (val < 0)
                    clauses.add(new Clause(i, j, -val, true));
                else if (val > 0)
                    clauses.add(new Clause(i, j, val, false));
            }
        }
    }

    private ArrayList<Clause> reflectH(ArrayList<Clause> clauses) {
        ArrayList<Clause> rClauses = new ArrayList<>(clauses);
        int[][] cBoard = makeClauseBoard(rClauses);
        int[][] refH = new int[Config.getBoardHeight()][Config.getBoardWidth()];

        // Reflect
        for (int i = 0; i < cBoard.length; i++) {
            for (int j = 0; j < cBoard[i].length; j++) {
                refH[i][j] = cBoard[i][cBoard[i].length - 1 - j];
            }
        }

        addClauseBoardToList(refH, rClauses);
        return rClauses;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rule)) return false;

        Rule rule = (Rule) obj;
        return this == rule ||
                (this.symmetryClauses.equals(rule.symmetryClauses) &&
                        (this.action.equals(rule.action)));
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(this.symmetryClauses, this.action);
        return 31 * hash;
    }


    protected class ClauseList {
        ArrayList<Clause> clauses;
        int symmetry;

        public ClauseList(int symmetry, ArrayList<Clause> clauses) {
            this.clauses = clauses;
            this.symmetry = symmetry;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ClauseList)) return false;

            ClauseList list = (ClauseList) obj;
            return this == list ||
                    (this.clauses.equals(list.clauses));
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hashCode(this.clauses);
        }
    }

}
