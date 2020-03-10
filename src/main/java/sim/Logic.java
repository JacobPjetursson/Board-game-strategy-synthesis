package sim;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.util.ArrayList;
import java.util.LinkedList;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Logic implements FFTLogic {
    private static ArrayList<Line> LEGAL_LINES;
    static {
        LEGAL_LINES = new ArrayList<>();
        LEGAL_LINES.add(new Line(0, 1));
        LEGAL_LINES.add(new Line(0, 2));
        LEGAL_LINES.add(new Line(0, 3));
        LEGAL_LINES.add(new Line(0, 4));
        LEGAL_LINES.add(new Line(0, 5));
        LEGAL_LINES.add(new Line(1, 2));
        LEGAL_LINES.add(new Line(1, 3));
        LEGAL_LINES.add(new Line(1, 4));
        LEGAL_LINES.add(new Line(1, 5));
        LEGAL_LINES.add(new Line(2, 3));
        LEGAL_LINES.add(new Line(2, 4));
        LEGAL_LINES.add(new Line(2, 5));
        LEGAL_LINES.add(new Line(3, 4));
        LEGAL_LINES.add(new Line(3, 5));
        LEGAL_LINES.add(new Line(4, 5));
    }

    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, State state) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Line line : LEGAL_LINES) {
            boolean exists = false;
            for (Line l : state.lines) {
                if (l.samePos(line))
                    exists = true;
            }
            if (!exists)
                moves.add(new Move(team, line));
        }
        return moves;
    }

    public static boolean gameOver(State state) {
        for (Line l : state.lines) {

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

        Line l = new Line(m.line);
        l.color = m.team;
        state.lines.add(l);

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
