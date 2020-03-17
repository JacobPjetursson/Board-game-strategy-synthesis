package sim;

import fftlib.*;
import fftlib.game.Transform;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import sim.ai.LookupTableMinimax;

import java.util.ArrayList;
import java.util.HashSet;

import static fftlib.FFTManager.gameBoardHeight;
import static misc.Globals.*;

public class Main {

    public static void main(String[] args) {
        CURRENT_GAME = SIM;
        State s = new State();
        new LookupTableMinimax(PLAYER1, s);

        GameSpecifics specs = new GameSpecifics();
        FFTManager fftManager = new FFTManager(specs);

        try {
            fftManager.autogenFFT();
        } catch (TransitionDefinitionException | MoveDefinitionException | GoalDefinitionException e) {
            e.printStackTrace();
        }
    }

    public static HashSet<Clause> findAutomorphismsTest(Clause clause) {
        int [] vertices = new int[gameBoardHeight];
        for (int i = 0; i < gameBoardHeight; i++) {
            vertices[i] = i;
        }
        ArrayList<int[]> permutations = Transform.findPermutations(vertices);
        HashSet<Clause> transformations = new HashSet<>();
        System.out.println("PERMUTATIONS SIZE: " + permutations.size());
        for(int[] arr : permutations) {
            System.out.print("permutations: ");
            for (int i : arr) System.out.print(i + ", ");
            System.out.println();

            Clause precons = new Clause();
            /*
            Action action = null;
            for (Literal lit : rule.action.addClause.literals) {
                action = new Action(arr[lit.row], arr[lit.col], lit.pieceOcc, lit.negation);
            }
            */
            for (Literal lit : clause.literals) {
                precons.add(new Literal(arr[lit.row], arr[lit.col], lit.pieceOcc, lit.negation));
            }
            transformations.add(new Clause(precons));
        }
        return transformations;
    }
}
