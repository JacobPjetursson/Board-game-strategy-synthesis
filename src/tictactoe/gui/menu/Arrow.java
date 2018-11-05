package tictactoe.gui.menu;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class Arrow extends Group {

    public Arrow(double x, double y, Color color) {
        double width = 3.0;
        Circle circle = new Circle();
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setStrokeWidth(width);
        circle.setSmooth(true);
        circle.setStroke(color);
/*
        Line a1 = new Line();
        Line a2 = new Line();
        a1.setStrokeWidth(width);
        a2.setStrokeWidth(width);
        a1.setSmooth(true);
        a2.setSmooth(true);
        a1.setStroke(color);
        a2.setStroke(color);
        a1.setEndX(x);
        a1.setEndY(y);
        a2.setEndX(x);
        a2.setEndY(y);
        double factor = width / Math.hypot(startX - x, startY - y);
        double factorO = width / Math.hypot(startX - x, startY - y);
        // part in direction of main line
        double dx = (startX - x) * factor;
        double dy = (startY - y) * factor;

        // part ortogonal to main line
        double ox = (startX - x) * factorO;
        double oy = (startY - y) * factorO;

        a1.setStartX(x + dx - oy);
        a1.setStartY(y + dy + ox);
        a2.setStartX(x + dx + oy);
        a2.setStartY(y + dy - ox);

        getChildren().addAll(PLAYER1, a1, a2);
        */
    }
}
