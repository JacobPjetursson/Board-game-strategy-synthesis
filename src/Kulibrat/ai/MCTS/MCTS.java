package ai.MCTS;

import ai.AI;
import ai.Minimax.Minimax;
import game.Logic;
import game.Move;
import game.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MCTS extends AI {
    private boolean debug = true;
    // PARAMETERS
    private long calculationTime;
    private int max_moves = 300;
    private boolean useMinimax = false;
    private int minimaxDepth = 2;
    private Minimax minimax;
    private int simulationDepth = 0;
    private Node curr_node; // used to keep track of the current game state/node, to save the statistics

    public MCTS(State startState, int team, int calculationTime) {
        super(team);
        this.calculationTime = calculationTime;
        minimax = new Minimax(team, calculationTime);
        minimax.setUseTranspo(false);
        curr_node = new Node(startState);
    }

    // Main function for MCTS, which consist of all four steps in the algorithm.
    private void run(Node startNode) {
        Node sim_node = startNode;
        Node terminalNode = null;
        int winner = 0; // 1 is red, 2 is black
        boolean playOut = false;
        sim_node.incrementPlays();
        for (int i = 0; i < max_moves; i++) {
            if (i > simulationDepth) {
                simulationDepth = i;
                if (debug && simulationDepth > (max_moves - 50)) {
                    System.out.println("Cycle spotted!: ");
                    System.out.println("State: " + sim_node);
                    System.out.println("Board: " + Arrays.deepToString(sim_node.getState().getBoard()));
                    System.out.println("Turn: " + sim_node.getState().getTurn());
                    System.out.println("Legal moves size: " + sim_node.getState().getLegalMoves().size());
                    System.out.println("State plays: " + sim_node.getPlays());
                    System.out.println("Depth: " + simulationDepth);
                    System.out.println();
                }
            }
            // Break loop if game is over
            if (Logic.gameOver(sim_node.getState())) {
                winner = Logic.getWinner(sim_node.getState());
                if (terminalNode == null) terminalNode = sim_node;
                break;
            }
            if (playOut) {
                if (useMinimax) {
                    // Shallow minimax search as rollout
                    minimax.setTeam(sim_node.getState().getTurn());
                    ai.Minimax.Node node = new ai.Minimax.Node(sim_node.getState());
                    Move move = minimax.minimax(node, minimaxDepth, Integer.MIN_VALUE,
                            Integer.MAX_VALUE, System.currentTimeMillis()).move;

                    sim_node = sim_node.getNextNode(move);
                } else {
                    // Random playout, no node expansion
                    int r = new Random().nextInt(sim_node.getState().getLegalMoves().size());
                    Move m = sim_node.getState().getLegalMoves().get(r);
                    sim_node = sim_node.getNextNode(m);
                }
                continue;
            }
            ArrayList<Node> unexplored = new ArrayList<>();
            boolean containsAll = true;
            for (Node child : sim_node.getChildren()) {
                if (child.getPlays() == 0) {
                    containsAll = false;
                    unexplored.add(child);
                }
            }
            Node bestNode = null;
            if (containsAll) {
                double bestUCB = 0.0;
                for (Node child : sim_node.getChildren()) {
                    if (child.UCB(1) >= bestUCB) {
                        bestUCB = child.UCB(1);
                        bestNode = child;
                    }
                }
            } else {
                int rIndex = new Random().nextInt(unexplored.size());
                bestNode = unexplored.get(rIndex);
                playOut = true;
                terminalNode = bestNode;
            }
            sim_node = bestNode;
        }
        // Game is over, backpropagating
        terminalNode.backPropagate(winner);
    }

    public Move makeMove(State currState) {
        initialize(curr_node, calculationTime);

        Move move;
        if (currState.getLegalMoves().size() == 1) {
            move = currState.getLegalMoves().get(0);
        } else move = getBestMove(curr_node);

        return move;
    }

    // Called when MCTS makes its final move based on the move with the highest chances of winning
    private Move getBestMove(Node node) {
        Move bestMove = null;
        double best_val = Integer.MIN_VALUE;
        for (Node child : node.getChildren()) {
            double payOff = (child.getPlays() == 0) ? 0 : (child.getWins() / child.getPlays());
            if (child.getPlays() == 0) {
                System.out.println("No records for play: " + "oldRow: " + child.getState().getMove().oldRow + ", oldCol: "
                        + child.getState().getMove().oldCol + ", newRow: " + child.getState().getMove().newRow + ", newCol: " +
                        child.getState().getMove().newCol + ", board: " + Arrays.deepToString(child.getState().getBoard()));
            } else {
                System.out.println("payOff: " + payOff + ", play: " + "oldRow: " + child.getState().getMove().oldRow + ", oldCol: "
                        + child.getState().getMove().oldCol + ", newRow: " + child.getState().getMove().newRow + ", newCol: " +
                        child.getState().getMove().newCol + ", plays: " + child.getPlays() + ", wins: " + child.getWins());
            }
            if (payOff > best_val) {
                best_val = payOff;
                bestMove = child.getState().getMove();
            }
        }
        System.out.println();
        return bestMove;
    }

    // Main loop of MCTS for statistic gathering
    private void initialize(Node startNode, long calculationTime) {
        int games = 0;
        simulationDepth = 0;
        startNode.setParent(null); // Deletes all the previous states by creating floating objects for garbage collection
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < calculationTime) {
            run(startNode);
            games++;
        }
        if (System.currentTimeMillis() - startTime > calculationTime + 250) {
            System.out.println("Memory issues spotted!");
        }
        System.out.println("Depth: " + simulationDepth);
        System.out.println("Games: " + games);
    }

    public void update(State state) {
        curr_node = curr_node.getNextNode(state.getMove());
    }
}
