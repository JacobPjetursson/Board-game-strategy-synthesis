package tictactoe.FFT;

import fftlib.Action;
import fftlib.Literal;
import fftlib.Rule;
import fftlib.game.*;
import fftlib.gui.FFTFailState;
import fftlib.gui.InteractiveFFTState;
import misc.Config;
import misc.Globals;
import tictactoe.game.Controller;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.HashSet;

import static fftlib.Literal.PIECEOCC_ANY;
import static fftlib.Literal.PIECEOCC_PLAYER;
import static fftlib.game.Transform.*;
import static misc.Globals.*;


public class GameSpecifics implements FFTGameSpecifics {
    private Controller cont;
    public InteractiveState interactiveState;

    public GameSpecifics(Controller cont) {
        this.cont = cont;
    }

    @Override
    public FFTMove actionToMove(Action a, int team) {
        if (a == null || (a.addClause.isEmpty() && a.remClause.isEmpty()))
            return null;
        int row = -1;
        int col = -1;
        for (Literal l : a.addClause.literals) {
            row = l.row;
            col = l.col;
        }
        return new Move(row, col, team);
    }

    @Override
    public FFTState preconsToState(HashSet<Literal> literals, int team) {
        State s = new State();
        int opponent = team == PLAYER1 ? PLAYER2 : PLAYER1;
        for (Literal l : literals)
            if (l.boardPlacement && !l.negation && l.pieceOcc != PIECEOCC_ANY) {
                int entry;
                if (l.pieceOcc == PIECEOCC_PLAYER)
                    entry = team;
                else
                    entry = opponent;

                s.setBoardEntry(l.row, l.col, entry);
            }
        s.setTurn(team);
        return s;
    }

    @Override
    public Rule gdlToRule(String precons, String action) {
        HashSet<Literal> literals = new HashSet<>();
        Action a = new Action();
        if (!precons.isEmpty() && !precons.equals("∅")) {
            for (String precon : precons.split("∧")) {
                String t = precon.split("cell\\(")[1];
                String[] tok = t.split(",");
                int col = Integer.parseInt(tok[0].substring(0, 1)) - 1;
                int row = Integer.parseInt(tok[1].substring(0, 1)) - 1;
                String o = tok[2].substring(0, 1);
                int occ = o.equals("x") ? PLAYER1 : PLAYER2;
                boolean neg = false;
                if (o.equals("b")) {
                    occ = PLAYER_ANY;
                    neg = true;
                }
                Literal l = new Literal(row, col, occ, neg);
                l.name = precon;
                literals.add(l);
            }
        }
        String t = action.split("mark\\(")[1];
        String[] tok = t.split(",");
        int col = Integer.parseInt(tok[0].substring(0, 1)) - 1;
        int row = Integer.parseInt(tok[1].substring(0, 1)) - 1;
        int occ = tok[2].substring(0,1).equals("x") ? PLAYER1 : PLAYER2;
        Literal l = new Literal(row, col, occ, false);
        l.name = action;
        a.addClause.add(l);
        Rule r = new Rule(literals, a);
        return new Rule(literals, a);
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
    public int[] getAllowedTransformations() {
        return new int[]{TRANS_HREF, TRANS_VREF, TRANS_ROT};
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
    public FFTDatabase getDatabase() {
        return new Database();
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
    public int getMaxPrecons() {
        int[] dim = getBoardDim();
        return dim[0] * dim[1];
    }

    @Override
    public int getGameWinner() {
        if (Config.SIMPLE_RULES)
            return PLAYER1;
        return PLAYER_NONE;
    }
}
