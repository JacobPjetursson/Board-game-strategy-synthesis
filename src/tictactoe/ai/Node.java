package tictactoe.ai;

import fftlib.game.FFTMove;
import fftlib.game.FFTNode;
import tictactoe.game.Logic;
import tictactoe.game.Move;
import tictactoe.game.State;

import java.util.ArrayList;


public class Node implements FFTNode {
    private State state;

    // Starting Root state
    public Node(State startState) {
        this.state = new State(startState);
    }

    // Non-root state
    private Node(Node parent, Move m) {
        this.state = new State(parent.state);
        this.state.setMove(m);
        Logic.doTurn(m, this.state);
    }

    // Duplicate constructor, for "root" state
    public Node(Node node) {
        this.state = new State(node.state);
        this.state.setMove(node.state.getMove());
    }

    @Override
    public Node getNextNode(FFTMove move) {
        return getNextNode((Move) move);
    }

    public Node getNextNode(Move m) {
        return new Node(this, m);
    }

    public ArrayList<Node> getChildren() {
        ArrayList<Node> children = new ArrayList<>();
        for (Move m : state.getLegalMoves()) {
            Node child = new Node(this, m);
            children.add(child);
        }
        return children;
    }


    public State getState() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;
        Node node = (Node) obj;
        return this == node || this.state.equals(node.state);
    }

    @Override
    public int hashCode() {
        int result = this.state.hashCode();
        //result += Arrays.deepHashCode(this.reflect().board);
        return result * 31;

    }
}
