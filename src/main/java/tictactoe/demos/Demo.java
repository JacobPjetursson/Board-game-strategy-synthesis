package tictactoe.demos;

import fftlib.*;
import fftlib.game.FFTSolver;
import tictactoe.FFT.GameSpecifics;
import tictactoe.game.State;

import static misc.Globals.PLAYER1;

public class Demo {

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics(null);
        new FFTManager(gs);
        FFTSolver.solveGame(new State());


        Clause lits = new Clause();
        lits.add(new Literal("!PE(1, 1)"));
        lits.add(new Literal("!PE(1, 0)"));
        lits.add(new Literal("!PE(1, 2)"));
        lits.add(new Literal("!PE(0, 0)"));
        lits.add(new Literal("P(2, 2)"));

        Literal addLit = new Literal("P(0, 2)");
        Clause addC = new Clause();
        addC.add(addLit);
        Clause remC = new Clause();
        Action a = new Action(addC, remC);
        Rule r = new Rule(lits, a);
        System.out.println(r);
    }
}
