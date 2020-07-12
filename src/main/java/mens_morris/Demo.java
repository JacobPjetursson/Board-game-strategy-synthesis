package mens_morris;

import fftlib.FFTManager;
import fftlib.logic.Action;
import fftlib.logic.Literal;
import fftlib.logic.Rule;

import java.util.Scanner;

import static mens_morris.Logic.POS_NONBOARD;

public class Demo {
    public static boolean PLAY_GAME = false;
    public static void main(String[] args) {
        GameSpecifics specs = new GameSpecifics();
        FFTManager.initialize(specs);
        if (PLAY_GAME)
            playGame();

        Node node = new Node();
        System.out.println(node.convert());
        System.out.println(node.convert().getBitString());
        Action testAction = new Action();
        testAction.adds.add(new Literal("P1(1, 0)"));
        testAction.rems.add(new Literal("P1(0, 0)"));
        Rule testRule = new Rule(node.convert(), testAction);
        System.out.println("All preconditions:");
        System.out.println(testRule.getAllPreconditions());
        System.out.println("Action preconditions:");
        System.out.println(testAction.getPreconditions());
        System.out.println(testRule.getSymmetryRules());
    }

    static void playGame() {
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
    }
}
