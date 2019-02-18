package tictactoe.FFT;

import fftlib.*;
import fftlib.game.FFTState;
import fftlib.gui.InteractiveFFTState;
import javafx.scene.Node;
import tictactoe.game.Controller;
import tictactoe.game.State;
import tictactoe.gui.PlayBox;
import tictactoe.gui.board.BoardTile;

import java.util.ArrayList;
import java.util.HashSet;

import static misc.Config.CLICK_INTERACTIVE;

public class InteractiveState implements InteractiveFFTState {
    private Controller cont;
    private PlayBox pb;
    private State state;
    private Rule rule;
    private BoardTile actionTile;

    InteractiveState(Controller cont) {
        this.cont = cont;
        this.state = new State();
        this.rule = new Rule();
    }
    @Override
    public Node getInteractiveNode(FFTState fftState) {
        State s = (State) fftState;
        Node node = makePlayBox();
        updateRuleFromState(s);

        return node;
    }

    private Node makePlayBox() {
        int tilesize = 60;
        PlayBox pb = new PlayBox(tilesize, CLICK_INTERACTIVE, cont);
        this.pb = pb;
        pb.update(state);
        return pb;
    }

    public void setActionTile(BoardTile bt) {
        actionTile = bt;
    }

    public BoardTile getActionTile() {
        return actionTile;
    }

    @Override
    public Node getInteractiveNode(Rule r) {
        this.rule = r;
        return getInteractiveNode(state);
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    private void updateRuleFromState(State s) {
        HashSet<Literal> literals = s.getLiterals();
        ArrayList<Literal> literalList = new ArrayList<>(literals);
        // Remove scorelimit and points if initial
        ArrayList<Literal> lits = new ArrayList<>();
        for (Literal l : literalList) {
            if (l.boardPlacement)
                lits.add(l);
        }
        this.rule.setClause(new Clause(lits));
    }
    @Override
    public void clear() {
        this.rule = new Rule();
        this.state = new State();
    }

    @Override
    public void setPerspective(int team) {
        state.setTurn(team);
        if (actionTile != null) {
            actionTile.setMandatory(false);
            updateRuleFromTile(actionTile);
        }
        actionTile = null;
        updateRuleFromState(state);
    }

    @Override
    public int getPerspective() {
        return state.getTurn();
    }

    public void updateRuleFromTile(BoardTile bt) {
        if (!bt.isAction())
            state.setBoardEntry(bt.getRow(), bt.getCol(), bt.getTeam());
        rule.removeLiterals(bt.getRow(), bt.getCol());
        if (bt.isMandatory()) {
            Literal l;
            if (bt.getTeam() == -1) // Negated empty board = occupied board
                l = new Literal(bt.getRow(), bt.getCol(), bt.getTeam(), !bt.isNegated());
            else
                l = new Literal(bt.getRow(), bt.getCol(), bt.getTeam(), bt.isNegated());

            if (bt.isAction()) {
                Clause addClause = new Clause();
                Clause remClause = new Clause();
                addClause.add(l);
                rule.setAction(new Action(addClause, remClause));
            } else {
                System.out.println("adding literal");
                rule.addLiteral(l);
            }
        }
    }

    public PlayBox getPlayBox() {
        return pb;
    }

}
