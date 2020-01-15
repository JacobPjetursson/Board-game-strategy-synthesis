package fftlib.GGPAutogen;


import fftlib.FFTManager;
import kulibrat.game.State;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.prover.aima.knowledge.KnowledgeBase;
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

        boolean debug = false;
        if (debug) {
            testGameRules(ms);
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

    private boolean testGameRules(MachineState ms) throws MoveDefinitionException {
        State testState = new State();
        for (GdlSentence sentence : ms.getContents()) {
            String s = sentence.toString();
            if (s.contains("pieces_left redplayer ")) {
                String pieces_red = Character.toString(s.split("pieces_left redplayer ")[1].charAt(0));
                testState.setUnplaced(PLAYER1, Integer.parseInt(pieces_red));
            }
            else if (s.contains("pieces_left blackplayer ")) {
                String pieces_black = Character.toString(s.split("pieces_left blackplayer ")[1].charAt(0));
                testState.setUnplaced(PLAYER2, Integer.parseInt(pieces_black));
            }
            else if (s.contains("score redplayer ")) {
                String score_red = Character.toString(s.split("score redplayer ")[1].charAt(0));
                testState.setScore(PLAYER1, Integer.parseInt(score_red));
            }
            else if (s.contains("score blackplayer ")) {
                String score_black = Character.toString(s.split("score blackplayer ")[1].charAt(0));
                testState.setScore(PLAYER1, Integer.parseInt(score_black));
            }
            else if (s.contains("control redplayer")) {
                testState.setTurn(PLAYER1);
            }
            else if (s.contains("control blackplayer")) {
                testState.setTurn(PLAYER2);
            }
            else if (s.contains("cell ")) {
                String cellInfo = s.split("cell ")[1];
                String col = Character.toString(cellInfo.charAt(0));
                String row = Character.toString(cellInfo.charAt(2));
                String team = cellInfo.substring(4);
                if (team.startsWith("red"))
                    testState.setBoardEntry(Integer.parseInt(row) - 1, Integer.parseInt(col) - 1, PLAYER1);
                else if (team.startsWith("black"))
                    testState.setBoardEntry(Integer.parseInt(row) - 1, Integer.parseInt(col) - 1, PLAYER2);
            }
        }
        List<Move> legalMoves = GGPManager.getLegalMoves(ms, GGPManager.getRole(ms));
        Set<kulibrat.game.Move> kMoves = new HashSet<>();
        for (Move move : legalMoves) {
            String m = move.toString();
            if (m.contains("add black ")) {
                String add = m.split("add black ")[1];
                int newCol = Integer.parseInt(Character.toString(add.charAt(0))) - 1;
                int newRow = Integer.parseInt(Character.toString(add.charAt(2))) - 1;
                kMoves.add(new kulibrat.game.Move(-1, -1, newRow, newCol, PLAYER2));
            }
            else if (m.contains("add red ")) {
                String add = m.split("add red ")[1];
                int newCol = Integer.parseInt(Character.toString(add.charAt(0))) - 1;
                int newRow = Integer.parseInt(Character.toString(add.charAt(2))) - 1;
                kMoves.add(new kulibrat.game.Move(-1, -1, newRow, newCol, PLAYER1));
            }
            else if (m.contains("remove black ")) {
                String remove = m.split("remove black ")[1];
                int oldCol = Integer.parseInt(Character.toString(remove.charAt(0))) - 1;
                int oldRow = Integer.parseInt(Character.toString(remove.charAt(2))) - 1;
                kMoves.add(new kulibrat.game.Move(oldRow, oldCol, -1, -1, PLAYER2));
            }
            else if (m.contains("remove red ")) {
                String remove = m.split("remove red ")[1];
                int oldCol = Integer.parseInt(Character.toString(remove.charAt(0))) - 1;
                int oldRow = Integer.parseInt(Character.toString(remove.charAt(2))) - 1;
                kMoves.add(new kulibrat.game.Move(oldRow, oldCol, -1, -1, PLAYER1));
            }
            else if (m.contains("move black ")) {
                String moveStr = m.split("move black ")[1];
                int oldCol = Integer.parseInt(Character.toString(moveStr.charAt(0))) - 1;
                int oldRow = Integer.parseInt(Character.toString(moveStr.charAt(2))) - 1;
                int newCol = Integer.parseInt(Character.toString(moveStr.charAt(4))) - 1;
                int newRow = Integer.parseInt(Character.toString(moveStr.charAt(6))) - 1;
                kMoves.add(new kulibrat.game.Move(oldRow, oldCol, newRow, newCol, PLAYER2));
            } else if (m.contains("move red ")) {
                String moveStr = m.split("move red ")[1];
                int oldCol = Integer.parseInt(Character.toString(moveStr.charAt(0))) - 1;
                int oldRow = Integer.parseInt(Character.toString(moveStr.charAt(2))) - 1;
                int newCol = Integer.parseInt(Character.toString(moveStr.charAt(4))) - 1;
                int newRow = Integer.parseInt(Character.toString(moveStr.charAt(6))) - 1;
                kMoves.add(new kulibrat.game.Move(oldRow, oldCol, newRow, newCol, PLAYER1));
            }
        }
        HashSet<kulibrat.game.Move> moveSet = new HashSet<>(testState.getLegalMoves());
        if (maxSentences != ms.getContents().size() || !kMoves.equals(moveSet)) {
            System.out.println("ERROR");
            System.out.println("MACHINESTATE: " + ms);
            System.out.println("TESTSTATE: " + testState);
            System.out.println("TESTSTATE PIECES LEFT: " + testState.getUnplaced(PLAYER1) + " " + testState.getUnplaced(PLAYER2));
            System.out.println("MACHINESTATE MOVES: " + legalMoves);
            System.out.println("Translated moves: " + kMoves);
            System.out.println("legal moves: " + testState.getLegalMoves());
            System.out.println();
        }
        return true;
    }

}

