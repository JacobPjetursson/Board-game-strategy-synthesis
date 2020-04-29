package tictactoe.FFT;

import fftlib.Action;
import fftlib.Literal;
import fftlib.auxiliary.Position;
import fftlib.Rule;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;
import fftlib.gui.interactiveFFTNode;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.Node;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.PlayBox.InteractivePlayBox;

import java.util.ArrayList;

import static misc.Globals.*;
import static tictactoe.gui.board.BoardTile.blueStr;

public class InteractiveNode implements interactiveFFTNode {
    private Controller cont;
    private InteractivePlayBox pb;
    private Rule rule;
    private int perspective;
    private int tilesize;
    private Move move;
    private BoardTile actionTile;

    InteractiveNode(Controller cont) {
        this.cont = cont;
        this.rule = new Rule();
        this.tilesize = 60;
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

    @Override
    public javafx.scene.Node getInteractiveNode(Rule r) {
        this.rule = new Rule(r);
        this.pb = new InteractivePlayBox(tilesize, CLICK_INTERACTIVE, cont);
        this.move = (Move) r.getAction().getMove();
        actionTile = pb.getBoard().getTiles()[move.row][move.col];
        actionTile.setAction(true);

        pb.update(r);

        return pb;
    }

    public void setActionFromTile(BoardTile bt) {
        for (int i = 1; i < 4; i++) {
            Position pos = new Position(bt.getRow(), bt.getCol(), i);
            Literal l = new Literal(Atoms.posToId.get(pos), false);
            rule.removePrecondition(l);
            l.setNegated(true);
            rule.removePrecondition(l);
        }
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
            this.move = (Move) a.getMove();
        actionTile = pb.getBoard().getTiles()[move.row][move.col];
        actionTile.setAction(true);
        pb.update(rule);
    }
    private Rule getRuleFromState(Node n) {
        Rule r = new Rule();
        LiteralSet literals = n.getState().getLiterals();
        ArrayList<Literal> literalList = new ArrayList<>(literals);
        // TODO - Only take boardplacement?
        LiteralSet lits = new LiteralSet(literalList);
        r.setPreconditions(lits);
        return r;
    }
    @Override
    public void clear() {
        this.actionTile = null;
        this.rule = new Rule();
        this.move = null;
    }

    // TODO
    public void updateRuleFromTile(BoardTile bt) {
        /*
        //rule.removeLiterals(bt.getRow(), bt.getCol());
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

         */
    }
}
