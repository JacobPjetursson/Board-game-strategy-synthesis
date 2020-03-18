package sim;

import fftlib.*;
import fftlib.game.Transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import static fftlib.FFTManager.gameBoardHeight;
import static misc.Globals.*;

public class Demo {

    public static void main(String[] args) {
        CURRENT_GAME = SIM;
        State s = new State();
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

/*
        Move m = new Move(PLAYER1, new Line(0, 1));
        Move m1 = new Move(PLAYER2, new Line(0, 2));
        Move m2 = new Move(PLAYER1, new Line(0, 3));
        Move m3 = new Move(PLAYER2, new Line(3, 5));
        Move m4 = new Move(PLAYER1, new Line(3, 1));
        State s1 = s.getNextState(m);
        System.out.println(s1);
        System.out.println(s1.hashCode());
        State s2 = s1.getNextState(m1);
        System.out.println(s2);
        System.out.println(s2.hashCode());
        State s3 = s2.getNextState(m2);
        System.out.println(s3);
        System.out.println(s3.hashCode());
        State s4 = s3.getNextState(m3);
        System.out.println(s4);
        System.out.println(s4.hashCode());
        State s5 = s4.getNextState(m4);
        System.out.println(s5);
        System.out.println(s5.hashCode());

        System.out.println();

        State ss1 = s.getNextState(m2);
        System.out.println(ss1.hashCode());
        State ss2 = ss1.getNextState(m1);
        System.out.println(ss2.hashCode());
        State ss3 = ss2.getNextState(m);
        System.out.println(ss3.hashCode());
        State ss4 = ss3.getNextState(m3);
        System.out.println(ss4.hashCode());
        State ss5 = ss4.getNextState(m4);
        System.out.println(ss5.hashCode());


        FFTManager.gameBoardHeight = 6;
        HashSet<Literal> precons = new HashSet<>();
        //precons.add(new Literal(0, 1, Literal.PIECEOCC_PLAYER, false));
        //precons.add(new Literal(1, 2, Literal.PIECEOCC_PLAYER, false));

        precons.add(new Literal(0, 1, Literal.PIECEOCC_PLAYER, false));
        precons.add(new Literal(1, 2, Literal.PIECEOCC_ENEMY, false));

        precons.add(new Literal(2, 3, Literal.PIECEOCC_PLAYER, false));
        precons.add(new Literal(3, 4, Literal.PIECEOCC_ENEMY, false));
        precons.add(new Literal(4, 5, Literal.PIECEOCC_PLAYER, false));
        precons.add(new Literal(5, 0, Literal.PIECEOCC_ENEMY, false));


        System.out.print("precons: ");
        for (Literal precon : precons) System.out.print(precon + ", ");
        System.out.println();

        // TEST FOR RULES
        Action action = new Action(2, 0, Literal.PIECEOCC_PLAYER, false);
        Rule rule = new Rule(precons, action);
        rule.setTransformedRules();
        System.out.println("TRANSFORMATION INFO:");
        for (Rule r : rule.symmetryRules)
            System.out.println(r);
        System.out.println("SYMMETRY RULES SIZE: " + rule.symmetryRules.size());


        // TEST FOR CLAUSES ONLY
        HashSet<Clause> symmetryClauses = findAutomorphismsTest(new Clause(precons));
        System.out.println("TRANSFORMATION INFO:");
        System.out.println(symmetryClauses.size());
        for (Clause c : symmetryClauses)
            System.out.println(c);

*/
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
