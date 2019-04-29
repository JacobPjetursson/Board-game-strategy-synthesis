package fftlib;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.game.Transform;

import java.util.*;

import static fftlib.Literal.PIECEOCC_ANY;
import static fftlib.Literal.PIECEOCC_ENEMY;
import static fftlib.Literal.PIECEOCC_PLAYER;
import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;
import static misc.Config.PLAYER_ANY;


public class Rule {
    private static final ArrayList<String> separators = new ArrayList<>(
            Arrays.asList("and", "And", "AND", "&", "∧"));
    private HashSet<Clause> transformedPrecons;
    public Clause preconditions;
    public Action action;
    private String actionStr, preconStr;


    // If multirule, the rule class instead contains a list of rules
    public boolean multiRule;
    public HashSet<Rule> rules;
    public boolean errors;

    // parsing constructor
    public Rule(String preconStr, String actionStr) {
        this.multiRule = isMultiRule(preconStr, actionStr);
        if (multiRule) {
            rules = new HashSet<>();
            replaceWildcards(preconStr, actionStr, rules);
            // TODO - format this shit
            this.preconStr = preconStr;
            this.actionStr = actionStr;
        } else {
            this.action = getAction(actionStr);
            this.preconditions = getPreconditions(preconStr);
            this.transformedPrecons = getTransformedPreconditions();
        }
        errors = !isValidRuleFormat(this);
    }

    public Rule(Clause precons, Action action) {
        this.multiRule = false;
        this.action = action;
        this.preconditions = precons;
        this.transformedPrecons = getTransformedPreconditions();
    }

    // Empty constructor to allow rule buildup
    public Rule() {
        this.preconditions = new Clause();
        this.action = new Action();
        this.transformedPrecons = getTransformedPreconditions();
        preconStr = "";
        actionStr = "";
    }

    // Duplicate constructor
    public Rule(Rule duplicate) {
        this.multiRule = duplicate.multiRule;
        if (multiRule) {
            rules = new HashSet<>(duplicate.rules);
            this.preconStr = duplicate.preconStr;
            this.actionStr = duplicate.actionStr;
        } else {
            this.action = new Action(duplicate.action);
            this.preconditions = new Clause(duplicate.preconditions);
            this.transformedPrecons = getTransformedPreconditions();
        }
        this.errors = duplicate.errors;

    }

    public void addPrecondition(Literal l) {
        if (!preconditions.literals.contains(l))
            preconditions.add(l);
        this.transformedPrecons = getTransformedPreconditions();
    }

    public void removePrecondition(Literal l) {
        this.preconditions.remove(l);
        this.transformedPrecons = getTransformedPreconditions();
    }

    public void removeLiterals(int row, int col) {
        ArrayList<Literal> removeList = new ArrayList<>();
        for (Literal l : preconditions.literals) {
            if (l.row == row && l.col == col)
                removeList.add(l);
        }
        preconditions.literals.removeAll(removeList);
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

        this.transformedPrecons = getTransformedPreconditions();
    }

    public void setAction(Action a) {
        if (a == null)
            this.action = new Action();
        else
            this.action = a;
    }

    public void setPreconditions(Clause c) {
        if (c == null)
            this.preconditions = new Clause();
        else
            this.preconditions = c;
        this.transformedPrecons = getTransformedPreconditions();
    }

    private static ArrayList<String> prepPreconditions(String preconStr) {
        ArrayList<String> precons = new ArrayList<>();
        StringBuilder regex = new StringBuilder();
        regex.append(separators.get(0));
        for (int i = 1; i < separators.size(); i++)
            regex.append("|").append(separators.get(i));

        String[] parts = preconStr.split(regex.toString());
        for (String part : parts) {
            if (part.length() < 2)
                continue;
            part = part.trim();
            precons.add(part);
        }
        return precons;
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

    private static Clause getPreconditions(String clauseStr) {
        HashSet<Literal> literals = new HashSet<>();
        for (String c : prepPreconditions(clauseStr)) {
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
        for (Literal l : r.preconditions.literals)
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

    private static boolean isMultiRule(String preconStr, String actionStr) {
        ArrayList<String> prepLiteralStr = prepPreconditions(preconStr);
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

    private static void replaceWildcards(String preconStr, String actionStr, HashSet<Rule> rules) {
        ArrayList<String> prepLiteralStr = prepPreconditions(preconStr);
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
            rules.add(new Rule(preconStr, actionStr));
            return;
        }

        for (String wc : wildCardsRow) {
            for (int i = 0; i < FFTManager.gameBoardHeight; i++) {
                String finalCStr = preconStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }
        }

        for (String wc : wildCardsCol) {
            for (int i = 0; i < FFTManager.gameBoardWidth; i++) {
                String finalCStr = preconStr.replace(wc, Integer.toString(i));
                String finalAStr = actionStr.replace(wc, Integer.toString(i));
                replaceWildcards(finalCStr, finalAStr, rules);
            }
        }
    }

    public String print() {
        String cStr, aStr;
        if (preconditions != null && action != null) {
            cStr = preconditions.getFormattedString();
            aStr = action.getFormattedString();
        } else {
            cStr = preconStr;
            aStr = actionStr;
        }
        return "IF [" + cStr + "] THEN [" + aStr + "]";
    }

    String getPreconStr() {
        if (preconditions != null)
            return preconditions.getFormattedString();
        else
            return preconStr;
    }

    String getActionStr() {
        if (action != null)
            return action.getFormattedString();
        else
            return actionStr;
    }

    private HashSet<Clause> getTransformedPreconditions() {
        HashSet<Clause> transformedPrecons = new HashSet<>();
        HashSet<Literal> nonBoardPlacements = preconditions.extractNonBoardPlacements();
        int[][] cBoard = preconsToBoard(preconditions);
        HashSet<Transform.TransformedArray> tSet = Transform.applyAll(FFTManager.gameSymmetries, cBoard);
        for (Transform.TransformedArray tArr : tSet) {
            transformedPrecons.add(boardToPrecons(tArr, nonBoardPlacements));
        }

        return transformedPrecons;
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
        for (Clause clause : transformedPrecons) { // TODO - possible to optimize here by only including unique clauses
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
        }
        return stLiterals.contains(l);
    }

    public boolean verify(int team) {
        if (team == PLAYER_ANY)
            return verify(PLAYER1) && verify(PLAYER2);
        FFTState initialState = FFTManager.initialFFTState;
        LinkedList<FFTState> frontier = new LinkedList<>();
        HashSet<FFTState> closedSet = new HashSet<>();
        frontier.add(initialState);
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        // Check if win or draw is even possible
        int score = FFTManager.db.queryState(initialState).getScore();
        if (team == PLAYER1 && score < -1000) {
            System.out.println("A perfect player 2 has won from start of the game");
            return false;
        } else if (team == PLAYER2 && score > 1000) {
            System.out.println("A perfect player 1 has won from the start of the game");
            return false;
        }

        while (!frontier.isEmpty()) {
            FFTState state = frontier.pop();
            if (FFTManager.logic.gameOver(state)) {
                if (FFTManager.logic.getWinner(state) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            } else if (team != state.getTurn()) {
                for (FFTState child : state.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            } else {
                FFTMove move = apply(state);
                ArrayList<? extends FFTMove> nonLosingPlays = FFTManager.db.nonLosingMoves(state);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    for (FFTMove m : state.getLegalMoves()) {
                        if (nonLosingPlays.contains(m)) {
                            FFTState nextState = state.getNextState(m);
                            if (!closedSet.contains(nextState)) {
                                closedSet.add(nextState);
                                frontier.add(nextState);
                            }
                        }
                    }
                } else if (!nonLosingPlays.contains(move)) {
                    return false;
                } else {
                    FFTState nextNode = state.getNextState(move);
                    if (!closedSet.contains(nextNode)) {
                        closedSet.add(nextNode);
                        frontier.add(nextNode);
                    }
                }
            }
        }
        return true;
    }

    // Returns a board with the literals on it, the value equals to the piece occ.
    public static int[][] preconsToBoard(Clause clause) {
        int height = FFTManager.gameBoardHeight;
        int width = FFTManager.gameBoardWidth;
        int[][] preconBoard = new int[height][width];

        for (Literal l : clause.literals) {
            if (l.boardPlacement && l.row >= 0 && l.col >= 0 && l.row <= height-1 && l.col <= width-1) {
                if (l.negation)
                    preconBoard[l.row][l.col] = -l.pieceOcc;
                else
                    preconBoard[l.row][l.col] = l.pieceOcc;
            }
        }
        return preconBoard;
    }

    // returns preconditions derives from transformed integer matrix and non-boardplacement literals
    private static Clause boardToPrecons(Transform.TransformedArray tArr, HashSet<Literal> nonBoardPlacements) {
        HashSet<Literal> literals = new HashSet<>();
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
        return (this.transformedPrecons.equals(rule.transformedPrecons) &&
                        (this.action.equals(rule.action)));
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(this.transformedPrecons, this.action);
        return 31 * hash;
    }
}
