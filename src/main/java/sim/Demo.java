package sim;

import fftlib.*;
import fftlib.logic.LiteralSet;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.logic.rule.PropRule;

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
        LiteralSet precons = new LiteralSet();
        precons.add(new Literal("!P1(2, 3)"));
        precons.add(new Literal("!P2(2, 3)"));
        Action action = new Action("P2(0, 1)");
        PropRule rule = new PropRule(precons, action);
        System.out.println(rule.getSymmetryRules());
        // Make strategy with meta rules
        //ArrayList<FFT> ffts = FFTManager.load("FFTs/simFFT.txt");
        //FFTSolver.solveGame(n);
        //FFTManager.autogenFFT(ffts.get(0));
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
                n = n.getNextNode(move);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Incorrect format, please try again");
            }
        }
        System.out.println("The winner is player " + Logic.getWinner(n));
    }

}
