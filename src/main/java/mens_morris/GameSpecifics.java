package mens_morris;

import com.google.common.collect.Sets;
import fftlib.auxiliary.Position;
import fftlib.auxiliary.Transform;
import fftlib.game.FFTGameSpecifics;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import fftlib.game.LiteralSet;
import fftlib.gui.FFTFailNode;
import fftlib.gui.interactiveFFTNode;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.logic.Rule;
import fftlib.logic.SymmetryRule;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            remSet.add(new Literal(Atoms.posToId.get(pos)));
            return new Action(new LiteralSet(), remSet);
        } else { // move
            Position addPos = new Position(move.newRow, move.newCol, move.team);
            Position remPos = new Position(move.oldRow, move.oldCol, move.team);
            LiteralSet addSet = new LiteralSet();
            LiteralSet remSet = new LiteralSet();
            addSet.add(new Literal(Atoms.posToId.get(addPos)));
            remSet.add(new Literal(Atoms.posToId.get(remPos)));
            return new Action(addSet, remSet);
        }
    }

    @Override
    public LiteralSet nodeToLiterals(FFTNode n) {
        Node node = (Node) n;
        LiteralSet literals = new LiteralSet();
        if (node.getTurn() == PLAYER1)
            literals.add(new Literal("p1Turn"));
        if (node.phase2)
            literals.add(new Literal("phase2"));
        if (!THREE_MENS && node.canRemove)
            literals.add(new Literal("canRemove"));
        for (int i = 0; i < node.board.length; i++) {
            for (int j = 0; j < node.board[i].length; j++) {
                if (!Node.validPos(i, j))
                    continue;
                int occ = node.board[i][j];
                if (occ == 0)
                    continue;
                Position pos = new Position(i, j, occ);
                int id = Atoms.posToId.get(pos);
                literals.add(new Literal(id, false));
            }
        }
        return literals;
    }

    @Override
    public Rule gdlToRule(String precons, String action) {
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
        return Atoms.stringToId.getOrDefault(name, 0);
    }

    @Override
    public FFTFailNode getFailNode() {
        return null;
    }

    @Override
    public interactiveFFTNode getInteractiveNode() {
        return null;
    }

    @Override
    public HashSet<SymmetryRule> getSymmetryRules(Rule rule) {
        int[] transformations = new int[] {TRANS_HREF, TRANS_VREF, TRANS_ROT};
        return Transform.getSymmetryRules(transformations, rule);
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
                precons.add(new Literal("p1Turn"));
            else
                precons.add(new Literal("!p1Turn"));
        }

        for (Literal rem : action.rems)
            precons.addAll(Atoms.remToPrecons.get(rem));


        if (action.rems.isEmpty()) {// phase 1
            precons.add(new Literal("!phase2"));
            if (!THREE_MENS)
                precons.add(new Literal("!canRemove"));

        }
        else if (action.adds.isEmpty()) { // canRemove
            precons.add(new Literal("canRemove"));
            // turn
            Literal rem = action.rems.iterator().next();
            if (rem.getName().startsWith("P1"))
                precons.add(new Literal("!p1Turn"));
            else
                precons.add(new Literal("p1Turn"));

        } else { // phase 2, can only move
            if (!THREE_MENS)
                precons.add(new Literal("!canRemove"));
            precons.add(new Literal("phase2"));
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

    @Override
    public HashSet<LiteralSet> getCoveredStates(Rule rule) {
        HashSet<LiteralSet> coveredStates = new HashSet<>();
        List<Set<Literal>> subsets = new ArrayList<>();
        LiteralSet precons = rule.getAllPreconditions();
        int row, col, occ;
        Position pos;
        Literal l, negLit;
        LiteralSet litSet;

        // find sets of preconditions that can not co-exist

        // canRemove
        if (!THREE_MENS) {
            l = new Literal("canRemove");
            negLit = new Literal("!canRemove");
            if (!precons.contains(l) && !precons.contains(negLit)) {
                litSet = new LiteralSet(Literal.NULL);
                litSet.add(new Literal("canRemove"));
                subsets.add(litSet);
            }
        }
        // turn
        l = new Literal("p1Turn");
        negLit = new Literal("!p1Turn");
        if (!precons.contains(l) && !precons.contains(negLit)) {
            litSet = new LiteralSet(Literal.NULL);
            litSet.add(new Literal("p1Turn"));
        }

        // phase 2
        l = new Literal("phase2");
        negLit = new Literal("!phase2");
        if (!precons.contains(l) && !precons.contains(negLit)) {
            litSet = new LiteralSet(Literal.NULL);
            litSet.add(new Literal("phase2"));
            subsets.add(litSet);
        }

        for (row = 0; row < Node.BOARD_SIZE; row++) {
            for (col = 0; col < Node.BOARD_SIZE; col++) {
                if (!Node.validPos(row, col))
                    continue;
                boolean setExists = false;
                litSet = new LiteralSet();
                // make set of all relevant literals from this cell
                for (occ = PLAYER1; occ <= PLAYER2; occ++) {
                    pos = new Position(row, col, occ);
                    l = new Literal(posToId(pos), false);
                    if (precons.contains(l)) {
                        setExists = true;
                        break;
                    }
                    negLit = new Literal(posToId(pos), true);
                    if (!precons.contains(negLit)) {
                        litSet.add(l);
                    }
                }
                if (!setExists) {
                    litSet.add(Literal.NULL);
                    subsets.add(litSet);
                }
            }
        }
        // Find the cartesian product of those sets (combination of sets where
        // we only pick one from each set)
        Set<List<Literal>> cartesianSet = Sets.cartesianProduct(subsets);
        // Add each if not null literal
        for (List<Literal> literals : cartesianSet) {
            LiteralSet newSet = new LiteralSet(precons);
            for (Literal lit : literals) {
                if (!lit.equals(Literal.NULL))
                    newSet.add(lit);
            }
            coveredStates.add(newSet);
        }
        return coveredStates;
    }

    @Override
    public long getNumberOfCoveredStates(Rule rule) {
        LiteralSet precons = rule.getAllPreconditions();
        long coveredStates = 1;
        int row, col, occ;
        Literal l, lneg;
        Position pos;

        // phase 2
        l = new Literal("phase2");
        lneg = new Literal("!phase2");
        if (!precons.contains(l) && !precons.contains(lneg))
            coveredStates *= 2;
        // turn
        l = new Literal("p1Turn");
        lneg = new Literal("!p1Turn");
        if (!precons.contains(l) && !precons.contains(lneg))
            coveredStates *= 2;
        if (!THREE_MENS) {
            // canRemove
            l = new Literal("canRemove");
            lneg = new Literal("!canRemove");
            if (!precons.contains(l) && !precons.contains(lneg))
                coveredStates *= 2;
        }

        for (row = 0; row < Node.BOARD_SIZE; row++) {
            for (col = 0; col < Node.BOARD_SIZE; col++) {
                if (!Node.validPos(row, col))
                    continue;
                int combinations = 3;
                // make set of all relevant literals from this cell
                for (occ = PLAYER1; occ <= PLAYER2; occ++) {
                    pos = new Position(row, col, occ);
                    l = new Literal(posToId(pos), false);
                    if (precons.contains(l)) {
                        combinations = 1;
                        break;
                    }
                    lneg = new Literal(posToId(pos), true);
                    if (precons.contains(lneg))
                        combinations--;
                }
                coveredStates *= combinations;
            }
        }
        return coveredStates;
    }
}
