package tictactoe.game;

public class NodeAndMove {
    public boolean random;
    private Node node;
    private Move move;
    private int turnNo;

    public NodeAndMove(Node node, Move move, int turnNo) {
        this.node = node;
        this.move = move;
        this.turnNo = turnNo;
    }

    // used for fft
    public NodeAndMove(Node node, Move move, boolean random) {
        this.node = node;
        this.move = move;
        this.random = random;
    }

    public Move getMove() {
        return move;
    }

    public Node getNode() {
        return node;
    }

    public int getTurnNo() {
        return turnNo;
    }
}
