package sim;

import com.google.common.collect.Sets;
import fftlib.*;
import fftlib.auxiliary.Aux;
import fftlib.auxiliary.Position;
import fftlib.auxiliary.Transform;
import fftlib.game.FFTGameSpecifics;
import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public Rule gdlToRule(String precons, String action) {
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
    public FFTState getInitialState() {
        return new State();
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
    public FFTFailState getFailState() {
        return null;
    }

    @Override
    public InteractiveFFTState getInteractiveState() {
        return null;
    }

    @Override
    public HashSet<SymmetryRule> getSymmetryRules(Rule rule) {
        return Transform.findAutomorphisms(rule);
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
    public HashSet<Long> getCoveredStateBitCodes(Rule rule) {
        HashSet<Long> bitCodes = new HashSet<>();
        List<Set<Literal>> subsets = new ArrayList<>();
        int n1, n2, occ;
        Position pos;
        Literal l;
        // find sets of preconditions that can not co-exist
        for (n1 = 0; n1 < 6; n1++) {
            for (n2 = n1+1; n2 < 6; n2++) {
                boolean setExists = false;
                HashSet<Literal> litSet = new HashSet<>();
                // make set of all relevant literals from this cell
                for (occ = 1; occ < 4; occ++) {
                    pos = new Position(n1, n2, occ);
                    l = new Literal(posToId(pos), false);
                    if (rule.preconditions.contains(l)) {
                        setExists = true;
                        break;
                    }
                    Literal negLit = new Literal(posToId(pos), true);
                    if (!rule.preconditions.contains(negLit))
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
            HashSet<Literal> precons = new HashSet<>(rule.preconditions);
            precons.addAll(literals);
            bitCodes.add(Literal.getBitString(precons));
        }
        return bitCodes;
    }

    @Override
    public long getNumberOfCoveredStates(Rule rule) {
        int free_slots = FFTManager.max_precons - rule.preconditions.size();
        return Aux.pow2(3, free_slots);
    }
}
