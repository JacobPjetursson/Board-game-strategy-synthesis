package tictactoe.FFT;

import fftlib.auxiliary.Position;
import fftlib.auxiliary.Transform;
import fftlib.game.*;
import fftlib.gui.FFTFailNode;
import fftlib.gui.FFTRuleEditPane;
import fftlib.logic.literal.Literal;
import fftlib.logic.literal.LiteralSet;
import fftlib.logic.literal.PropLiteral;
import fftlib.logic.rule.Action;
import fftlib.logic.rule.PropRule;
import fftlib.logic.rule.SymmetryRule;
import misc.Config;
import tictactoe.game.Controller;
import tictactoe.game.Move;
import tictactoe.game.Node;

import java.util.ArrayList;
import java.util.HashSet;

import static fftlib.auxiliary.Transform.*;
import static misc.Config.SHOW_GUI;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;


public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public RuleEditPane interactiveNode;

    public GameSpecifics(Controller cont) {
        this.cont = cont;
    }

    public GameSpecifics() {

    }

    @Override
    public FFTMove actionToMove(Action a) {
        if (a == null || a.adds.size() != 1)
            return null;
        FFTMove m = null;
        for (Literal l : a.adds) {
            Position pos = Atoms.idToPos.get(l.id);
            m = new Move(pos.row, pos.col, pos.occ);
        }
        return m;
    }

    @Override
    public Action moveToAction(FFTMove move) {
        if (move == null)
            return null;
        Move m = (Move) move;
        Position pos = new Position(m.row, m.col, m.team);
        return new Action(Atoms.posToId.get(pos));
    }

    @Override
    public LiteralSet nodeToLiterals(FFTNode n) {
        Node node = (Node) n;
        LiteralSet literals = new LiteralSet();
        Position pos;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int occ = node.getBoard()[i][j];
                if (occ == 0) {
                    pos = new Position(i, j, -PLAYER1);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                    pos = new Position(i, j, -PLAYER2);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                } else if (occ == 1) {
                    pos = new Position(i, j, -PLAYER2);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                    pos = new Position(i, j, PLAYER1);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                } else {
                    pos = new Position(i, j, -PLAYER1);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                    pos = new Position(i, j, PLAYER2);
                    literals.add(new PropLiteral(Atoms.posToId.get(pos)));
                }
            }
        }
        return new LiteralSet(literals);
    }

    @Override
    public PropRule gdlToRule(String precons, String action) {
        // TODO
        return null;
    }

    @Override
    public String getFFTFilePath() {
        if (Config.ENABLE_GGP_PARSER)
            return "FFTs/tictactoe_GGP_FFT.txt";
        return "FFTs/tictactoeFFT.txt";
    }

    @Override
    public int[] getBoardDim() {
        return new int[] {3, 3};
    }

    @Override
    public String[] getPlayerNames() {
        return new String[]{"Cross", "Nought"};
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
        return new FailNodePane(cont);
    }

    @Override
    public FFTRuleEditPane getInteractiveNode() {
        if (interactiveNode == null && SHOW_GUI)
            interactiveNode = new RuleEditPane(cont);
        return interactiveNode;
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
        return Atoms.actionToPrecons.get(action);
    }

    @Override
    public int getMaxStateLiterals() {
        return 9;
    }

    @Override
    public ArrayList<Integer> legalIndices() {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            indices.add(i);
        }
        return indices;
    }

    /*
    public HashSet<LiteralSet> getCoveredStates(Rule rule) {
        HashSet<LiteralSet> states = new HashSet<>();
        List<Set<Literal>> subsets = new ArrayList<>();
        LiteralSet precons = rule.getAllPreconditions();
        int row, col, occ;
        Position pos;
        // find sets of preconditions that can not co-exist
        for (row = 0; row < 3; row++) {
            for (col = 0; col < 3; col++) {
                boolean setExists = false;
                LiteralSet litSet = new LiteralSet();
                // make set of all relevant literals from this cell
                for (occ = PLAYER1; occ <= PLAYER2; occ++) {
                    pos = new Position(row, col, occ);
                    Literal l = new Literal(posToId(pos));
                    if (precons.contains(l)) {
                        setExists = true;
                        break;
                    }
                    Literal negLit = new Literal(posToId(pos));
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
            for (Literal l : literals) {
                if (!l.equals(Literal.NULL))
                    newSet.add(l);
            }
            states.add(newSet);
        }
        return states;
    }

    @Override
    public long getNumberOfCoveredStates(Rule rule) {
        LiteralSet precons = rule.getAllPreconditions();
        long coveredStates = 1;
        int row, col, occ;
        Position pos;

        for (row = 0; row < 3; row++) {
            for (col = 0; col < 3; col++) {
                int combinations = 3;
                // make set of all relevant literals from this cell
                for (occ = PLAYER1; occ <= PLAYER2; occ++) {
                    pos = new Position(row, col, occ);
                    Literal l = new Literal(posToId(pos));
                    if (precons.contains(l)) {
                        combinations = 1;
                        break;
                    }
                    Literal negLit = new Literal(posToId(pos));
                    if (precons.contains(negLit))
                        combinations--;
                }
                coveredStates *= combinations;
            }
        }
        return coveredStates;
    }

     */
}
