package kulibrat.FFT;

import fftlib.*;
import fftlib.game.FFTState;
import fftlib.game.Transform;
import fftlib.gui.InteractiveFFTState;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.gui.board.PlayBox.InteractivePlayBox;
import kulibrat.gui.board.PlayBox.PlayBox;
import kulibrat.gui.board.BoardPiece;
import kulibrat.gui.board.BoardTile;

import java.util.ArrayList;
import java.util.HashSet;

import static fftlib.Literal.PIECEOCC_PLAYER;
import static kulibrat.game.Logic.POS_NONBOARD;
import static misc.Config.*;


public class InteractiveState implements InteractiveFFTState {
    private InteractivePlayBox pb;
    private BoardPiece selected;
    private ArrayList<Move> curHighLights;
    private Rule rule;
    private Move move;
    private int perspective;
    private Controller cont;
    private int tilesize;

    InteractiveState(Controller cont) {
        this.rule = new Rule();
        this.cont = cont;
        this.tilesize = 60;
        curHighLights = new ArrayList<>();
    }

    @Override
    public Node getInteractiveNode(FFTState fftState) {
        State s = (State) fftState;
        this.perspective = s.getTurn();
        this.rule = getRuleFromState(s);

        this.pb = new InteractivePlayBox(tilesize, CLICK_INTERACTIVE, cont);
        pb.update(s);
        return pb;
    }

    // Assume correct and unambigious format
    public Node getInteractiveNode(Rule r) {
        this.rule = new Rule(r);
        this.pb = new InteractivePlayBox(tilesize, CLICK_INTERACTIVE, cont);
        this.move = (Move) r.action.getMove(perspective);
        if (r.multiRule) {
            // TODO - how to handle multirule?
        } else {
            pb.update(r, perspective);
        }
        return pb;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public void setPerspective(int perspective) {
        deselect();
        this.perspective = perspective;
        if (this.move != null)
            this.move.team = perspective;
        pb.update(rule, perspective);
    }

    public void setAction(Action a) {
        this.rule.setAction(a);
        if (a == null)
            this.move = null;
        else
            this.move = (Move) a.getMove(perspective);

        pb.update(rule, perspective);
    }

    public void removeAction() {
        pb.removeArrows();
        rule.setAction(null);
        this.move = null;
    }

    @Override
    public int getPerspective() {
        return perspective;
    }

    // This is called when the add rule button is pressed from game screen
    private Rule getRuleFromState(State s) {
        Rule r = new Rule();
        HashSet<Literal> literals = s.getLiterals();
        ArrayList<Literal> literalList = new ArrayList<>(literals);
        // Remove scorelimit and points
        ArrayList<Literal> lits = new ArrayList<>();
        for (Literal l : literalList)
            if (l.boardPlacement)
                lits.add(l);

        r.setPreconditions(new Clause(lits));
        return r;
    }

    public void clear() {
        deselect();
        this.rule = new Rule();
        this.move = null;
        curHighLights.clear();
    }

    private void highlightMoves(int row, int col, int team, boolean highlight) {
        BoardTile[][] tiles = pb.getBoard().getTiles();

        if (this.perspective != team)
            return;
        if (highlight) {
            int[][] board = setTeamForBoard(Rule.preconsToBoard(rule.preconditions), perspective);
            curHighLights = Logic.legalMovesFromPiece(row, col, team, board);
        }
        for (Move m : curHighLights) {
            if (m.newCol == POS_NONBOARD && m.newRow == POS_NONBOARD) {
                if (team == PLAYER1) {
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
    }

    private void deselect() {
        if (selected != null) {
            highlightMoves(selected.getRow(), selected.getCol(), selected.getTeam(), false);
            selected.deselect();
            selected = null;
        }
    }

    public void updateRuleFromTile(BoardTile bt) {
        rule.removeLiterals(bt.getRow(), bt.getCol());
        if (move != null && ((move.oldCol == bt.getCol() && move.oldRow == bt.getRow()) ||
                (move.newCol == bt.getCol() && move.newRow == bt.getRow()))) {
            removeAction();
        }
        if (bt.isMandatory()) {
            Literal l;
            int tileTeam = bt.getTeam();

            if (bt.getTeam() == PLAYER_ANY)
                // Negated empty board = occupied board
                l = new Literal(bt.getRow(), bt.getCol(), tileTeam, !bt.isNegated());
            else {
                int tilePerspective;
                if (perspective == PLAYER1)
                    tilePerspective = tileTeam;
                else
                    tilePerspective = (tileTeam == PLAYER1) ? PLAYER2 : PLAYER1;

                l = new Literal(bt.getRow(), bt.getCol(), tilePerspective, bt.isNegated());
            }

            rule.addPrecondition(l);
        }
    }

    public void setArrowEndpoint(int row, int col) {
        move = new Move(selected.getRow(), selected.getCol(), row, col, perspective);
        rule.setAction(move.getAction());

        pb.removeArrows();
        highlightMoves(move.oldRow, move.oldCol, perspective,false);
        pb.addArrow(move, Color.BLUE);
        deselect();
    }

    public void setScoreLimit(int value, boolean mandatory) {
        // Check if exists
        Literal scoreLimit = null;
        for (Literal l : rule.preconditions.literals)
            if (l.name.toUpperCase().startsWith("SL="))
                scoreLimit = l;

        if (mandatory) {
            String slString = "SL=" + value;
            if (scoreLimit != null)
                scoreLimit.name = slString;
            else {
                rule.addPrecondition(new Literal(slString));
            }
        } else if (scoreLimit != null) {
            rule.removePrecondition(scoreLimit);
        }
    }

    public void setScore(int team, int value, boolean mandatory) {
        // Check if exists
        Literal score = null;
        String prefix =(team == PLAYER1) ? "P1SCORE=" : "P2SCORE=";
        for (Literal l : rule.preconditions.literals)
            if (l.name.toUpperCase().startsWith(prefix))
                score = l;

        if (mandatory) {
            String scoreStr = prefix + value;
            if (score != null)
                score.name = scoreStr;
            else {
                rule.addPrecondition(new Literal(scoreStr));
            }
        } else if (score != null) {
            rule.removePrecondition(score);

        }
    }

    private int[][] setTeamForBoard(int[][] board, int team) {
        if (team == PLAYER1) // This means the values of PIECEOCC_PLAYER and PIECEOCC_ENEMY on board will be correct
            return board;
        int[][] copy = Transform.copyArray(board);
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy[i].length; j++) {
                if (copy[i][j] == PLAYER1)
                    copy[i][j] = PLAYER2;
                else if (copy[i][j] == PLAYER2)
                    copy[i][j] = PLAYER1;
            }
        }
        return copy;
    }

}