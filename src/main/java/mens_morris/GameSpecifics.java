package mens_morris;

import fftlib.auxiliary.Position;
import fftlib.auxiliary.Transform;
import fftlib.game.FFTGameSpecifics;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.logic.literal.LiteralSet;
import fftlib.gui.FFTFailNode;
import fftlib.gui.FFTRuleEditPane;
import fftlib.logic.rule.Action;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.SymmetryRule;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;

import static fftlib.auxiliary.Transform.*;
import static mens_morris.Logic.POS_NONBOARD;
import static misc.Config.THREE_MENS;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class GameSpecifics implements FFTGameSpecifics {
    @Override
    public FFTMove actionToMove(Action a) {
        if (a == null)
            return null;
        if (a.rems.isEmpty()) { // phase 1
            Literal add = a.adds.iterator().next();
            Position pos = idToPos(add.id);
            return new Move(POS_NONBOARD, POS_NONBOARD, pos.row, pos.col, pos.occ);
        } else if (a.adds.isEmpty()) { // canRemove
            Literal rem = a.rems.iterator().next();
            Position pos = idToPos(rem.id);
            return new Move(pos.row, pos.col, POS_NONBOARD, POS_NONBOARD, pos.occ);
        } else { // move
            Literal add = a.adds.iterator().next();
            Literal rem = a.rems.iterator().next();
            Position addPos = idToPos(add.id);
            Position remPos = idToPos(rem.id);
            return new Move(remPos.row, remPos.col, addPos.row, addPos.col, addPos.occ);
        }
    }

    @Override
    public Action moveToAction(FFTMove m) {
        if (m == null)
            return null;
        Move move = (Move) m;
        if (move.oldCol == POS_NONBOARD) {// phase 1
            Position pos = new Position(move.newRow, move.newCol, move.team);
            return new Action(Atoms.posToId.get(pos));
        } else if (move.newCol == POS_NONBOARD) { // canRemove
            Position pos = new Position(move.oldRow, move.oldCol, move.team);
            LiteralSet remSet = new LiteralSet();
            remSet.add(new PropLiteral(Atoms.posToId.get(pos)));
            return new Action(new LiteralSet(), remSet);
        } else { // move
            Position addPos = new Position(move.newRow, move.newCol, move.team);
            Position remPos = new Position(move.oldRow, move.oldCol, move.team);
            LiteralSet addSet = new LiteralSet();
            LiteralSet remSet = new LiteralSet();
            addSet.add(new PropLiteral(Atoms.posToId.get(addPos)));
            remSet.add(new PropLiteral(Atoms.posToId.get(remPos)));
            return new Action(addSet, remSet);
        }
    }
    // todo
    @Override
    public LiteralSet nodeToLiterals(FFTNode n) {
        Node node = (Node) n;
        LiteralSet literals = new LiteralSet();
        String pfx = node.getTurn() == PLAYER1 ? "" : "!";

        literals.add(new PropLiteral(pfx + "p1Turn"));
        pfx = node.phase2 ? "" : "!";
        literals.add(new PropLiteral(pfx + "phase2"));
        if (!THREE_MENS) {
            pfx =  node.canRemove ? "" : "!";
            literals.add(new PropLiteral(pfx + "canRemove"));
        }
        Position pos;
        for (int i = 0; i < node.board.length; i++) {
            for (int j = 0; j < node.board[i].length; j++) {
                if (!Node.validPos(i, j))
                    continue;
                int occ = node.board[i][j];
                if (occ == 0) {
                    pos = new Position(i, j, -PLAYER1);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                    pos = new Position(i, j, -PLAYER2);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                } else if (occ == PLAYER1) {
                    pos = new Position(i, j, PLAYER1);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                    pos = new Position(i, j, -PLAYER2);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                } else {
                    pos = new Position(i, j, -PLAYER1);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                    pos = new Position(i, j, PLAYER2);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                }
            }
        }
        return literals;
    }

    @Override
    public PropRule gdlToRule(String precons, String action) {
        return null;
    }

    @Override
    public String getFFTFilePath() {
        if (Config.ENABLE_GGP_PARSER)
            return "FFTs/mens_morris_GGP_FFT.txt";
        return "FFTs/mens_morris_FFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        if (THREE_MENS) {
            return new int[] {3, 3};
        }
        return new int[] {5,5};
    }

    @Override
    public String[] getPlayerNames() {
        return new String[0];
    }

    @Override
    public FFTNode getInitialNode() {
        return new Node();
    }

    @Override
    public ArrayList<Integer> getGameAtoms() {
        return Atoms.gameAtoms;
    }

    @Override
    public String getAtomName(int atom) {
        return Atoms.idToString.get(atom);
    }

    @Override
    public int getAtomId(String name) {
        return Atoms.stringToId.get(name);
    }

    @Override
    public FFTFailNode getFailNode() {
        return null;
    }

    @Override
    public FFTRuleEditPane getInteractiveNode() {
        return null;
    }

    @Override
    public HashSet<SymmetryRule> getSymmetryRules(PropRule propRule) {
        int[] transformations = new int[] {TRANS_HREF, TRANS_VREF, TRANS_ROT};
        return Transform.getSymmetryRules(transformations, propRule);
    }

    @Override
    public int posToId(Position pos) {
        return Atoms.posToId.get(pos);
    }

    @Override
    public Position idToPos(int id) {
        return Atoms.idToPos.get(id);
    }

    @Override
    public LiteralSet getActionPreconditions(Action action) {
        LiteralSet precons = new LiteralSet();
        for (Literal add : action.adds) {
            precons.addAll(Atoms.addToPrecons.get(add));
            if (add.getName().startsWith("P1"))
                precons.add(new PropLiteral("p1Turn"));
            else
                precons.add(new PropLiteral("!p1Turn"));
        }

        for (Literal rem : action.rems)
            precons.addAll(Atoms.remToPrecons.get(rem));


        if (action.rems.isEmpty()) {// phase 1
            precons.add(new PropLiteral("!phase2"));
            if (!THREE_MENS)
                precons.add(new PropLiteral("!canRemove"));

        }
        else if (action.adds.isEmpty()) { // canRemove
            precons.add(new PropLiteral("canRemove"));
            // turn
            Literal rem = action.rems.iterator().next();
            if (rem.getName().startsWith("P1"))
                precons.add(new PropLiteral("!p1Turn"));
            else
                precons.add(new PropLiteral("p1Turn"));

        } else { // phase 2, can only move
            if (!THREE_MENS)
                precons.add(new PropLiteral("!canRemove"));
            precons.add(new PropLiteral("phase2"));
        }
        return precons;
    }

    @Override
    public int getMaxStateLiterals() {
        if (THREE_MENS)
            return 11; // 9 + 2
        return 19; // 16 + 3
    }

    @Override
    public ArrayList<Integer> legalIndices() {
        return null;
    }
}
