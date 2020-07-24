package tictactoe.FFT;

import fftlib.FFTManager;
import fftlib.auxiliary.Position;
import fftlib.game.FFTMove;
import fftlib.gui.FFTRuleEditPane;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.Rule;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.gui.board.BoardTile;
import tictactoe.gui.board.PlayBox.InteractivePlayBox;

import static misc.Config.AUTOGEN_TEAM;
import static misc.Globals.*;

public class RuleEditPane extends FFTRuleEditPane {
    private int tilesize;
    InteractivePlayBox pb;

    RuleEditPane(Controller cont) {
        this.rule = new PropRule();
        this.tilesize = 60;
        this.pb = new InteractivePlayBox(tilesize, CLICK_INTERACTIVE, cont);
        this.node = pb;
    }

    @Override
    public void update(Rule r) {
        this.rule = r;
        enable();
        pb.update(r);
    }

    @Override
    public Rule getRule() {
        return rule;
    }
    
    @Override
    public void clear() {
        pb.clear();
        pb.setDisable(true);
    }

    public void disable() {
        pb.setDisable(true);
        pb.setOpacity(0.3);
    }

    public void enable() {
        pb.setDisable(false);
        pb.setOpacity(1.0);
    }

    public void update(BoardTile bt) {
        fftEditPane.pushUndoStack();
        Rule clone = rule.clone();
        // start by clearing rule based on this tile
        for (int occ = -PLAYER2; occ <= PLAYER2; occ++) {
            if (occ == 0)
                continue;
            Position pos = new Position(bt.getRow(), bt.getCol(), occ);
            int id = FFTManager.getIdFromPos.apply(pos);
            Literal l = new PropLiteral(id);
            rule.removePrecondition(l);
        }
        if (bt.isAction()) // action of rule will be reset again according to this tile
            rule.removeAction();
        FFTMove m = rule.getAction().convert();
        if (m != null) { // action is set for the rule but not for the tile
            Move move = (Move) m;
            if (move.row == bt.getRow() && move.col == bt.getCol()) {// the position matches, so we have inconsistency
                rule.removeAction();
            }
        }

        // set new properties of this tile
        if (bt.isUsed() && !bt.isAction()) { // take action later
            Position pos;
            Literal l;
            if (bt.getTeam() == 0) { // add two literals
                int p1 = (bt.isNegated()) ? -PLAYER1 : PLAYER1;
                int p2 = (bt.isNegated()) ? -PLAYER2 : PLAYER2;
                pos = new Position(bt.getRow(), bt.getCol(), p1);
                l = new PropLiteral(FFTManager.getIdFromPos.apply(pos));
                rule.addPrecondition(l);

                pos = new Position(bt.getRow(), bt.getCol(), p2);
                l = new PropLiteral(FFTManager.getIdFromPos.apply(pos));
                rule.addPrecondition(l);
            } else {
                int occ = (bt.isNegated()) ? -bt.getTeam() : bt.getTeam();
                pos = new Position(bt.getRow(), bt.getCol(), occ);
                l = new PropLiteral(FFTManager.getIdFromPos.apply(pos));
                rule.addPrecondition(l);
            }
        }
        // set action (if relevant)
        if (bt.isAction()) {
            Move move = new Move(bt.getRow(), bt.getCol(), AUTOGEN_TEAM);
            rule.setAction(move.convert());
        }

        if (clone.equals(rule))
            fftEditPane.popUndoStack();
        fftEditPane.setRuleText(rule);
        fftEditPane.showRules();
    }
}
