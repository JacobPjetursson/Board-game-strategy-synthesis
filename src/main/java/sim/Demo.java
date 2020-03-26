package sim;

import fftlib.*;
import fftlib.game.FFTSolution;
import fftlib.game.FFTSolver;
import fftlib.game.Transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import static fftlib.FFTManager.gameBoardHeight;
import static misc.Globals.*;

public class Demo {
    public static boolean playGame = false;

    public static void main(String[] args) {
        CURRENT_GAME = SIM;
        GameSpecifics gs = new GameSpecifics();
        new FFTManager(gs);
        State s = new State();
        if (playGame) {
            playGame(s);
            return;
        }
        // Make strategy with meta rules
        ArrayList<FFT> ffts = FFTManager.load("FFTs/simFFT.txt");
        FFTSolver.solveGame(s);
        FFTManager.autogenFFT(ffts.get(0));
    }

    public static void playGame(State s) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Input move as <u,v>");
        while (!Logic.gameOver(s)) {
            System.out.println("State: ");
            System.out.println(s);
            System.out.println("Player " + s.getTurn() + ", please make your move");
            try {
                String[] vertices = scan.nextLine().split(",");
                int u = Integer.parseInt(vertices[0]);
                int v = Integer.parseInt(vertices[1]);
                Move move = new Move(s.getTurn(), new Line(u, v));
                s = s.getNextState(move);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Incorrect format, please try again");
            }
        }
        System.out.println("The winner is player " + Logic.getWinner(s));
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
