package tictactoe.FFT;

import com.google.common.collect.Sets;
import fftlib.*;
import fftlib.auxiliary.Aux;
import fftlib.auxiliary.Position;
import fftlib.auxiliary.Transform;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;
import tictactoe.game.Controller;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fftlib.auxiliary.Transform.*;


public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public InteractiveState interactiveState;

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
    public Rule gdlToRule(String precons, String action) {
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
    public FFTState getInitialState() {
        return new State();
    }

    @Override
    public FFTLogic getLogic() {
        return new Logic();
    }

    @Override
    public ArrayList<Integer> getGameAtoms() {
        return Atoms.getGameAtoms();
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
        return new FailStatePane(cont);
    }

    @Override
    public InteractiveFFTState getInteractiveState() {
        if (interactiveState == null)
            interactiveState = new InteractiveState(cont);
        return interactiveState;
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

    // TODO - can we do this smarter and more domain-independent?
    // TODO - does there exist a more general version of this that is less ad-hoc?
    @Override
    public HashSet<Long> getCoveredStateBitCodes(Rule rule) {
        HashSet<Long> bitCodes = new HashSet<>();
        List<Set<Literal>> subsets = new ArrayList<>();
        int row, col, occ;
        Position pos;
        Literal l;
        // find sets of preconditions that can not co-exist
        for (row = 0; row < 3; row++) {
            for (col = 0; col < 3; col++) {
                boolean setExists = false;
                HashSet<Literal> litSet = new HashSet<>();
                // make set of all relevant literals from this cell
                for (occ = 1; occ < 4; occ++) {
                    pos = new Position(row, col, occ);
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
