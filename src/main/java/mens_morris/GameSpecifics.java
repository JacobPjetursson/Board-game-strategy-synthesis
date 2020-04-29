package mens_morris;

import com.google.common.collect.Sets;
import fftlib.*;
import fftlib.auxiliary.Position;
import fftlib.auxiliary.Transform;
import fftlib.game.*;
import fftlib.gui.FFTFailNode;
import fftlib.gui.interactiveFFTNode;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fftlib.auxiliary.Transform.*;
import static misc.Config.THREE_MENS;

public class GameSpecifics implements FFTGameSpecifics {
    @Override
    public FFTMove actionToMove(Action a) { // TODO
        return null;
    }

    @Override
    public Action moveToAction(FFTMove m) {
        return null;
    }

    @Override
    public State nodeToState(FFTNode n) {
        return null;
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
    public FFTLogic getLogic() {
        return new Logic();
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
        return null;
    }

    @Override
    public int getMaxStateLiterals() {
        if (THREE_MENS)
            return 10; // 9 + 1
        return 18; // 16 + 2
    }

    @Override
    public HashSet<Long> getCoveredStateBitCodes(Rule rule) {
        HashSet<Long> bitCodes = new HashSet<>();
        List<Set<Literal>> subsets = new ArrayList<>();
        LiteralSet precons = rule.getAllPreconditions();
        int row, col, occ;
        Position pos;
        Literal l, negLit;
        // find sets of preconditions that can not co-exist
        // TODO - canRemove and phase2

        for (row = 0; row < Node.BOARD_SIZE; row++) {
            for (col = 0; col < Node.BOARD_SIZE; col++) {
                if (!Node.validPos(row, col))
                    continue;
                boolean setExists = false;
                LiteralSet litSet = new LiteralSet();
                // make set of all relevant literals from this cell
                for (occ = 1; occ < 4; occ++) {
                    pos = new Position(row, col, occ);
                    l = new Literal(posToId(pos), false);
                    if (precons.contains(l)) {
                        setExists = true;
                        break;
                    }
                    negLit = new Literal(posToId(pos), true);
                    if (!precons.contains(negLit))
                        litSet.add(l);
                }
                if (!setExists) {
                    subsets.add(litSet);
                }
            }
        }
        // Find the cartesian product of those sets (combination of sets where
        // we only pick one from each set)
        Set<List<Literal>> cartesianSet = Sets.cartesianProduct(subsets);
        // Add each
        for (List<Literal> literals : cartesianSet) {
            LiteralSet newSet = new LiteralSet(precons);
            newSet.addAll(literals);
            bitCodes.add(newSet.getBitString());
        }
        return bitCodes;
    }

    @Override
    public long getNumberOfCoveredStates(Rule rule) {

        return 0;
    }
}
