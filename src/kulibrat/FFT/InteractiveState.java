package kulibrat.FFT;

import fftlib.*;
import fftlib.game.FFTState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.PlayBox;
import kulibrat.gui.board.BoardPiece;
import kulibrat.gui.board.BoardTile;

import java.util.ArrayList;
import java.util.HashSet;

import static fftlib.Literal.PIECEOCC_ANY;
import static misc.Config.*;


public class InteractiveState implements InteractiveFFTState {
    private Controller cont;
    private PlayBox pb;
    private BoardPiece selected;
    private ArrayList<Move> curHighLights;
    private State state;
    private Rule rule;
    private Move move;

    InteractiveState(Controller cont) {
        this.cont = cont;
        this.state = new State();
        this.rule = new Rule();
    }

    @Override
    public Node getInteractiveNode(FFTState fftState) {
        State s = (State) fftState;
        Node node = makePlayBox(s);
        this.state = new State(s);
        updateRule(s);

        return node;
    }

    // Assume correct and unambigious format
    public Node getInteractiveNode(Rule r) {
        this.rule = r;
        this.state = (State) FFTManager.clauseToState.apply(r.clause);
        return getInteractiveNode(state);
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public void setPerspective(int team) {
        deselect();
        state.setTurn(team);
        updateRule(state);
        pb.removeArrows();
    }

    private Node makePlayBox(State s) {
        int tilesize = 60;
        PlayBox pb = new PlayBox(tilesize, CLICK_INTERACTIVE, cont);
        this.pb = pb;

        pb.update(s);
        pb.addScore(s.getScoreLimit(), s.getScore(PLAYER1), s.getScore(PLAYER2), true);

        return pb;
    }

    private void updateRule(State s) {
        HashSet<Literal> literals = s.getLiterals();
        ArrayList<Literal> literalList = new ArrayList<>(literals);
        // Remove scorelimit and points
        ArrayList<Literal> lits = new ArrayList<>();
        for (Literal l : literalList) {
            if (l.boardPlacement)
                lits.add(l);
        }
        this.rule.setClause(new Clause(lits));
    }

    public void clear() {
        this.rule = new Rule();
        this.state = new State();
    }

    private void highlightMoves(int row, int col, int team, boolean highlight) {
        if (state.getTurn() != team)
            return;
        if (highlight) curHighLights = Logic.legalMovesFromPiece(row,
                col, team, state);
        BoardTile[][] tiles = pb.getBoard().getTiles();
        for (int i = 0; i < curHighLights.size(); i++) {
            Move m = curHighLights.get(i);

            if (m.newCol == -1 && m.newRow == -1) {
                if (state.getTurn() == PLAYER1) {
                    pb.getGoal(PLAYER1).setHighlight(highlight, false, false, "");
                } else {
                    pb.getGoal(PLAYER2).setHighlight(highlight, false, false, "");
                }
            } else tiles[m.newRow][m.newCol].setHighlight(highlight, false, false, "");
        }
    }

    public void setSelected(BoardPiece piece) {
        deselect();
        selected = piece;
        highlightMoves(piece.getRow(), piece.getCol(), piece.getTeam(), true);

        move = new Move();
        move.oldRow = piece.getRow();
        move.oldCol = piece.getCol();
        move.team = piece.getTeam();
    }

    private void deselect() {
        if (selected != null) {
            highlightMoves(selected.getRow(), selected.getCol(), selected.getTeam(), false);
            selected.deselect();
            selected = null;
        }
    }

    public void update(BoardTile bt) {
        state.setBoardEntry(bt.getRow(), bt.getCol(), bt.getTeam());
        rule.removeLiterals(bt.getRow(), bt.getCol());
        if (bt.isMandatory()) {
            Literal l;
            if (bt.getTeam() == -1)
                // Negated empty board = occupied board
                l = new Literal(bt.getRow(), bt.getCol(), bt.getTeam(), !bt.isNegated());
            else
                l = new Literal(bt.getRow(), bt.getCol(), bt.getTeam(), bt.isNegated());

            rule.addLiteral(l);
        }
    }

    public void setArrowEndpoint(int row, int col) {
        move.newRow = row;
        move.newCol = col;
        pb.removeArrows();
        pb.addArrow(move, Color.BLUE);
        ArrayList<Literal> addLits = new ArrayList<>();
        if (row != -1)
            addLits.add(new Literal(row, col, PIECEOCC_ANY, false));
        ArrayList<Literal> remLits = new ArrayList<>();
        if (move.oldRow != -1)
            remLits.add(new Literal(move.oldRow, move.oldCol, PIECEOCC_ANY, false));
        Clause addClause = new Clause(addLits);
        Clause remClause = new Clause(remLits);
        Action a = new Action(addClause, remClause);
        rule.setAction(a);
        highlightMoves(move.oldRow, move.oldCol, move.team, false);
        deselect();
    }

    public PlayBox getPlayBox() {
        return pb;
    }

    public void setScoreLimit(int value, boolean use) {
        // Check if exists
        Literal scoreLimit = null;
        for (Literal l : rule.clause.literals)
            if (l.name.startsWith("SL"))
                scoreLimit = l;
        if (use) {
            String slString = "SL=" + value;
            if (scoreLimit != null)
                scoreLimit.name = slString;
            else {
                rule.addLiteral(new Literal(slString));
            }
        } else if (scoreLimit != null) {
            rule.removeLiteral(scoreLimit);
        }
    }

    public void setScore(int team, int value, boolean use) {
        // Check if exists
        Literal score = null;
        String prefix =(team == PLAYER1) ? "P1SCORE=" : "P2SCORE=";
        for (Literal l : rule.clause.literals)
            if (l.name.startsWith(prefix))
                score = l;
        if (use) {
            String scoreStr = prefix + value;
            if (score != null)
                score.name = scoreStr;
            else {
                rule.addLiteral(new Literal(scoreStr));
            }
        } else if (score != null) {
            rule.removeLiteral(score);

        }
    }

}
