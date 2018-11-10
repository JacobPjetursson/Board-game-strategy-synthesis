package kulibrat.misc;

import fftlib.game.FFTDatabase;
import fftlib.game.FFTMinimaxPlay;
import fftlib.game.FFTMove;
import fftlib.game.FFTState;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kulibrat.ai.Minimax.LookupTableMinimax;
import kulibrat.ai.Minimax.MinimaxPlay;
import kulibrat.game.State;
import kulibrat.game.Logic;
import kulibrat.game.Move;
import kulibrat.gui.Dialogs.OverwriteDBDialog;
import misc.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;


public class Database implements FFTDatabase {
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

        String tableName = "plays_" + Config.SCORELIMIT;
        long key = new State(new kulibrat.game.State()).getHashCode();
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
    public static ArrayList<Move> bestPlays(State n) {
        ArrayList<Move> bestPlays = new ArrayList<>();
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        if (!Logic.gameOver(n.getNextState(bestPlay.move))) {
            bestScore = queryPlay(n.getNextState(bestPlay.move)).score;
        }
        for (State child : n.getChildren()) {
            Move m = child.getMove();
            kulibrat.game.State state = n.getNextState(m);
            if (Logic.gameOver(state)) {
                if (Logic.getWinner(state) == m.team) bestPlays.add(m);
            } else if (queryPlay(child).score == bestScore) {
                bestPlays.add(m);
            }
        }
        return bestPlays;
    }

    public static ArrayList<Move> nonLosingPlays(State n) {
        ArrayList<Move> nonLosingPlays = new ArrayList<>();
        MinimaxPlay bestPlay = queryPlay(n);
        int bestScore = 0;
        boolean won = false;
        if (!Logic.gameOver(n.getNextState(bestPlay.move))) {
            bestScore = queryPlay(n.getNextState(bestPlay.move)).score;
        } else
            won = true;

        for (State child : n.getChildren()) {
            Move m = child.getMove();
            kulibrat.game.State state = n.getNextState(m);
            if (Logic.gameOver(state)) {
                if (Logic.getWinner(state) == m.team)
                    nonLosingPlays.add(m);
            } else {
                int score = queryPlay(child).score;
                if (score == bestScore) {
                    nonLosingPlays.add(m);
                } else if (won && m.team == PLAYER1 && score > 0)
                    nonLosingPlays.add(m);
                else if (won && m.team == PLAYER2 && score < 0)
                    nonLosingPlays.add(m);
                else if (bestScore > 0 && score > 0) {
                    nonLosingPlays.add(m);
                } else if (bestScore < 0 && score < 0) {
                    nonLosingPlays.add(m);
                }
            }
        }
        return nonLosingPlays;
    }

    // Fetches the best play corresponding to the input node
    public static MinimaxPlay queryPlay(State n) {
        MinimaxPlay play = null;
        String tableName = "plays_" + Config.SCORELIMIT;
        Long key = n.getHashCode();
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("select oldRow, oldCol, newRow, newCol, team, score from "
                    + tableName + " where id=" + key);
            while (resultSet.next()) {
                Move move = new Move(resultSet.getInt(1), resultSet.getInt(2),
                        resultSet.getInt(3), resultSet.getInt(4), resultSet.getInt(5));
                int score = resultSet.getInt(6);
                play = new MinimaxPlay(move, score, 0);
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
    public static String turnsToTerminal(int turn, State n) {
        int score = queryPlay(n).score;
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
        Stage newStage = new Stage();
        String labelText = "The DB Table for this score limit has not been built.\n" +
                "       Do you want to build it? It will take a while";
        newStage.setScene(new Scene(new OverwriteDBDialog(labelText), 500, 150));
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setOnCloseRequest(Event::consume);
        newStage.show();
    }

    public static void fillLookupTable(HashMap<Long, MinimaxPlay> lookupTable) throws SQLException {
        System.out.println("Inserting data into table. This will take some time");
        String tableName = "plays_" + Config.SCORELIMIT;
        long startTime = System.currentTimeMillis();
        dbConnection.createStatement().execute("truncate table " + tableName);

        final int batchSize = 1000;
        int count = 0;
        PreparedStatement stmt = dbConnection.prepareStatement("insert into " + tableName + " values (?, ?, ?, ?, ?, ?, ?)");
        for (Map.Entry<Long, MinimaxPlay> entry : lookupTable.entrySet()) {
            Long key = entry.getKey();
            MinimaxPlay value = entry.getValue();
            stmt.setLong(1, key);
            stmt.setInt(2, value.move.oldRow);
            stmt.setInt(3, value.move.oldCol);
            stmt.setInt(4, value.move.newRow);
            stmt.setInt(5, value.move.newCol);
            stmt.setInt(6, value.move.team);
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
        String tableName = "plays_" + Config.SCORELIMIT;
        try {
            dbConnection.createStatement().execute("create table " + tableName +
                    "(id bigint primary key, oldRow smallint, oldCol smallint, newRow smallint, newCol smallint, team smallint, score smallint)");
        } catch (SQLException e) {
            System.out.println("Table '" + tableName + "' exists in the DB");
        }
    }

    // Builds the DB
    public static void buildLookupDB() {
        kulibrat.game.State state = new kulibrat.game.State();
        new LookupTableMinimax(PLAYER1, state, true);
    }

    public ArrayList<? extends FFTMove> nonLosingPlays(FFTState n) {
        return nonLosingPlays((State) n);
    }

    public FFTMinimaxPlay queryPlay(FFTState n) {
        return queryPlay((State) n);
    }


}
