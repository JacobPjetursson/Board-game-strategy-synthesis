package fftlib.GGP;


import fftlib.FFTManager;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.HashMap;
import java.util.List;


public class Solver {
    private static int CURR_MAX_DEPTH;
    private static int unevaluatedNodes = 0;
    private static HashMap<MachineState, GGPMapping> lookupTable;
    private static StateMachine sm;
    private static Role p1Role, p2Role, solverRole;
    private static Move noop;


    public Solver() {
        this.sm = FFTManager.sm;
        lookupTable = new HashMap<>();
        p1Role = FFTManager.xrole;
        p2Role = FFTManager.orole;
        solverRole = p1Role; // role needs to be consistent when solving, doesn't matter which is chosen
        noop = FFTManager.noop;
    }

    private Role getStateRole(MachineState ms) {
        try {
            List<Move> moves = sm.getLegalMoves(ms, p1Role);
            return (moves.size() == 1 && moves.get(0).equals(noop)) ? p2Role : p1Role;
        } catch(MoveDefinitionException e) {
            return p2Role;
        }
    }


    public HashMap<MachineState, GGPMapping> solve() throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        System.out.println("Solving the game");
        long timeStart = System.currentTimeMillis();
        boolean done = false;
        int doneCounter = 0;
        while (!done) {
            int prevSize = lookupTable.size();
            int prevUnevaluatedNodes = unevaluatedNodes;
            unevaluatedNodes = 0;
            System.out.println("Minimax iterative depth: " + CURR_MAX_DEPTH);
            CURR_MAX_DEPTH += 1;
            MachineState initialState = sm.getInitialState();
            minimax(initialState.clone(), CURR_MAX_DEPTH);


            if (lookupTable.size() == prevSize && unevaluatedNodes == prevUnevaluatedNodes) {
                doneCounter++;
            } else
                doneCounter = 0;
            if (doneCounter == 2) done = true;
        }

        double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
        System.out.println("Time spent on solving game: " + timeSpent);
        return lookupTable;
    }

    // Is called for every depth limit of the iterative deepening function. Classic minimax with no pruning
    private GGPMapping minimax(MachineState state, int depth) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        // TODO - crucial to speed up this process
        Move bestMove = null;
        Role stateRole = getStateRole(state); // TODO - scale to other games and more than 2 players
        int bestScore = (stateRole.equals(solverRole)) ? Integer.MIN_VALUE : Integer.MAX_VALUE; // TODO - same as above
        int score;
        if (sm.isTerminal(state) || depth == 0) {
            return new GGPMapping(bestMove, heuristic(state), depth, stateRole);
        }
        GGPMapping mapping = lookupTable.get(state);
        if (mapping != null && depth <= mapping.depth) {
            return mapping;
        }
        boolean evaluated = true;


        for (Move move : sm.getLegalMoves(state, stateRole)) {
            MachineState child = FFTManager.getNextState(state, move);
            score = minimax(child, depth - 1).score;
            if (score > 1000) score--;
            else {
                score++;
                evaluated = false;
            }

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
            lookupTable.put(state,
                    new GGPMapping(bestMove, bestScore, depth, stateRole));
        }
        if (!evaluated) unevaluatedNodes++;
        return new GGPMapping(bestMove, bestScore, depth, stateRole);
    }

    // Heuristic function which values a win with 2000, and -2000 for a loss. All other nodes are 0
    private int heuristic(MachineState state) throws GoalDefinitionException {
        if (sm.isTerminal(state)) {
            List<Role> winners = FFTManager.getWinners(state);
            if (winners.size() == sm.getRoles().size())
                return 0;
            if (winners.contains(solverRole)) // TODO - scale to several players
                return 2000;
            else
                return -2000;
        }
        return 0;
    }

}

