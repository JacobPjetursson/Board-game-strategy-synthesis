package kulibrat.game;

import fftlib.Clause;
import fftlib.Literal;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import kulibrat.ai.Minimax.Zobrist;
import misc.Config;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class State implements Serializable, FFTState {
    private int[][] board;
    private int turn;
    private int redScore;
    private int blackScore;
    private int scoreLimit;
    private int unplacedRed;
    private int unplacedBlack;
    private ArrayList<Move> legalMoves;
    private Move move;
    private long zobrist_key;

    // Starting Root state
    public State() {
        int rows = Config.kuliBHeight;
        int columns = Config.kuliBWidth;
        board = new int[rows][columns];
        redScore = 0;
        blackScore = 0;
        unplacedRed = 4;
        unplacedBlack = 4;

        turn = PLAYER1;
        this.scoreLimit = Config.SCORELIMIT;
        this.zobrist_key = initHashCode();
    }

    // Non-root state
    private State(State parent, Move m) {
        this(parent);
        zobrist_key = parent.zobrist_key;
        move = m;
        Logic.doTurn(m, this);
        updateHashCode(parent);
    }

    // Duplicate constructor, for "root" state
    public State(State state) {
        board = new int[state.board.length][];
        for (int i = 0; i < state.board.length; i++) {
            board[i] = Arrays.copyOf(state.board[i], state.board[i].length);
        }
        redScore = state.redScore;
        blackScore = state.blackScore;
        unplacedRed = state.unplacedRed;
        unplacedBlack = state.unplacedBlack;
        turn = state.turn;
        scoreLimit = state.scoreLimit;
        move = state.move;
        zobrist_key = state.zobrist_key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State)) return false;
        return (((State) obj).getHashCode() == getHashCode());
    }

    @Override
    public int hashCode() {
        return (int) zobrist_key;
    }

    public long getHashCode() {
        return this.zobrist_key;
    }


    private long initHashCode() {
        long hash = 0L;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0) {
                    int k = board[i][j]; // team occupying spot
                    hash = hash ^ Zobrist.board[i][j][k];
                }
            }
        }
        hash = hash ^ Zobrist.turn[turn];
        hash = hash ^ Zobrist.redPoints[redScore];
        hash = hash ^ Zobrist.blackPoints[blackScore];
        return hash;
    }

    private void updateHashCode(State parent) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != parent.getBoard()[i][j]) {
                    int k_parent = parent.getBoard()[i][j]; // team occupying spot
                    int k = board[i][j];
                    if (k_parent != 0) zobrist_key ^= Zobrist.board[i][j][k_parent];
                    if (k != 0) zobrist_key ^= Zobrist.board[i][j][k];
                }
            }
        }
        if (turn != parent.getTurn()) {
            zobrist_key ^= Zobrist.turn[parent.getTurn()];
            zobrist_key ^= Zobrist.turn[turn];
        }
        if (redScore != parent.getScore(PLAYER1)) {
            zobrist_key ^= Zobrist.redPoints[parent.getScore(PLAYER1)];
            zobrist_key ^= Zobrist.redPoints[redScore];
        }
        if (blackScore != parent.getScore(PLAYER2)) {
            zobrist_key ^= Zobrist.blackPoints[parent.getScore(PLAYER2)];
            zobrist_key ^= Zobrist.blackPoints[blackScore];
        }
    }



    public int[][] getBoard() {
        return board;
    }

    public void setBoardEntry(int row, int col, int team) {
        board[row][col] = team;
    }

    public void addPoint(int team) {
        if (team == PLAYER1) {
            redScore++;
            unplacedRed++;
        } else {
            blackScore++;
            unplacedBlack++;
        }
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getScoreLimit() {
        return scoreLimit;
    }

    public void setScoreLimit(int scoreLimit) {
        this.scoreLimit = scoreLimit;
    }

    public void addUnPlaced(int team) {
        if (team == PLAYER1) unplacedRed++;
        else unplacedBlack++;
    }

    public void removeUnPlaced(int team) {
        if (team == PLAYER1) unplacedRed--;
        else unplacedBlack--;
    }

    public int getUnplaced(int team) {
        if (team == PLAYER1) return unplacedRed;
        else return unplacedBlack;
    }

    // Get a list of pieces/points from this state
    public ArrayList<Point> getPieces(int team) {
        ArrayList<Point> entries = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == team) {
                    entries.add(new Point(j, i));
                }
            }
        }
        if (getUnplaced(team) > 0) {
            entries.add(new Point(-1, -1));
        }
        return entries;
    }

    public int getScore(int team) {
        return (team == PLAYER1) ? redScore : blackScore;
    }

    public ArrayList<State> getChildren() {
        ArrayList<State> children = new ArrayList<>();
        for (Move m : getLegalMoves()) {
            State child = new State(this, m);
            children.add(child);
        }
        return children;
    }

    @Override
    public FFTState getNextState(FFTMove move) {
        return getNextState((Move) move);
    }

    public State getNextState(Move m) {
        return new State(this, m);
    }

    // Creates and/or returns a list of new state objects which correspond to the children of the given state.
    public ArrayList<Move> getLegalMoves() {
        if (legalMoves != null) return legalMoves;
        legalMoves = Logic.legalMoves(turn, this);
        return legalMoves;
    }

    public HashSet<Literal> getLiterals() {
        HashSet<Literal> literals = new HashSet<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int pieceOcc = board[i][j];
                if (pieceOcc > 0) {
                    if (turn == PLAYER1)
                        literals.add(new Literal(i, j, pieceOcc, false));
                    else {
                        pieceOcc = (pieceOcc == 1) ? 2 : 1;
                        literals.add(new Literal(i, j, pieceOcc, false));
                    }
                }
            }
        }
        literals.add(new Literal("SL=" + scoreLimit));
        literals.add(new Literal("P1SCORE=" + redScore));
        literals.add(new Literal("P2SCORE=" + blackScore));
        return literals;
    }


    // Returns the material of a state, which is the value of the state based on various heuristics.
    // It checks the current turn, and outputs a positive number if it is good, or negative if bad
    public int getMaterial() {
        int score = 0;

        // Bonus for legal moves
        score += (getLegalMoves().size() * 2);

        // Bonus for being in front of opponent on your turn
        for (Point pR : getPieces(PLAYER1)) {
            for (Point pB : getPieces(PLAYER2)) {

                if (pR.x == pB.x && (pR.y - 1) == pB.y) {
                    score += 2;
                }
            }
        }
        // Win cycle bonus
        boolean bot = false;
        boolean mid = false;
        boolean top = false;
        int tempScore = 0;
        // CIRCLE
        for (Point p : getPieces(PLAYER1)) {
            if (p.x == 1) {
                if (p.y == 0) top = true;
                else if (p.y == 1) mid = true;
                else if (p.y == 2) bot = true;
            }
        }
        if (mid && (top || bot)) {
            tempScore += 20;
        }
        if (top && mid && bot) tempScore += 100;
        score += (turn == PLAYER1) ? tempScore : -tempScore;

        bot = false;
        mid = false;
        top = false;
        tempScore = 0;
        // CROSS
        for (Point p : getPieces(PLAYER2)) {
            if (p.x == 1) {
                if (p.y == 3) top = true;
                else if (p.y == 2) mid = true;
                else if (p.y == 1) bot = true;
            }
        }
        if (mid && (top || bot)) {
            tempScore += 20;
        }
        if (top && mid && bot) tempScore += 100;
        score += (turn == PLAYER2) ? tempScore : -tempScore;
        return score;
    }
}
