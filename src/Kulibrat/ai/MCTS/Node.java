package ai.MCTS;

import game.Logic;
import game.Move;
import game.State;

import java.util.ArrayList;

class Node {
    private State state;
    private ArrayList<Node> children;
    // UCB stuff
    private double plays = 0;
    private double wins = 0;
    private Node parent;

    // Starting Root state
    public Node(State startState) {
        this.state = new State(startState);
    }

    // Non-root state
    private Node(Node parent, Move m) {
        this.state = new State(parent.getState());
        this.parent = parent;
        Logic.doTurn(m, this.state);
        this.state.setMove(m);
    }

    // Duplicate constructor
    private Node(Node node) {
        this.state = new State(node.getState());
    }

    Node getNextNode(Move m) {
        if (children == null) {
            Node node = new Node(this);
            Logic.doTurn(m, node.getState());
            node.getState().setMove(m);
            return node;
        }
        for (Node child : children) {
            if (child.getState().getMove().equals(m)) return child;
        }
        return null;
    }

    public State getState() {
        return state;
    }

    double getPlays() {
        return plays;
    }

    double getWins() {
        return wins;
    }

    void incrementPlays() {
        plays++;
    }

    void setParent(Node node) {
        parent = node;
    }

    // Creates and/or returns a list of new state objects which correspond to the children of the given state.
    public ArrayList<Node> getChildren() {
        if (children != null) return children;
        children = new ArrayList<>();
        for (Move m : state.getLegalMoves()) {
            Node child = new Node(this, m);
            children.add(child);
        }
        return children;
    }

    double UCB(double explorationConstant) {
        double payOff = (wins / plays);
        return payOff + explorationConstant * Math.sqrt(Math.log(parent.plays) / plays);
    }

    void backPropagate(int winner) {
        Node node = this;
        while (node.parent != null) {
            node.plays++;
            if (node.getState().getMove().team == winner) {
                node.wins++;
            }
            node = node.parent;
        }
    }
}
