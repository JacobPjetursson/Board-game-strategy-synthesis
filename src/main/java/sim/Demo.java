package sim;

import fftlib.*;
import fftlib.game.FFTSolver;

import java.util.ArrayList;
import java.util.Scanner;

public class Demo {
    public static boolean playGame = false;

    public static void main(String[] args) {
        GameSpecifics gs = new GameSpecifics();
        FFTManager.initialize(gs);
        Node n = new Node();
        if (playGame) {
            playGame(n);
            return;
        }
        // Make strategy with meta rules
        ArrayList<FFT> ffts = FFTManager.load("FFTs/simFFT.txt");
        FFTSolver.solveGame(n);
        FFTManager.autogenFFT(ffts.get(0));
    }

    public static void playGame(Node n) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Input move as <u,v>");
        while (!Logic.gameOver(n)) {
            System.out.println("State: ");
            System.out.println(n);
            System.out.println("Player " + n.getTurn() + ", please make your move");
            try {
                String[] vertices = scan.nextLine().split(",");
                int u = Integer.parseInt(vertices[0]);
                int v = Integer.parseInt(vertices[1]);
                Move move = new Move(n.getTurn(), new Line(u, v));
                n = n.getNextState(move);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Incorrect format, please try again");
            }
        }
        System.out.println("The winner is player " + Logic.getWinner(n));
    }
/*
    public static HashSet<LiteralSet> findAutomorphismsTest(LiteralSet literals) {
        int [] vertices = new int[gameBoardHeight];
        for (int i = 0; i < gameBoardHeight; i++) {
            vertices[i] = i;
        }
        ArrayList<int[]> permutations = Transform.findPermutations(vertices);
        HashSet<LiteralSet> transformations = new HashSet<>();
        System.out.println("PERMUTATIONS SIZE: " + permutations.size());
        for(int[] arr : permutations) {
            System.out.print("permutations: ");
            for (int i : arr) System.out.print(i + ", ");
            System.out.println();

            LiteralSet precons = new HashSet<>();

            Action action = null;
            for (Literal lit : rule.action.addClause.literals) {
                action = new Action(arr[lit.row], arr[lit.col], lit.pieceOcc, lit.negation);
            }

            for (Literal lit : literals) {
                precons.add(new Literal(arr[lit.row], arr[lit.col], lit.pieceOcc, lit.negation));
            }
            transformations.add(precons);
        }
        return transformations;
    }
    */
}
