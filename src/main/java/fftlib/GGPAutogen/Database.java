package fftlib.GGPAutogen;

import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.*;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Database {
    private static HashMap<MachineState, GGPMapping> lookupTable;
    private static HashMap<MachineState, Set<Move>> optimalMovesTable = new HashMap<>();

    static void initialize(HashMap<MachineState, GGPMapping> solution) {
        lookupTable = solution;
    }

    public static GGPMapping queryState(MachineState ms) {
        return lookupTable.get(ms);
    }

    public static Set<Move> optimalMoves(MachineState ms) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
        if (optimalMovesTable.get(ms) != null)
            return optimalMovesTable.get(ms);
        Set<Move> optimalMoves = new HashSet<>();
        if (GGPManager.isTerminal(ms)) {
            optimalMovesTable.put(ms, optimalMoves);
            return optimalMoves;
        }

        GGPMapping mapping = queryState(ms);
        if (mapping.move == null) { // There is no moves in this state
            optimalMovesTable.put(ms, optimalMoves);
            return optimalMoves;
        }

        Role r = GGPManager.getRole(ms);
        MachineState next = GGPManager.getNextState(ms, mapping.move);

        // In case of game over in next state
        if (GGPManager.isTerminal(next)) {
            List<Integer> winners = GGPManager.getPlayerWinners(next);
            if (winners == null) { // should not happen
                optimalMovesTable.put(ms, optimalMoves);
                return optimalMoves;
            }

            for (Move m : GGPManager.getLegalMoves(ms, r)) {
                MachineState child = GGPManager.getNextState(ms, m);
                List<Integer> childWinners = GGPManager.getPlayerWinners(child);
                if (childWinners == null) { // game is still going
                    if (winners.contains(queryState(child).getWinner()))
                        optimalMoves.add(m);
                    continue;
                }
                if (winners.equals(childWinners))
                    optimalMoves.add(m);
            }
            optimalMovesTable.put(ms, optimalMoves);
            return optimalMoves;
        }

        int bestMoveWinner = queryState(next).getWinner();
        int team = GGPManager.roleToPlayer(r);
        for (Move m : GGPManager.getLegalMoves(ms, r)) {
            MachineState child = GGPManager.getNextState(ms, m);
            if (GGPManager.isTerminal(child)) { // Game is stuck
                if (team == PLAYER1 && bestMoveWinner == PLAYER2)
                    optimalMoves.add(m);
                else if (team == PLAYER2 && bestMoveWinner == PLAYER1)
                    optimalMoves.add(m);
                continue;
            }
            int childWinner = queryState(child).getWinner();
            if (team == PLAYER1) {
                if (bestMoveWinner == childWinner)
                    optimalMoves.add(m);
                else if (bestMoveWinner == PLAYER2)
                    optimalMoves.add(m);
            } else {
                if (bestMoveWinner == childWinner)
                    optimalMoves.add(m);
                else if (bestMoveWinner == PLAYER1)
                    optimalMoves.add(m);
            }
        }
        optimalMovesTable.put(ms, optimalMoves);
        return optimalMoves;
    }
}
