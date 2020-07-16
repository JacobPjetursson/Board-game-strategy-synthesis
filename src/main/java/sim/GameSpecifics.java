package sim;

import fftlib.auxiliary.Position;
import fftlib.auxiliary.Transform;
import fftlib.game.*;
import fftlib.gui.FFTFailNode;
import fftlib.gui.InteractiveFFTNode;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.Action;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.SymmetryRule;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;
import static sim.Line.NO_COLOR;

public class GameSpecifics implements FFTGameSpecifics {


    @Override
    public FFTMove actionToMove(Action a) {
        if (a == null || a.adds.size() != 1)
            return null;
        FFTMove m = null;
        for (Literal l : a.adds) {
            Position pos = Atoms.idToPos.get(l.id);
            m = new Move(pos. occ, new Line(pos.row, pos.col));
        }
        return m;
    }

    @Override
    public Action moveToAction(FFTMove move) {
        if (move == null)
            return null;
        Move m = (Move) move;
        Position pos = new Position(m.line.n1, m.line.n2, m.team);
        return new Action(Atoms.posToId.get(pos));
    }

    @Override
    public LiteralSet nodeToLiterals(FFTNode n) {
        Node node = (Node) n;
        LiteralSet literals = new LiteralSet();
        Position pos;
        for (Line l : node.lines) {
            int occ = l.color;
            if (occ == NO_COLOR) {
                pos = new Position(l.n1, l.n2, -PLAYER1);
                literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                pos = new Position(l.n1, l.n2, -PLAYER2);
                literals.add(new PropLiteral(Atoms.posToId.get(pos)));
            } else if (occ == PLAYER1) {
                pos = new Position(l.n1, l.n2, PLAYER1);
                literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                pos = new Position(l.n1, l.n2, -PLAYER2);
                literals.add(new PropLiteral(Atoms.posToId.get(pos)));
            } else {
                pos = new Position(l.n1, l.n2, -PLAYER1);
                literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                pos = new Position(l.n1, l.n2, PLAYER2);
                literals.add(new PropLiteral(Atoms.posToId.get(pos)));
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
            return "FFTs/sim_GGP_FFT.txt";
        return "FFTs/simFFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        return new int[] {6, 6};
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
    public InteractiveFFTNode getInteractiveNode() {
        return null;
    }

    @Override
    public HashSet<SymmetryRule> getSymmetryRules(PropRule propRule) {
        return Transform.findAutomorphisms(propRule);
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
        return Atoms.actionToPrecons.get(action);
    }

    @Override
    public int getMaxStateLiterals() {
        return 15;
    }

    @Override
    public ArrayList<Integer> legalIndices() {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            indices.add(i);
        }
        return indices;
    }
}
