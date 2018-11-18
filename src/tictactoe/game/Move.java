package tictactoe.game;

import fftlib.game.FFTMove;
import misc.Config;

import java.util.Objects;

public class Move implements FFTMove {
    public int row;
    public int col;
    public int team;

    public Move(int row, int col, int team) {
        this.row = row;
        this.col = col;
        this.team = team;
    }

    public Move(Move move) {
        this.row = move.row;
        this.col = move.col;
        this.team = move.team;
    }

    public Move(int team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return row == move.row &&
                col == move.col &&
                team == move.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, team);
    }

    public Move reflect() {
        int[][] board = new int[Config.getBoardWidth()][Config.getBoardHeight()];
        if (col != -1)
            board[row][col] = team;

        int[][] ref = new int[Config.getBoardWidth()][Config.getBoardHeight()];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                ref[i][j] = board[i][board.length - 1 - j];
            }
        }
        Move refMove = new Move(team);
        boolean newSet = false;
        for (int i = 0; i < ref.length; i++) {
            for (int j = 0; j < ref[i].length; j++) {
                if (ref[i][j] == team) {
                    refMove.row = i;
                    refMove.col = j;
                    newSet = true;
                }
            }
        }
        if (!newSet) {
            refMove.row = -1;
            refMove.col = -1;
        }
        return refMove;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public String print() {
        return "A_" + team + ": " + "(" + row + "," + col + ")";
    }
}
