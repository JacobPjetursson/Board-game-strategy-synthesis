package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.game.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import static fftlib.Literal.PIECEOCC_ANY;
import static fftlib.Literal.PIECEOCC_ENEMY;
import static fftlib.Literal.PIECEOCC_PLAYER;


public class Rule {
    private static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "∧"));
    private ArrayList<Clause> transformedClauses;
    public Clause clause;
    public Action action;
    private String actionStr, clauseStr;


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
            this.transformedClauses = getTransformedClauses();
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
        this.transformedClauses = getTransformedClauses();
    }

    public void removeLiteral(Literal l) {
        this.clause.remove(l);
        this.transformedClauses = getTransformedClauses();
    }

    public void removeLiterals(int row, int col) {
        ArrayList<Literal> removeList = new ArrayList<>();
        for (Literal l : clause.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        clause.literals.removeAll(removeList);
        removeList.clear();
        for (Literal l : action.addClause.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        action.addClause.literals.removeAll(removeList);
        removeList.clear();
        for (Literal l : action.remClause.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        action.remClause.literals.removeAll(removeList);

        this.transformedClauses = getTransformedClauses();
    }

    public void setAction(Action a) {
        if (a == null)
            this.action = new Action();
        else
            this.action = a;
    }

    public void setClause(Clause c) {
        if (c == null)
            this.clause = new Clause();
        else
            this.clause = c;
        this.transformedClauses = getTransformedClauses();
    }

    private static ArrayList<String> prepClause(String clauseStr) {
        ArrayList<String> clause = new ArrayList<>();
        StringBuilder regex = new StringBuilder();
        regex.append(separators.get(0));
        for (int i = 1; i < separators.size(); i++)
            regex.append("|").append(separators.get(i));

        String[] parts = clauseStr.split(regex.toString());
        for (String part : parts) {
            if (part.length() < 2)
                continue;
            part = part.trim();
            clause.add(part);
        }
        return clause;
    }

    private static ArrayList<String> prepAction(String actionStr) {
        StringBuilder regex = new StringBuilder();
        regex.append(separators.get(0));
        for (int i = 1; i < separators.size(); i++)
            regex.append("|").append(separators.get(i));

        String[] parts = actionStr.split(regex.toString());
        ArrayList<String> corrected_parts = new ArrayList<>();
        for (String part : parts) {
            if (part.length() < 2)
                continue;
            part = part.trim();
            corrected_parts.add(part);

        }

        return corrected_parts;
    }

    private static Clause getClause(String clauseStr) {
        ArrayList<Literal> literals = new ArrayList<>();
        for (String c : prepClause(clauseStr)) {
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
        for (Literal l : r.clause.literals)
            if (l.error)
                return false;
        if (r.action.addClause.isEmpty() && r.action.remClause.isEmpty())
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
            String[] coords = Literal.getCoords(lStr);
            if (coords == null)
                return false;

            if (!Character.isDigit(coords[0].charAt(0)))
                return true;
            if (!Character.isDigit(coords[1].charAt(0)))
                return true;

        }
        for (String aStr : prepActionStr) {
            String[] coords = Literal.getCoords(aStr);
            if (coords == null)
                return false;

            if (!Character.isDigit(coords[0].charAt(0)))
                return true;
            if (!Character.isDigit(coords[1].charAt(0)))
                return true;
        }
        return false;
    }

    private static void replaceWildcards(String clauseStr, String actionStr, HashSet<Rule> rules) {
        ArrayList<String> prepLiteralStr = prepClause(clauseStr);
        ArrayList<String> prepActionStr = prepAction(actionStr);
        HashSet<String> wildCardsRow = new HashSet<>();
        HashSet<String> wildCardsCol = new HashSet<>();
        for (String lStr : prepLiteralStr) {
            String[] coords = Literal.getCoords(lStr);
            if (coords == null)
                continue;
            if (!Character.isDigit(coords[0].charAt(0)))
                wildCardsRow.add(coords[0]);
            if (!Character.isDigit(coords[1].charAt(0)))
                wildCardsCol.add(coords[1]);

        }
        for (String aStr : prepActionStr) {
            String[] coords = Literal.getCoords(aStr);
            if (coords == null)
                continue;
            if (!Character.isDigit(coords[0].charAt(0)))
                wildCardsRow.add(coords[0]);
            if (!Character.isDigit(coords[1].charAt(0)))
                wildCardsCol.add(coords[1]);
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

    public String print() {
        String cStr, aStr;
        if (clause != null && action != null) {
            cStr = clause.getFormattedString();
            aStr = action.getFormattedString();
        } else {
            cStr = clauseStr;
            aStr = actionStr;
        }
        return "IF [" + cStr + "] THEN [" + aStr + "]";
    }

    String getClauseStr() {
        if (clause != null)
            return clause.getFormattedString();
        else
            return clauseStr;
    }

    String getActionStr() {
        if (action != null)
            return action.getFormattedString();
        else
            return actionStr;
    }

    private ArrayList<Clause> getTransformedClauses() {
        ArrayList<Clause> transformedClauses = new ArrayList<>();
        ArrayList<Literal> nonBoardPlacements = clause.extractNonBoardPlacements();
        int[][] cBoard = clauseToBoard(clause);
        HashSet<Transform.TransformedArray> tSet = Transform.applyAll(FFTManager.gameSymmetries, cBoard);
        for (Transform.TransformedArray tArr : tSet)
            transformedClauses.add(boardToClause(tArr, nonBoardPlacements));

        return transformedClauses;
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
        for (Clause clause : transformedClauses) {
            boolean match = true;
            for (Literal l : clause.literals) {
                if (l.pieceOcc == PIECEOCC_ANY) {
                    Literal temp = new Literal(l);
                    temp.pieceOcc = PIECEOCC_PLAYER;
                    temp.format();
                    match = matchLiteral(temp, stLiterals);
                    if (!match)
                        break;

                    temp.pieceOcc = PIECEOCC_ENEMY;
                    temp.format();
                    match = matchLiteral(temp, stLiterals);
                } else {
                    match = matchLiteral(l, stLiterals);
                }
                if (!match)
                    break;

            }
            if (match) {
                Action a = action.transform(clause.transformations);
                FFTMove move = a.getMove(state.getTurn());

                if (FFTManager.logic.isLegalMove(state, move)) {
                    return move;
                }
            }
        }
        return null;
    }

    private boolean matchLiteral(Literal l, HashSet<Literal> stLiterals) {
        if (l.negation) {
            Literal temp = new Literal(l);
            temp.negation = false;
            temp.format();
            return !stLiterals.contains(temp);

        } else
            return stLiterals.contains(l);
    }

    // Returns a board with the literals on it, the value equals to the piece occ.
    public static int[][] clauseToBoard(Clause clause) {
        int height = FFTManager.gameBoardHeight;
        int width = FFTManager.gameBoardWidth;
        int[][] clauseBoard = new int[height][width];

        for (Literal l : clause.literals) {
            if (l.boardPlacement && l.row >= 0 && l.col >= 0 && l.row <= height-1 && l.col <= width-1) {
                if (l.negation)
                    clauseBoard[l.row][l.col] = -l.pieceOcc;
                else
                    clauseBoard[l.row][l.col] = l.pieceOcc;
            }
        }
        return clauseBoard;
    }

    // returns clause derives from transformed integer matrix and non-boardplacement literals
    private static Clause boardToClause(Transform.TransformedArray tArr, ArrayList<Literal> nonBoardPlacements) {
        ArrayList<Literal> literals = new ArrayList<>();
        for (int i = 0; i < tArr.board.length; i++) {
            for (int j = 0; j < tArr.board[i].length; j++) {
                int val = tArr.board[i][j];
                if (val < 0)
                    literals.add(new Literal(i, j, -val, true));
                else if (val > 0)
                    literals.add(new Literal(i, j, val, false));
            }
        }
        literals.addAll(nonBoardPlacements);
        return new Clause(tArr.transformations, literals);
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
        return (this.transformedClauses.equals(rule.transformedClauses) &&
                        (this.action.equals(rule.action)));
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(this.transformedClauses, this.action);
        return 31 * hash;
    }
}
