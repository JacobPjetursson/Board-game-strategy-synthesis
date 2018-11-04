package gui.menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import misc.Config;

public class ReadMePane extends VBox {

    ReadMePane() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        TextArea readme = new TextArea();
        readme.setWrapText(true);
        readme.setEditable(false);
        readme.setText("# PROJECT DESCRIPTION AND SETUP\n" +
                "\n" +
                "This project is about solving the board game called Kulibrat.\n" +
                "\n" +
                "It does so by brute-forcing with Minimax.\n" +
                "\n" +
                "Three different AI's are implemented.\n" +
                "\n" +
                "The first agent is a Minimax algorithm with iterative deepening, alpha beta pruning and transposition tables.\n" +
                "\n" +
                "The second agent is an MCTS algorithm.\n" +
                "\n" +
                "The third agent is cheating, since it just looks up the best move from a local database.\n" +
                "The database stores all possible states from the current game configuration and links them to the best respective play.\n" +
                "The DB is not uploaded to Git due to its size. It is generated, if wished, when starting the game, and is then stored on disk for future use.\n" +
                "\n" +
                "The project can be compiled as is. There is also an executable JAR-file which launches the project instantly. The database and library files must be in the same folder as the JAR-file.\n" +
                "\n" +
                "# HOW TO PLAY THE GAME\n" +
                "\n" +
                "When starting a new game, choose who should be playing as Red and Black\n" +
                "\n" +
                "It is possible to change the computation time for each move for MCTS and Minimax.\n" +
                "\n" +
                "For the lookup table, there is an option to overwrite the database, which is necessary for the perfect player to work. \n" +
                "\n" +
                "Once in-game, the human player can ask for help by the perfect player, granted that the database has been built for the chosen score limit. This will highlight the best moves in green, and all other moves in red.\n" +
                "The numbers shown on the tiles say how many turns it takes to win (for positive number) or lose (for negative number), for that child state. This is assuming perfect play from opponent.\n" +
                "\n" +
                "During or after the game, it is possible to review the game, when playing vs. the AI. This will show the player all the moves that was made during the game, and if they were perfect or not.\n" +
                "It also gives the option to go back to a state of free choice and play the game from there.\n");
        VBox.setVgrow(readme, Priority.ALWAYS);
        Button back = new Button("Back");
        VBox.setMargin(back, new Insets(10));
        back.setMinWidth(Config.WIDTH / 6);
        setPadding(new Insets(0, 0, 10, 0));
        back.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new MenuPane(),
                    Config.WIDTH, Config.HEIGHT));
        });

        getChildren().addAll(readme, back);
    }
}
