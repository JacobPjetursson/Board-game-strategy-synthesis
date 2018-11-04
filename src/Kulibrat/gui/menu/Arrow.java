package gui.menu;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;


public class Arrow extends Group {

    public Arrow(double startX, double startY, double endX, double endY, Color color) {
        double width = 3.0;
        Line line = new Line(startX, startY, endX, endY);
        line.setStrokeWidth(width);
        line.setSmooth(true);
        line.setStroke(color);

        Line a1 = new Line();
        Line a2 = new Line();
        a1.setStrokeWidth(width);
        a2.setStrokeWidth(width);
        a1.setSmooth(true);
        a2.setSmooth(true);
        a1.setStroke(color);
        a2.setStroke(color);
        a1.setEndX(endX);
        a1.setEndY(endY);
        a2.setEndX(endX);
        a2.setEndY(endY);
        double factor = width / Math.hypot(startX - endX, startY - endY);
        double factorO = width / Math.hypot(startX - endX, startY - endY);
        // part in direction of main line
        double dx = (startX - endX) * factor;
        double dy = (startY - endY) * factor;

        // part ortogonal to main line
        double ox = (startX - endX) * factorO;
        double oy = (startY - endY) * factorO;

        a1.setStartX(endX + dx - oy);
        a1.setStartY(endY + dy + ox);
        a2.setStartX(endX + dx + oy);
        a2.setStartY(endY + dy - ox);

        getChildren().addAll(line, a1, a2);
    }
}
