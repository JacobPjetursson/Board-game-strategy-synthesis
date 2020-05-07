package kulibrat.FFT;

import fftlib.game.LiteralSet;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.logic.Rule;
import fftlib.game.FFTNode;
import fftlib.gui.interactiveFFTNode;
import javafx.scene.paint.Color;
import kulibrat.game.Controller;
import kulibrat.game.Move;
import kulibrat.game.Node;
import kulibrat.gui.board.BoardPiece;
import kulibrat.gui.board.BoardTile;
import kulibrat.gui.board.PlayBox.InteractivePlayBox;

import java.util.ArrayList;

import static misc.Globals.*;


public class InteractiveNode implements interactiveFFTNode {
    private InteractivePlayBox pb;
    private BoardPiece selected;
    private ArrayList<Move> curHighLights;
    private Rule rule;
    private Move move;
    private int perspective;
    private Controller cont;
    private int tilesize;

    InteractiveNode(Controller cont) {
        this.rule = new Rule();
        this.cont = cont;
        this.tilesize = 52;
        curHighLights = new ArrayList<>();
    }

    @Override
    public javafx.scene.Node getInteractiveNode(FFTNode fftNode) {
        Node n = (Node) fftNode;
        this.perspective = n.getTurn();
        this.rule = getRuleFromState(n);

        this.pb = new InteractivePlayBox(tilesize, CLICK_INTERACTIVE, cont);
        pb.update(n);
        return pb;
    }

    // Assume correct and unambigious format
    public javafx.scene.Node getInteractiveNode(Rule r) {
        this.rule = new Rule(r);
        this.pb = new InteractivePlayBox(tilesize, CLICK_INTERACTIVE, cont);
        if (r.getAction() != null)
            this.move = (Move) r.getAction().convert();
        pb.update(r);

        return pb;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    public void setAction(Action a) {
        this.rule.setAction(a);
        if (a == null)
            this.move = null;
        else
            this.move = (Move) a.convert();

        pb.update(rule);
    }

    public void removeAction() {
        pb.removeArrows();
        rule.setAction(null);
        this.move = null;
    }

    // This is called when the add rule button is pressed from game screen
    private Rule getRuleFromState(Node n) { // TODO - requires Atoms class
        /*

        Rule r = new Rule();
        LiteralSet literals = n.convert().getAll();
        // Remove scorelimit and points
        LiteralSet lits = new LiteralSet();
        for (Literal l : literals) {
            Position pos = Atoms.idToPos(l.id);
            if (pos != null)
                lits.add(l);
        }
        r.setPreconditions(lits);
        return r;

         */
        return null;
    }

    public void clear() {
        deselect();
        this.rule = new Rule();
        this.move = null;
        curHighLights.clear();
    }

    private void highlightMoves(int row, int col, int team, boolean highlight) { // TODO
        /*
        BoardTile[][] tiles = pb.getBoard().getTiles();

        if (this.perspective != team)
            return;
        if (highlight) {
            int[][] board = setTeamForBoard(Transform.preconsToBoard(rule.preconditions), perspective);
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

         */
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

    public void updateRuleFromTile(BoardTile bt) { // TODO
        /*
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

         */
    }

    public void setArrowEndpoint(int row, int col) {
        move = new Move(selected.getRow(), selected.getCol(), row, col, perspective);
        rule.setAction(move.convert());

        pb.removeArrows();
        highlightMoves(move.oldRow, move.oldCol, perspective,false);
        pb.addArrow(move, Color.BLUE);
        deselect();
    }
    public void setScoreLimit(int value, boolean mandatory) { // TODO
/*
        // Check if exists
        Literal scoreLimit = null;
        for (Literal l : rule.preconditions)
            if (l.getName().toUpperCase().startsWith("SL="))
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

 */
    }

    public void setScore(int team, int value, boolean mandatory) { // TODO

 /*
        // Check if exists
        Literal score = null;
        String prefix =(team == PLAYER1) ? "P1SCORE=" : "P2SCORE=";
        for (Literal l : rule.preconditions)
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

 */
    }

    private int[][] setTeamForBoard(int[][] board, int team) { // TODO
        /*
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

         */
        return null;
    }

}
