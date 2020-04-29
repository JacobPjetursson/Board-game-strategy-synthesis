package sim;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTNode;

import java.util.ArrayList;
import java.util.LinkedList;

import static misc.Config.SIM_SIMPLE_RULES;
import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;
import static sim.Line.NO_COLOR;

public class Logic implements FFTLogic {

    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, Node node) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Line line : node.lines) {
            if (line.color == NO_COLOR)
                moves.add(new Move(team, line));
        }
        return moves;
    }

    public static boolean gameOver(Node node) {
        // SMALLER STATESPACE FOR DEBUGGING
        if (SIM_SIMPLE_RULES) {
            int p1_count = 0;
            int p2_count = 0;
            for (Line l : node.lines) {
                if (l.color == PLAYER1)
                    p1_count++;
                else if (l.color == PLAYER2)
                    p2_count++;
            }
            if (p1_count > 3 || p2_count > 3)
                return true;
        }


        for (Line l : node.lines) {
            if (l.color == Line.NO_COLOR)
                continue;
            ArrayList<Integer> n1Set = getLinesFromNode(node.lines, l, l.n1);
            ArrayList<Integer> n2Set = getLinesFromNode(node.lines, l, l.n2);
            for (int n : n1Set)
                if (n2Set.contains(n))
                    return true;
        }

        return false;

    }

    private static ArrayList<Integer> getLinesFromNode(LinkedList<Line> lines, Line line, int node) {
        ArrayList<Integer> nodes = new ArrayList<>();
        for (Line l : lines) {
            if (l.equals(line) || l.color != line.color)
                continue;
            if (l.n1 == node)
                nodes.add(l.n2);
            else if (l.n2 == node)
                nodes.add(l.n1);
        }
        return nodes;
    }

    // Finds the winner, granted that the game is over
    public static int getWinner(Node node) {
        if (gameOver(node)) {
            return node.getMove().team == PLAYER1 ? PLAYER2 : PLAYER1;
        }
        return 0;
    }

    static void doTurn(Move m, Node node) {
        if (m.team != node.getTurn()) {
            System.out.println("Not your turn");
            return;
        }

        for (Line l : node.lines)
            if (l.samePos(m.line)) {
                l.color = m.team;
                break;
            }

        // Change turn
        if (node.getTurn() == PLAYER1)
            node.setTurn(PLAYER2);
        else
            node.setTurn(PLAYER1);
    }

    public boolean gameOver(FFTNode node) {
        return gameOver((Node) node);
    }

    public int getWinner(FFTNode node) {
        return getWinner((Node) node);
    }

    public boolean isLegalMove(FFTNode node, FFTMove move) {
        Move m = (Move) move;
        return legalMoves(move.getTeam(), (Node) node).contains(m);
    }
}
