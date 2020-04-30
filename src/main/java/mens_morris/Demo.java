package mens_morris;

import java.util.Scanner;

import static mens_morris.Logic.POS_NONBOARD;

public class Demo {

    public static void main(String[] args) {
        Node n = new Node();
        Scanner scan = new Scanner(System.in);
        System.out.println("Input move as <row, col> for add/remove and <oldRow, oldCol, newRow, newCol> for move");
        while (!Logic.gameOver(n)) {
            System.out.println("State: ");
            System.out.println(n);
            System.out.println("Literals:");
            System.out.println(n.convert());
            System.out.println("Legal moves: ");
            System.out.println(n.getLegalMoves());
            System.out.println("Player " + n.getTurn() + ", please make your move");
            try {
                String[] pos = scan.nextLine().split(",");
                Move move;
                int row = Integer.parseInt(pos[0]);
                int col = Integer.parseInt(pos[1]);
                if (pos.length == 2) { // add/remove
                    if (n.canRemove) {
                        move = new Move(row, col, POS_NONBOARD, POS_NONBOARD, n.getTurn());
                    } else {
                        move = new Move(POS_NONBOARD, POS_NONBOARD, row, col, n.getTurn());
                    }
                } else {
                    int newRow = Integer.parseInt(pos[2]);
                    int newCol = Integer.parseInt(pos[3]);
                    move = new Move(row, col, newRow, newCol, n.getTurn());
                }
                n = n.getNextState(move);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Incorrect format, please try again");
            }
        }
        System.out.println("The winner is player " + Logic.getWinner(n));

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
        LiteralSet precons = new HashSet<>();
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
}
