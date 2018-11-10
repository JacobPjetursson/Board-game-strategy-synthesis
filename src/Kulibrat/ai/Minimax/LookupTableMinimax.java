package kulibrat.ai.Minimax;

import kulibrat.ai.AI;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.State;
import kulibrat.misc.Database;

import java.sql.SQLException;
import java.util.HashMap;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


public class LookupTableMinimax extends AI {
    private boolean useDB = true;
    private int CURR_MAX_DEPTH;
    private int unevaluatedNodes = 0;
    private HashMap<Long, MinimaxPlay> lookupTable;


    public LookupTableMinimax(int team, State state, boolean overwriteDB) {
        super(team);
        lookupTable = new HashMap<>();
        if (useDB && overwriteDB) {
            this.team = PLAYER1;
            System.out.println("Rebuilding lookup table. This will take some time.");
            buildLookupTable(state);
            try {
                Database.fillLookupTable(lookupTable);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.team = team;
        } else if (useDB) {
            Database.connectAndVerify();
        }
    }

    // This function fetches the best move from the DB, if it exists
    public Move makeMove(kulibrat.game.State state) {
        String teamstr = (team == PLAYER2) ? "CROSS" : "CIRCLE";
        System.out.println("Finding best play for " + teamstr);
        if (state.getLegalMoves().size() == 1) {
            return state.getLegalMoves().get(0);
        }
        // table lookup
        State simState = new State(state);
        MinimaxPlay play;
        if (useDB) {
            play = Database.queryPlay(simState);
        } else {
            play = iterativeDeepeningMinimax(state);
        }
        if (play == null) {
            System.err.println("DB Table is empty and needs to be rebuilt. Exiting");
            System.exit(0);
        }
        Move move = play.move;
        String winner = (play.score >= 1000) ? "CIRCLE" : (play.score == 0) ? "DRAW" : "CROSS";
        System.out.print("BEST PLAY:  " + "oldRow: " + move.oldRow +
                ", oldCol: " + move.oldCol + ", row: " + move.newRow + ", col: " + move.newCol +
                ", WINNER IS: " + winner);
        System.out.println(" in " + (play.score >= 1000 ? 2000 - play.score : (play.score == 0) ? "∞" : 2000 + play.score) + " moves!");
        return move;
    }

    // Runs an iterative deepening minimax as the exhaustive brute-force for the lookupDB. The data is saved in the transpo table
    private MinimaxPlay iterativeDeepeningMinimax(kulibrat.game.State state) {
        CURR_MAX_DEPTH = 0;
        boolean done = false;
        MinimaxPlay play = null;
        int doneCounter = 0;
        while (!done) {
            State simState = new State(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            int prevSize = lookupTable.size();
            int prevUnevaluatedNodes = unevaluatedNodes;
            unevaluatedNodes = 0;
            CURR_MAX_DEPTH += 1;
            play = minimax(simState, CURR_MAX_DEPTH);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH + ", LOOKUP TABLE SIZE: " + lookupTable.size() + ", UNEVALUATED NODES: " + unevaluatedNodes);
            if (lookupTable.size() == prevSize && unevaluatedNodes == prevUnevaluatedNodes) {
                System.out.println("State space explored, and unevaluated nodes unchanged between runs. I'm done");
                doneCounter++;
            } else
                doneCounter = 0;
            if (doneCounter == 2) done = true;

            if (Math.abs(play.score) >= 1000) {
                String player = (team == PLAYER1) ? "CIRCLE" : "CROSS";
                String opponent = (player.equals("CIRCLE")) ? "CROSS" : "CIRCLE";
                String winner = (play.score >= 1000) ? player : opponent;
                System.out.println("A SOLUTION HAS BEEN FOUND, WINNING STRAT GOES TO: " + winner);
            }
        }
        return play;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private MinimaxPlay minimax(State state, int depth) {
        Move bestMove = null;
        int bestScore = (state.getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (Logic.gameOver(state) || depth == 0) {
            return new MinimaxPlay(bestMove, heuristic(state), depth);
        }
        MinimaxPlay transpoPlay = lookupTable.get(state.getHashCode());
        if (transpoPlay != null && depth <= transpoPlay.depth) {
            return transpoPlay;
        }
        boolean evaluated = true;
        for (State child : state.getChildren()) {
            score = minimax(child, depth - 1).score;
            if (score > 1000) score--;
            else if (score < -1000) score++;
            else evaluated = false;

            if (state.getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = child.getMove();
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = child.getMove();
                }
            }
        }
        if (transpoPlay == null || depth > transpoPlay.depth) {
            lookupTable.put(state.getHashCode(),
                    new MinimaxPlay(bestMove, bestScore, depth));
        }
        if (!evaluated) unevaluatedNodes++;
        return new MinimaxPlay(bestMove, bestScore, depth);
    }

    // Heuristic function which values red with 2000 for a win, and -2000 for a loss. All other nodes are 0
    private int heuristic(kulibrat.game.State state) {
        int opponent = (team == PLAYER1) ? PLAYER2 : PLAYER1;
        if (Logic.gameOver(state)) {
            int winner = Logic.getWinner(state);
            if (winner == team)
                return 2000;
            else if (winner == opponent)
                return -2000;
        }
        return 0;
    }

    // This function builds the lookup table from scratch
    private void buildLookupTable(kulibrat.game.State state) {
        Database.createLookupTable();
        long startTime = System.currentTimeMillis();
        iterativeDeepeningMinimax(state);
        System.out.println("Lookup table successfully built. Time spent: " + (System.currentTimeMillis() - startTime));
    }
}
