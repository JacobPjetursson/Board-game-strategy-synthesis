package fftlib.GGPAutogen;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.HashMap;
import java.util.List;


public class Solver {
    private static int CURR_MAX_DEPTH;
    private static int winNodes, drawNodes, lossNodes;
    private static HashMap<MachineState, GGPMapping> lookupTable;
    private static Role p1Role, p2Role, solverRole;
    private static Move noop;

    private static int maxSentences; // for test purposes


    public Solver() {
        lookupTable = new HashMap<>();
        p1Role = GGPManager.p1role;
        p2Role = GGPManager.p2role;
        solverRole = p1Role; // role needs to be consistent when solving, doesn't matter which is chosen
        noop = GGPManager.noop;
    }

    private Role getStateRole(MachineState ms) {
        try {
            List<Move> moves = GGPManager.getLegalMoves(ms, p1Role);
            return (moves.size() == 1 && moves.get(0).equals(noop)) ? p2Role : p1Role;
        } catch(MoveDefinitionException e) {
            return p2Role;
        }
    }


    public HashMap<MachineState, GGPMapping> solve() throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        System.out.println("Solving the game");
        long timeStart = System.currentTimeMillis();
        boolean done = false;
        maxSentences = GGPManager.getInitialState().getContents().size();
        int doneCounter = 0;
        while (!done) {
            int prevSize = lookupTable.size();
            int prevWinNodes = winNodes;
            int prevDrawNodes = drawNodes;
            int prevLossNodes = lossNodes;
            winNodes = 0;
            drawNodes = 0;
            lossNodes = 0;
            CURR_MAX_DEPTH += 1;
            System.out.println("Minimax iterative depth: " + CURR_MAX_DEPTH + ". Size of solution: " + lookupTable.size());
            MachineState initialState = GGPManager.getInitialState();
            minimax(initialState.clone(), CURR_MAX_DEPTH);


            if (lookupTable.size() == prevSize &&
                    prevWinNodes == winNodes && prevDrawNodes == drawNodes && prevLossNodes == lossNodes) {
                done = true;
            }
        }

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on solving game: " + timeSpent);
        return lookupTable;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private GGPMapping minimax(MachineState ms, int depth) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        // TODO - crucial to speed up this process
        Move bestMove = null;
        Role stateRole = getStateRole(ms); // TODO - scale to other games and more than 2 players
        int bestScore = (stateRole.equals(solverRole)) ? Integer.MIN_VALUE : Integer.MAX_VALUE; // TODO - same as above
        int score;
        if (GGPManager.isTerminal(ms) || depth == 0) {
            return new GGPMapping(bestMove, heuristic(ms), depth, stateRole);
        }
        GGPMapping mapping = lookupTable.get(ms);
        if (mapping != null && depth <= mapping.depth) {
            return mapping;
        }


        for (Move move : GGPManager.getLegalMoves(ms, stateRole)) {
            MachineState child = GGPManager.getNextState(ms, move);
            score = minimax(child, depth - 1).score;
            if (score > 1000)
                score--;
            else
                score++;

            if (stateRole.equals(solverRole)) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }
        if (mapping == null || depth > mapping.depth) {
            lookupTable.put(ms,
                    new GGPMapping(bestMove, bestScore, depth, stateRole));
        }
        if (bestScore > 1000) {
            winNodes++;
        } else {
            if (bestScore < -1000)
                lossNodes++;
            else
                drawNodes++;
        }
        return new GGPMapping(bestMove, bestScore, depth, stateRole);
    }

    // Heuristic function which values a win with 2000, and -2000 for a loss. All other nodes are 0
    private int heuristic(MachineState ms) throws GoalDefinitionException {
        if (GGPManager.isTerminal(ms)) {
            List<Role> winners = GGPManager.getWinners(ms);
            if (winners.size() == GGPManager.getRoles().size())
                return 0;
            if (winners.contains(solverRole)) // TODO - scale to several players
                return 2000;
            else
                return -2000;
        }
        return 0;
    }
}

