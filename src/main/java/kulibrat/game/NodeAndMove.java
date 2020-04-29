package kulibrat.game;

public class NodeAndMove {
    private Node node;
    private Move move;
    private int turnNo;

    public NodeAndMove(Node node, Move move, int turnNo) {
        this.node = node;
        this.move = move;
        this.turnNo = turnNo;
    }

    public int getTurnNo() {
        return turnNo;
    }

    public Move getMove() {
        return move;
    }

    public Node getNode() {
        return node;
    }
}
