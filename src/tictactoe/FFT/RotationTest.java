package tictactoe.FFT;

import fftlib.game.Transform;
import tictactoe.ai.LookupTableMinimax;
import tictactoe.ai.MinimaxPlay;
import tictactoe.game.State;
import tictactoe.misc.Database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static fftlib.game.Transform.TRANS_HREF;
import static fftlib.game.Transform.TRANS_ROT;
import static fftlib.game.Transform.TRANS_VREF;
import static misc.Config.PLAYER1;

public class RotationTest {

    static int[] transforms = {TRANS_HREF, TRANS_VREF, TRANS_ROT};

    public static void main(String[] args) {
        HashSet<Transform.TransformedArray> transSet;
        State state = new State();
        state.setBoardEntry(0, 0, PLAYER1);
        transSet = Transform.applyAll(transforms, state.getBoard());
        for (Transform.TransformedArray t : transSet)
            System.out.println(t.hashCode());
        System.out.println();
        State copy = new State();
        copy.setBoardEntry(2, 2, PLAYER1);
        transSet = Transform.applyAll(transforms, copy.getBoard());
        for (Transform.TransformedArray t : transSet)
            System.out.println(t.hashCode());

        copy = new State();
        copy.setBoardEntry(2, 0, PLAYER1);
        System.out.println(copy.equals(state));

        copy = new State();
        copy.setBoardEntry(0, 2, PLAYER1);
        System.out.println(copy.equals(state));


        new LookupTableMinimax(PLAYER1, new State());
        //HashMap<State, MinimaxPlay> lookUp = Database.getLookupTable();
        //System.out.println(lookUp.size());
    }
}
