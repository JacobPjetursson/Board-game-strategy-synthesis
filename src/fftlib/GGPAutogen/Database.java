package fftlib.GGPAutogen;

import fftlib.FFTManager;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.*;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class Database {
    private static HashMap<MachineState, GGPMapping> lookupTable;

    static void initialize(HashMap<MachineState, GGPMapping> solution) {
        lookupTable = solution;
    }

    public static GGPMapping queryState(MachineState ms) {
        return lookupTable.get(ms);
    }

    public static Set<Move> nonLosingMoves(MachineState ms) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
        Set<Move> nonLosingMoves = new HashSet<>();
        StateMachine sm = FFTManager.sm;
        if (sm.isTerminal(ms)) {
            return nonLosingMoves;
        }

        GGPMapping mapping = queryState(ms);
        if (mapping.move == null) { // There is no moves in this state
            return nonLosingMoves;
        }

        Role stateRole = FFTManager.getStateRole(ms);
        MachineState next = FFTManager.getNextState(ms, mapping.move);

        // In case of game over in next state
        if (sm.isTerminal(next)) {
            List<Integer> winners = FFTManager.getPlayerWinners(next);
            if (winners == null) { // should not happen
                return nonLosingMoves;
            }

            for (Move m : sm.getLegalMoves(ms, stateRole)) {
                MachineState child = FFTManager.getNextState(ms, m);
                List<Integer> childWinners = FFTManager.getPlayerWinners(child);
                if (childWinners == null) { // game is still going
                    if (winners.contains(queryState(child).getWinner()))
                        nonLosingMoves.add(m);
                    continue;
                }
                if (winners.equals(childWinners))
                    nonLosingMoves.add(m);
            }
            return nonLosingMoves;
        }

        int bestMoveWinner = queryState(next).getWinner();
        int team = FFTManager.roleToPlayer(stateRole);
        for (Move m : sm.getLegalMoves(ms, stateRole)) {
            MachineState child = FFTManager.getNextState(ms, m);
            if (sm.isTerminal(child)) { // Game is stuck
                if (team == PLAYER1 && bestMoveWinner == PLAYER2)
                    nonLosingMoves.add(m);
                else if (team == PLAYER2 && bestMoveWinner == PLAYER1)
                    nonLosingMoves.add(m);
                continue;
            }
            int childWinner = queryState(child).getWinner();
            if (team == PLAYER1) {
                if (bestMoveWinner == childWinner)
                    nonLosingMoves.add(m);
                else if (bestMoveWinner == PLAYER2)
                    nonLosingMoves.add(m);
            } else {
                if (bestMoveWinner == childWinner)
                    nonLosingMoves.add(m);
                else if (bestMoveWinner == PLAYER1)
                    nonLosingMoves.add(m);
            }
        }
        return nonLosingMoves;
    }
}
