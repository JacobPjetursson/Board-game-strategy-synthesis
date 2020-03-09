package sim;

import fftlib.game.FFTLogic;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;
import static sim.Line.NO_COLOR;

public class Logic implements FFTLogic {

    // Outputs a list of legal moves from a state
    static ArrayList<Move> legalMoves(int team, State state) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Line l : state.lines) {
            if (l.color == NO_COLOR)
                moves.add(new Move(team, l));
        }
        return moves;
    }

    public static boolean gameOver(State state) {
        for (Line l : state.lines) {
            if (l.color == NO_COLOR)
                continue;
            Point p1 = l.p1;
            Point p2 = l.p2;
            Set<Point> p1Set = getColoredLines(state.lines, l, p1);
            Set<Point> p2Set = getColoredLines(state.lines, l, p2);
            for (Point p : p1Set)
                if (p2Set.contains(p))
                    return true;
        }
        return false;
    }

    private static HashSet<Point> getColoredLines(ArrayList<Line> lines, Line line, Point point) {
        HashSet<Point> colorSet = new HashSet<>();
        for (Line l : lines) {
            if (l.equals(line) || l.color != line.color)
                continue;
            if (l.p1.equals(point))
                colorSet.add(l.p2);
            else if (l.p2.equals(point))
                colorSet.add(l.p1);
        }
        return colorSet;
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

        for (Line l : state.lines) {
            if (m.line.samePos(l)) {
                l.color = m.team;
                break;
            }

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
