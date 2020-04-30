// This file provides additional functionality to the FFTSolution class
// The solution can be stored and retrieved from a database

package kulibrat.misc;

import fftlib.game.FFTMove;
import fftlib.FFTSolution;
import fftlib.game.FFTNode;
import fftlib.game.NodeMapping;
import kulibrat.ai.Minimax.LookupTableMinimax;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.game.Node;
import misc.Config;
import misc.Globals;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static misc.Globals.PLAYER2;


public class Database extends FFTSolution {
    public static Connection dbConnection;

    // Connects to the database. If the table in question is incomplete or missing, show a pane to allow creating the DB on the spot.
    public static boolean connectAndVerify() {
        System.out.println("Connecting to database. This might take some time");
        try {
            dbConnection = DriverManager.getConnection(
                    Config.DB_PATH);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Connection successful");

        String tableName = "plays_" + Globals.SCORELIMIT;
        long key = new Node(new Node()).getZobristKey();
        boolean error = false;
        // Try query to check for table existance
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("select oldRow, oldCol, newRow, newCol, team, score from "
                    + tableName + " where id=" + key);
            if (!resultSet.next()) {
                System.err.println("The database table '" + tableName + "' is incomplete.");
                error = true;
            }
            statement.close();
        } catch (SQLException e) {
            System.out.println("Table '" + tableName + "' does not exist in the database!");
            error = true;
        }
        if (error) {
            showOverwritePane();
            return false;
        }
        return true;
    }

    public static boolean connect() {
        System.out.println("Connecting to database. This might take some time");
        try {
            dbConnection = DriverManager.getConnection(
                    Config.DB_PATH);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Connection successful");
        return true;
    }

    public static void close() {
        try {
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Outputs a list of the best plays from a given node. Checks through the children of a node to find the ones
    // which have the least amount of turns to terminal for win, or most for loss.
    public static ArrayList<Move> bestMoves(Node n) {
        ArrayList<Move> bestMoves = new ArrayList<>();
        if (Logic.gameOver(n))
            return bestMoves;
        NodeMapping mapping = queryState(n);
        int bestScore = 0;
        if (!Logic.gameOver((Node)n.getNextNode(mapping.getMove()))) {
            bestScore = queryState((Node)n.getNextNode(mapping.getMove())).getScore();
        }
        for (FFTNode child : n.getChildren()) {
            FFTMove m = child.getMove();

            FFTNode node = n.getNextNode(m);
            if (Logic.gameOver((Node)node)) {
                if (Logic.getWinner((Node)node) == m.getTeam())
                    bestMoves.add((Move)m);
            } else if (queryState((Node)child).getScore() == bestScore) {
                bestMoves.add((Move)m);
            }
        }
        return bestMoves;
    }

    // Fetches the best play corresponding to the input node
    public static NodeMapping queryState(Node n) {
        // todo
        //HashMap<Node, StateMapping> table = (HashMap<Node, StateMapping>) FFTSolution.getLookupTable();
        HashMap<Node, NodeMapping> table = null;
        if (table != null && !table.isEmpty())
            return table.get(n);

        NodeMapping play = null;
        String tableName = "plays_" + Globals.SCORELIMIT;
        Long key = n.getZobristKey();
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("select oldRow, oldCol, newRow, newCol, team, score from "
                    + tableName + " where id=" + key);
            while (resultSet.next()) {
                Move move = new Move(resultSet.getInt(1), resultSet.getInt(2),
                        resultSet.getInt(3), resultSet.getInt(4), resultSet.getInt(5));
                int score = resultSet.getInt(6);
                play = new NodeMapping(move, score, 0);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (play == null) {
            System.err.println("PLAY DOES NOT EXIST IN DATABASE!");
        }
        return play;
    }

    // Outputs a string which is the amount of turns to a terminal node, based on a score from the database entry
    public static String turnsToTerminal(int turn, Node n) {
        int score = queryState(n).score;
        if (score == 0) {
            return "∞";
        }
        if (score > 0) {
            if (turn == PLAYER2) {
                return "" + (-2000 + score);
            } else {
                return "" + (2000 - score);
            }
        } else {
            if (turn == PLAYER2) {
                return "" + (2000 + score);
            } else {
                return "" + (-2000 - score);
            }
        }
    }

    // Opens the overwrite pane for DB
    private static void showOverwritePane() {
        /*
        Stage newStage = new Stage();
        String labelText = "The DB Table for this score limit has not been built.\n" +
                "       Do you want to build it? It will take a while";
        newStage.setScene(new Scene(new OverwriteDBDialog(labelText), 500, 150));
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setOnCloseRequest(Event::consume);
        newStage.show();

         */
    }

    public static void fillLookupTable(HashMap<Long, NodeMapping> lookupTable) throws SQLException {
        System.out.println("Inserting data into table. This will take some time");
        String tableName = "plays_" + Globals.SCORELIMIT;
        long startTime = System.currentTimeMillis();
        dbConnection.createStatement().execute("truncate table " + tableName);

        final int batchSize = 1000;
        int count = 0;
        PreparedStatement stmt = dbConnection.prepareStatement("insert into " + tableName + " values (?, ?, ?, ?, ?, ?, ?)");
        for (Map.Entry<Long, NodeMapping> entry : lookupTable.entrySet()) {
            Long key = entry.getKey();
            NodeMapping value = entry.getValue();
            Move move = (Move) value.getMove();
            stmt.setLong(1, key);
            stmt.setInt(2, move.oldRow);
            stmt.setInt(3, move.oldCol);
            stmt.setInt(4, move.newRow);
            stmt.setInt(5, move.newCol);
            stmt.setInt(6, move.team);
            stmt.setInt(7, value.score);

            stmt.addBatch();
            if (++count % batchSize == 0) {
                stmt.executeBatch();
            }
        }
        stmt.executeBatch();
        stmt.close();
        System.out.println("Data inserted successfully. Time spent: " + (System.currentTimeMillis() - startTime));
    }

    // Creates lookup table
    public static void createLookupTable() {
        System.out.println("Connecting to database. This might take some time");
        if (!connect())
            return;
        // Creating the table, if it does not exist already
        String tableName = "plays_" + Globals.SCORELIMIT;
        try {
            dbConnection.createStatement().execute("create table " + tableName +
                    "(id bigint primary key, oldRow smallint, oldCol smallint, newRow smallint, newCol smallint, team smallint, score smallint)");
        } catch (SQLException e) {
            System.out.println("Table '" + tableName + "' exists in the DB");
        }
    }

    // Builds the DB
    public static void buildLookupDB() {
        Node node = new Node();
        createLookupTable();
        try {
            fillLookupTable(LookupTableMinimax.solveGame(node));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
