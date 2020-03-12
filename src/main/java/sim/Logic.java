package sim;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.util.ArrayList;
import java.util.LinkedList;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;
import static sim.Line.NO_COLOR;

public class Logic implements FFTLogic {

    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, State state) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Line line : state.lines) {
            if (line.color == NO_COLOR)
                moves.add(new Move(team, line));
        }
        return moves;
    }

    public static boolean gameOver(State state) {
        // SMALLER STATESPACE FOR DEBUGGING
/*
        int p1_count = 0;
        int p2_count = 0;
        for (Line l : state.lines) {
            if (l.color == PLAYER1)
                p1_count++;
            else if (l.color == PLAYER2)
                p2_count++;
        }
        if (p1_count > 3 || p2_count > 3)
            return true;
*/

        for (Line l : state.lines) {
            if (l.color == Line.NO_COLOR)
                continue;
            ArrayList<Integer> n1Set = getLinesFromNode(state.lines, l, l.n1);
            ArrayList<Integer> n2Set = getLinesFromNode(state.lines, l, l.n2);
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
    public static int getWinner(State state) {
        if (gameOver(state)) {
            return state.getMove().team == PLAYER1 ? PLAYER2 : PLAYER1;
        }
        return 0;
    }

    static void doTurn(Move m, State state) {
        if (gameOver(state)) return;

        if (m.team != state.getTurn()) {
            System.out.println("Not your turn");
            return;
        }

        for (Line l : state.lines)
            if (l.samePos(m.line)) {
                l.color = m.team;
                break;
            }

        // Change turn
        if (state.getTurn() == PLAYER1)
            state.setTurn(PLAYER2);
        else
            state.setTurn(PLAYER1);
    }

    public boolean gameOver(FFTState state) {
        return gameOver((State) state);
    }

    public int getWinner(FFTState state) {
        return getWinner((State) state);
    }

    public boolean isLegalMove(FFTState state, FFTMove move) {
        Move m = (Move) move;
        return legalMoves(move.getTeam(), (State) state).contains(m);
    }
}
