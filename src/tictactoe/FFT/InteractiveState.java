package tictactoe.FFT;

import fftlib.Action;
import fftlib.Clause;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.FFTState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.PlayBox.InteractivePlayBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import static fftlib.Literal.PIECEOCC_PLAYER;
import static misc.Config.*;
import static misc.Config.PLAYER1;
import static tictactoe.gui.board.BoardTile.blueStr;

public class InteractiveState implements InteractiveFFTState {
    private Controller cont;
    private InteractivePlayBox pb;
    private Rule rule;
    private int perspective;
    private int tilesize;
    private Move move;
    private BoardTile actionTile;

    InteractiveState(Controller cont) {
        this.cont = cont;
        this.rule = new Rule();
        this.tilesize = 90;
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

    @Override
    public Node getInteractiveNode(Rule r) {
        this.rule = new Rule(r);
        this.pb = new InteractivePlayBox(tilesize, CLICK_INTERACTIVE, cont);
        this.move = (Move) r.action.getMove(perspective);
        actionTile = pb.getBoard().getTiles()[move.row][move.col];
        actionTile.setAction(true);
        if (r.multiRule) {
            // TODO - how to handle multirule?
        } else {
            pb.update(r, perspective);
        }
        return pb;
    }

    @Override
    public void setPerspective(int perspective) {
        this.perspective = perspective;
        if (this.move != null)
            this.move.team = perspective;
        pb.update(rule, perspective);
    }

    public void setActionFromTile(BoardTile bt) {
        rule.removeLiterals(bt.getRow(), bt.getCol());
        if (actionTile != null) {
            actionTile.removeHighlight();
            actionTile.setMandatory(false);
        }
        actionTile = bt;
        this.move = new Move(bt.getRow(), bt.getCol(), perspective);
        this.rule.setAction(move.getAction());
        pb.addHighlight(actionTile.getRow(), actionTile.getCol(), perspective, blueStr);
    }

    public void removeAction() {
        actionTile.removeHighlight();
        rule.setAction(null);
        move = null;
        actionTile = null;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public void setAction(Action a) {
        this.rule.setAction(a);
        if (a == null)
            this.move = new Move();
        else
            this.move = (Move) a.getMove(perspective);
        pb.update(rule, perspective);
    }
    private Rule getRuleFromState(State s) {
        Rule r = new Rule();
        HashSet<Literal> literals = s.getLiterals();
        ArrayList<Literal> literalList = new ArrayList<>(literals);
        // Only take boardplacement
        HashSet<Literal> lits = new HashSet<>();
        for (Literal l : literalList) {
            if (l.boardPlacement)
                lits.add(l);
        }
        r.setPreconditions(new Clause(lits));
        return r;
    }
    @Override
    public void clear() {
        this.actionTile = null;
        this.rule = new Rule();
        this.move = null;
    }

    @Override
    public int getPerspective() {
        return perspective;
    }

    public void updateRuleFromTile(BoardTile bt) {
        rule.removeLiterals(bt.getRow(), bt.getCol());
        if (move != null && (move.col == bt.getCol() && move.row == bt.getRow())) {
            removeAction();
        }

        if (bt.isMandatory()) {
            Literal l;
            if (bt.getTeam() == PLAYER_ANY) // Negated empty board = occupied board
                l = new Literal(bt.getRow(), bt.getCol(), bt.getTeam(), !bt.isNegated());
            else {
                int tilePerspective;
                if (perspective == PLAYER1)
                    tilePerspective = bt.getTeam();
                else
                    tilePerspective = (bt.getTeam() == PLAYER1) ? PLAYER2 : PLAYER1;

                l = new Literal(bt.getRow(), bt.getCol(), tilePerspective, bt.isNegated());
            }
            rule.addPrecondition(l);
        }
    }
}
