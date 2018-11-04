package FFT;
import ai.Minimax.Node;
import game.Logic;
import game.Move;
import game.State;
import game.StateAndMove;
import misc.Config;
import misc.Database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import static misc.Config.BLACK;
import static misc.Config.RED;

public class FFT {
    public String name;
    public ArrayList<RuleGroup> ruleGroups;
    StateAndMove failingPoint = null;

    public FFT(String name) {
        this.name = name;
        ruleGroups = new ArrayList<>();
    }


    public void addRuleGroup(RuleGroup ruleGroup) {
        ruleGroups.add(ruleGroup);
        FFTManager.save();
    }

    public boolean verify(int team, boolean wholeFFT) {
        Node initialNode = new Node(new State());
        LinkedList<Node> frontier = new LinkedList<>();
        HashSet<Node> closedSet = new HashSet<>();
        frontier.add(initialNode);
        int opponent = (team == RED) ? BLACK : RED;
        // Check if win or draw is even possible
        int score = Database.queryPlay(initialNode).score;
        if (team == RED && score < 0) {
            System.out.println("A perfect player black has won from start of the game");
            return false;
        } else if (team == BLACK && score > 0) {
            System.out.println("A perfect player red has won from the start of the game");
            return false;
        }

        while (!frontier.isEmpty()) {
            Node node = frontier.pop();
            if (Logic.gameOver(node.getState())) {
                if (Logic.getWinner(node.getState()) == opponent) {
                    // Should not hit this given initial check
                    System.out.println("No chance of winning vs. perfect player");
                    return false;
                }
            }
            else if (team != node.getState().getTurn()) {
                for (Node child : node.getChildren())
                    if (!closedSet.contains(child)) {
                        closedSet.add(child);
                        frontier.add(child);
                    }
            }
            else {
                Move move = makeMove(node);
                ArrayList<Move> nonLosingPlays = Database.nonLosingPlays(node);
                // If move is null, check that all possible (random) moves are ok
                if (move == null) {
                    for (Move m : node.getState().getLegalMoves()) {
                        if (nonLosingPlays.contains(m)) {
                            Node nextNode = node.getNextNode(m);
                            if (!closedSet.contains(nextNode)) {
                                closedSet.add(nextNode);
                                frontier.add(nextNode);
                            }
                        } else if (wholeFFT) {
                            System.out.println("FFT did not apply to certain state, random move lost you the game");
                            failingPoint = new StateAndMove(node.getState(), m, true);
                            return false;
                        }
                    }
                }
                else if (!nonLosingPlays.contains(move)) {
                    System.out.println("FFT applied, but its move lost you the game");
                    failingPoint = new StateAndMove(node.getState(), move, false);
                    return false;
                }
                else {
                    Node nextNode = node.getNextNode(move);
                    if (!closedSet.contains(nextNode)) {
                        closedSet.add(nextNode);
                        frontier.add(nextNode);
                    }
                }
            }
        }
        return true;
    }

    private Move makeMove(Node node) {
        State state = node.getState();
        for (RuleGroup ruleGroup : ruleGroups) {
            for (Rule rule : ruleGroup.rules) {
                for(int symmetry : Config.SYMMETRY) {
                    if (rule.applies(state, symmetry)) {
                        Action action = rule.action.applySymmetry(symmetry);
                        Move move = action.getMove();
                        move.team = state.getTurn();
                        if (Logic.isLegalMove(state, move)) {
                            return move;
                        }
                    }
                }
            }
        }
        return null;
    }
}
