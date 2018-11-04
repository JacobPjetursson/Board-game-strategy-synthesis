package gui.menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import misc.Config;

class RulesPane extends VBox {

    RulesPane() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        final WebView browser = new WebView();
        browser.setZoom(1);
        StackPane browserPane = new StackPane(browser);
        browserPane.setPrefWidth(Config.WIDTH);

        final WebEngine webEngine = browser.getEngine();
        browser.setZoom(1.3);
        webEngine.load(this.getClass().getClassLoader().getResource("kulibrat_rules.html").toExternalForm());

        Button back = new Button("Back");
        back.setMinWidth(Config.WIDTH / 6);
        setPadding(new Insets(0, 00, 10, 00));
        back.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        back.setOnMouseClicked(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(new MenuPane(),
                    Config.WIDTH, Config.HEIGHT));
        });

        getChildren().addAll(browser, back);
    }
}
