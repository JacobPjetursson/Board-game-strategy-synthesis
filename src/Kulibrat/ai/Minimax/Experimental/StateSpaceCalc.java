/**
 * This class calculates the state space of Kulibrat using Minimax
 */
package ai.Minimax.Experimental;

import ai.Minimax.MinimaxPlay;
import ai.Minimax.Node;
import ai.Minimax.Zobrist;
import game.Logic;
import game.Move;
import game.State;
import misc.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static misc.Config.BLACK;
import static misc.Config.RED;

public class StateSpaceCalc {
    private static HashMap<BoardConfig, MinimaxPlay> transTable = new HashMap<>();
    private static HashSet<BoardConfig> fullSpace = new HashSet<>();
    private static long legalMoves = 0;
    private static int team = RED;
    private static boolean findBranchFactor = true;

    private static void calcBoardPositions(State state) {
        fullSpace = produceStateSpace();
        iterativeDeepeningMinimax(state);
        System.out.println("FINAL STATE SPACE SIZE: " + transTable.size());
        printMissingStates(fullSpace);
        double avgBranchFactor = 0;
        if (findBranchFactor) avgBranchFactor = ((double) legalMoves / (double) transTable.size());
        System.out.println("AVG BRANCHING FACTOR: " + avgBranchFactor);
    }

    private static void iterativeDeepeningMinimax(State state) {
        int CURR_MAX_DEPTH = 0;
        boolean done = false;
        while (!done) {
            Node simNode = new Node(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            CURR_MAX_DEPTH++;
            int prevSize = transTable.size();
            minimax(simNode, CURR_MAX_DEPTH);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH);
            System.out.println("TABLE SIZE: " + transTable.size());
            if (transTable.size() == prevSize) done = true;
        }
    }

    public static MinimaxPlay minimax(Node node, int depth) {
        Move bestMove = null;
        int bestScore = (node.getState().getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (depth == 0) {
            return new MinimaxPlay(bestMove, heuristic(node.getState()), depth);
        }
        BoardConfig bc = new BoardConfig(node.getState());
        fullSpace.remove(bc);
        MinimaxPlay transpoPlay = transTable.get(bc);
        if (transpoPlay != null && depth <= transpoPlay.depth) {
            return transpoPlay;
        }

        for (Node child : node.getChildren()) {
            score = minimax(child, depth - 1).score;
            if (node.getState().getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
            }
        }
        if (transpoPlay == null || depth > transpoPlay.depth) {
            if (transpoPlay == null) if (findBranchFactor) legalMoves += node.getState().getLegalMoves().size();
            transTable.put(bc, new MinimaxPlay(bestMove, bestScore, depth));
        }

        return new MinimaxPlay(bestMove, bestScore, depth);
    }

    private static int heuristic(State state) {
        int opponent = (team == RED) ? BLACK : RED;
        int winner = Logic.getWinner(state);

        if (winner == team) return 1000;
        else if (winner == opponent) return -1000;
        return 0;
    }

    private static HashSet<BoardConfig> produceStateSpace() {
        HashSet<BoardConfig> fullSpace = new HashSet<>();
        int[] board = new int[12];
        // Empty board
        saveBoardConfig(board, RED, fullSpace);
        saveBoardConfig(board, BLACK, fullSpace);
        // Only black pieces
        placeBlackPieces(board, RED, fullSpace);
        placeBlackPieces(board, BLACK, fullSpace);
        // Mix of red and black pieces
        for (int turn = 1; turn <= 2; turn++) {
            for (int p1 = 0; p1 < board.length; p1++) {
                for (int p2 = p1; p2 < board.length; p2++) {
                    for (int p3 = p2; p3 < board.length; p3++) {
                        for (int p4 = p3; p4 < board.length; p4++) {
                            board = new int[12];
                            board[p1] = RED;
                            board[p2] = RED;
                            board[p3] = RED;
                            board[p4] = RED;
                            saveBoardConfig(board, turn, fullSpace);
                            placeBlackPieces(board, turn, fullSpace);
                        }
                    }
                }
            }
        }
        return fullSpace;
    }

    private static void placeBlackPieces(int[] origBoard, int turn, HashSet<BoardConfig> fullSpace) {
        int[] board = Arrays.copyOf(origBoard, origBoard.length);
        for (int b1 = 0; b1 < board.length; b1++) {
            for (int b2 = b1; b2 < board.length; b2++) {
                for (int b3 = b2; b3 < board.length; b3++) {
                    for (int b4 = b3; b4 < board.length; b4++) {
                        board = Arrays.copyOf(origBoard, origBoard.length);
                        if (board[b1] == 0) board[b1] = BLACK;
                        if (board[b2] == 0) board[b2] = BLACK;
                        if (board[b3] == 0) board[b3] = BLACK;
                        if (board[b4] == 0) board[b4] = BLACK;

                        saveBoardConfig(board, turn, fullSpace);
                    }
                }
            }
        }
    }

    private static void saveBoardConfig(int[] board, int turn, HashSet<BoardConfig> fullSpace) {
        int[][] gameBoard = new int[4][3];

        for (int i = 0; i < board.length; i++) {
            gameBoard[i % 4][i / 4] = board[i];
        }
        BoardConfig bc = new BoardConfig(gameBoard, turn);
        fullSpace.add(bc);
    }

    private static void printMissingStates(HashSet<BoardConfig> fullSpace) {
        System.out.println("AMOUNT OF STATES NOT IN THE STATESPACE: " + fullSpace.size());
        System.out.println("PRINTING THE STATES NOT IN THE STATESPACE");
        for (BoardConfig bc : fullSpace) {
            for (int i = 0; i < bc.board.length; i++) {
                for (int j = 0; j < bc.board[i].length; j++) {
                    System.out.print(bc.board[i][j] + " ");
                }
                System.out.println();
            }
            String turn = (bc.getTurn() == RED) ? "Red" : "Black";
            System.out.println("STATE TURN: " + turn);
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Zobrist.initialize();
        Config.SCORELIMIT = 15;
        State state = new State();
        calcBoardPositions(state);
    }


    private static class BoardConfig {
        int turn;
        int[][] board;

        BoardConfig(int[][] board, int turn) {
            this.board = new int[board.length][];

            for (int i = 0; i < board.length; i++) {
                this.board[i] = Arrays.copyOf(board[i], board[i].length);
            }
            this.turn = turn;
        }

        BoardConfig(State state) {
            board = new int[state.getBoard().length][];
            for (int i = 0; i < state.getBoard().length; i++) {
                board[i] = Arrays.copyOf(state.getBoard()[i], state.getBoard()[i].length);
            }
            turn = state.getTurn();
        }

        int getTurn() {
            return turn;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BoardConfig)) return false;
            BoardConfig cstate = (BoardConfig) obj;
            return this == cstate || turn == cstate.getTurn() &&
                    Arrays.deepEquals(board, cstate.board);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(board) + turn;
        }
    }
}

