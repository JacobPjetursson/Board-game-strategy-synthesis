package tictactoe.gui.board;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tictactoe.game.Controller;

import static misc.Config.AUTOGEN_TEAM;
import static misc.Globals.*;

public class BoardTile extends StackPane {
    private int row;
    private int col;
    private int tilesize;
    private int clickMode;
    private Node hoverNode = null;
    private boolean negated; // used for fft
    private boolean used; // used for fft
    private boolean isAction; // used for fft
    private Node highlightPiece = null;
    private Stage tileOptionsStage;
    private Label turnsToTerminalLabel;
    public TileOptionsPane tileOptionsPane;

    private Controller cont;
    public static Color blue = new Color(0, 0, 1, 0.4);
    public static Color green = new Color(0, 1, 0, 0.4);
    private Node piece;
    int team;
    Group redCross;

    public static final  String whiteStr = "-fx-background-color: rgb(255, 255, 255);";
    public static final  String grayStr = "-fx-background-color: rgb(150,150,150);";
    public static final String greenStr = "-fx-background-color: rgb(0, 225, 0);";
    public static final String blueStr = "-fx-background-color: rgb(0, 0, 225);";
    public static final String redStr = "-fx-background-color: rgb(255, 0, 0);";
    public static final String yellowStr = "-fx-background-color: rgb(255, 255, 0);";

    public BoardTile(int row, int col, int tilesize, int clickMode, Controller cont) {
        // set values
        this.row = row;
        this.col = col;
        this.tilesize = tilesize;
        this.clickMode = clickMode;
        this.cont = cont;
        // style
        setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setAlignment(Pos.CENTER);
        setPrefSize(tilesize, tilesize);
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        turnsToTerminalLabel = new Label("");
        turnsToTerminalLabel.setFont(Font.font("Verdana", tilesize/3));
        turnsToTerminalLabel.setTextFill(Color.BLACK);
        // mouse events
        setOnMouseEntered(me -> {
            if (isClickable() && hoverNode == null && piece == null) {
                addHover(cont.getNode().getTurn());
            }
        });

        setOnMouseExited(me -> {
            if (isClickable() && piece == null) {
                removeHover();
            }
        });

        setOnMouseClicked(event -> {
            if (clickMode == CLICK_INTERACTIVE) {
                showInteractiveOptions();
            }
            else if (clickMode == CLICK_DEFAULT && piece == null) {
                getChildren().clear();
                drawPiece(cont.getNode().getTurn());
                cont.doHumanTurn(row, col);

            }
        });

        // set some extra stuff if this tile is interactive (part of rulePane)
        if (clickMode == CLICK_INTERACTIVE) {
            tileOptionsPane = new TileOptionsPane(this);
            tileOptionsStage = new Stage();
            tileOptionsStage.setScene(new Scene(tileOptionsPane, 600, 600));
            tileOptionsStage.initModality(Modality.APPLICATION_MODAL);
            tileOptionsStage.initOwner(cont.getWindow());
        }
    }

    public void setTurnsToTerminal(String turns) {
        if (turns.isEmpty()) {
            getChildren().remove(turnsToTerminalLabel);
        } else {
            turnsToTerminalLabel.setText(turns);
            turnsToTerminalLabel.setTextFill(Color.BLACK);
            getChildren().add(turnsToTerminalLabel);
        }
    }

    // used both to color the tile but also to draw a shape in interactive mode (the action)
    public void addHighlight(String color, int team) {
        setStyle(color);
        if (clickMode == CLICK_INTERACTIVE) {
            highlightPiece(blue, team);

        }
    }

    public void highlightPiece(Color color, int team) {
        if (highlightPiece != null)
            getChildren().remove(highlightPiece);
        highlightPiece = getPiece(team, color);
        getChildren().add(highlightPiece);
    }

    // removes all color and also the highlighted piece, if present
    public void removeHighlight() {
        setStyle(whiteStr);

        if (highlightPiece != null)
            getChildren().remove(highlightPiece);
    }

    // adds a gray piece on this tile which symbolizes that the player hovers over the tile
    private void addHover(int team) {
        Node n = getPiece(team, Color.BLACK);
        hoverNode = n;
        getChildren().add(n);
    }

    private boolean isClickable() {
        if (clickMode == CLICK_INTERACTIVE)
            return false;
        return clickMode != CLICK_DISABLED && (cont.getPlayerInstance(cont.getNode().getTurn()) == HUMAN ||
                (cont.getPlayerInstance(cont.getNode().getTurn()) == FFT && cont.getFFTAllowInteraction()));
    }

    private void removeHover() {
        if (hoverNode != null)
            getChildren().remove(hoverNode);
        hoverNode = null;
    }

    // All the functions only relevant for the rulePane when editing FFT's

    public void clear() {
        negated = false;
        isAction = false;
        team = PLAYER_NONE;
        drawUsed(false);
    }

    // drawing functions equivalent to what is in the cell
    private Node getPiece(int team, Color color) {
        Node n = null;
        if (team == PLAYER1) {
            n = getCross(color);
        } else if (team == PLAYER2) {
            n = getCircle(color);
        }
        return n;
    }

    private Group getCross(Color color) {
        Group g = new Group();
        Line vertical = new Line(-(tilesize/4), -(tilesize/4), tilesize/4, tilesize/4);
        Line horizontal = new Line(-(tilesize/4), tilesize/4, tilesize/4, -(tilesize/4));
        vertical.setStrokeWidth(tilesize/9);
        vertical.setSmooth(true);
        horizontal.setStrokeWidth(tilesize/9);
        horizontal.setSmooth(true);

        horizontal.setStroke(color);
        vertical.setStroke(color);

        g.getChildren().addAll(horizontal, vertical);
        return g;
    }

    private Circle getCircle(Color color) {
        Circle c = new Circle();
        c.setRadius(tilesize/2 - (tilesize/12));
        c.setStrokeWidth(0);
        c.setFill(color);
        return c;
    }

    private void drawRedCross() {
        if (redCross != null)
            return;
        redCross = new Group();
        Line vertical = new Line(-(tilesize/3), -(tilesize/3), tilesize/3, tilesize/3);
        Line horizontal = new Line(-(tilesize/3), tilesize/3, tilesize/3, -(tilesize/3));
        vertical.setStrokeWidth(1);
        vertical.setSmooth(true);
        horizontal.setStrokeWidth(1);
        horizontal.setSmooth(true);
        horizontal.setStroke(Color.RED);
        vertical.setStroke(Color.RED);

        redCross.getChildren().addAll(horizontal, vertical);

        getChildren().add(redCross);
    }

    private void eraseRedCross() {
        getChildren().remove(redCross);
        redCross = null;
    }

    // adds a shape (piece) to this tile and removes the previous one
    public void drawPiece(int team) {
        erasePiece();
        Node n = getPiece(team, Color.BLACK);
        if (n != null) {
            getChildren().add(n);
            this.piece = n;
        }
    }

    private void erasePiece() {
        if (piece != null) {
            getChildren().remove(piece);
        }
        piece = null;
    }

    public void drawAction(int team) {
        tileOptionsPane.actionCheckBox.setSelected(true);
        drawUsed(true);
        highlightPiece(blue, team);
    }

    public void eraseAction() {
        tileOptionsPane.actionCheckBox.setSelected(false);
        removeHighlight();
    }

    public void drawUsed(boolean used) {
        tileOptionsPane.usedCheckBox.setSelected(used);

        if (!used) {
            drawNegated(false);
            eraseAction();
            erasePiece();
            drawRedCross();
        } else {
            eraseRedCross();
        }
    }

    // this function sets the tile when updating it from a rule
    // it is the only combination with setAction() that has a functional and visual change
    public void set(int team, boolean negated) {
        this.team = team;
        this.negated = negated;
        this.used = true;

        drawPiece(team);
        drawUsed(true);
        drawNegated(negated);
    }

    public void setAction(int team) {
        this.isAction = true;
        this.negated = false;
        this.used = true;
        this.team = team;

        drawAction(team);
    }

    public void drawNegated(boolean negated) {
        if (negated) {
            setStyle(grayStr);
        } else {
            setStyle(whiteStr);
        }
    }

    public String toString() {
        return "ROW: " + row + " , COL: " + col + " , TEAM: " + team + " , USED: " + used + " , NEGATED: " + negated;
    }

    private void showInteractiveOptions() {
        tileOptionsStage.show();
    }

    // getters and setters

    public int getTeam() {
        return team;
    }

    public boolean isUsed() {
        return used;
    }

    public Node getPiece() {
        return piece;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public boolean isNegated() {
        return negated;
    }

    public void setGreen() {
        addHighlight(greenStr, team);
    }

    public void setYellow() {
        addHighlight(yellowStr, team);
    }

    public void setRed() {
        addHighlight(redStr, team);
    }

    public void removeColor() {
        addHighlight(whiteStr, team);

    }

    public void setFFTChosen(int team) {
        highlightPiece(blue, team);
    }

    public boolean isAction() {
        return isAction;
    }

    public class TileOptionsPane extends BorderPane {
        BoardTile bt;
        GridPane choiceGrid;
        CheckBox usedCheckBox;
        CheckBox actionCheckBox;

        TileOptionsPane(BoardTile bt) {
            this.bt = bt;
            choiceGrid = makeChoiceGrid();
            BorderPane.setMargin(choiceGrid, new Insets(10));
            setCenter(choiceGrid);
            setBottom(makeBottomPane());
        }

        private VBox makeBottomPane() {
            VBox bottomPane = new VBox(10);
            bottomPane.setAlignment(Pos.CENTER);

            // Used box
            Label usedLabel = new Label("Use this tile in the rule?");
            usedLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
            usedCheckBox = new CheckBox();
            usedCheckBox.setPrefSize(22, 22);
            usedCheckBox.pressedProperty().addListener((observableValue, oldValue, newValue) -> {
                used = !used;
                cont.getInteractiveNode().update(bt);
                close();
            });
            HBox usedHBox = new HBox(5, usedLabel, usedCheckBox);
            usedHBox.setAlignment(Pos.CENTER);
            bottomPane.getChildren().add(usedHBox);

            // Action box
            Label actionLabel = new Label("Mark this tile as your action?");
            actionLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
            actionCheckBox = new CheckBox();
            actionCheckBox.setPrefSize(22, 22);
            actionCheckBox.pressedProperty().addListener((observable, oldValue, newValue) -> {
                isAction = !isAction;
                if (newValue)
                    team = AUTOGEN_TEAM;
                else
                    team = PLAYER_NONE;

                used = newValue;
                cont.getInteractiveNode().update(bt);
                close();
            });

            HBox actionBox = new HBox(5, actionLabel, actionCheckBox);
            actionBox.setAlignment(Pos.CENTER);
            bottomPane.getChildren().add(actionBox);

            // Information
            Label infoLabel = new Label("The grey tile negates what is on the tile.\n" +
                    "The while tile clears the selection.");
            infoLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
            infoLabel.setTextAlignment(TextAlignment.CENTER);
            bottomPane.getChildren().add(infoLabel);
            bottomPane.setPadding(new Insets(0, 0, 20, 0));
            return bottomPane;
        }

        private GridPane makeChoiceGrid() {
            choiceGrid = new GridPane();
            choiceGrid.setAlignment(Pos.CENTER);
            choiceGrid.setStyle(whiteStr);
            choiceGrid.setGridLinesVisible(true);

            RowConstraints rc1 = new RowConstraints();
            rc1.setValignment(VPos.CENTER);
            ColumnConstraints cc1 = new ColumnConstraints();
            cc1.setHalignment(HPos.CENTER);
            RowConstraints rc2 = new RowConstraints();
            rc2.setValignment(VPos.CENTER);
            ColumnConstraints cc2 = new ColumnConstraints();
            cc2.setHalignment(HPos.CENTER);
            rc1.setPercentHeight(50);
            rc2.setPercentHeight(50);
            cc1.setPercentWidth(50);
            cc2.setPercentWidth(50);
            choiceGrid.getColumnConstraints().addAll(cc1, cc2);
            choiceGrid.getRowConstraints().addAll(rc1, rc2);

            Node cross = getChoiceCross();
            Node circle = getChoiceCircle();
            StackPane bpPane1 = new StackPane();
            StackPane bpPane2 = new StackPane();
            bpPane1.setAlignment(Pos.CENTER);
            bpPane2.setAlignment(Pos.CENTER);
            bpPane1.getChildren().add(cross);
            bpPane2.getChildren().add(circle);

            bpPane1.setOnMouseClicked(event -> {
                used = true;
                team = PLAYER1;
                close();
                cont.getInteractiveNode().update(bt);
            });

            bpPane2.setOnMouseClicked(event -> {
                used = true;
                team = PLAYER2;
                close();
                cont.getInteractiveNode().update(bt);
            });

            VBox paneGray = new VBox();
            paneGray.setStyle(grayStr);
            paneGray.setOnMouseClicked(event -> {
                used = true;
                negated = true;
                close();
                cont.getInteractiveNode().update(bt);
            });

            VBox paneWhite = new VBox();
            paneWhite.setStyle(whiteStr);
            paneWhite.setOnMouseClicked(event -> {
                used = true;
                negated = false;
                team = PLAYER_NONE;
                cont.getInteractiveNode().update(bt);

                close();
            });

            choiceGrid.add(bpPane1, 0, 0);
            choiceGrid.add(bpPane2, 1, 0);
            choiceGrid.add(paneWhite, 0, 1);
            choiceGrid.add(paneGray, 1, 1);
            return choiceGrid;
        }

        private Group getChoiceCross() {
            int lineLength = 80;
            boolean hover = false;

            Group g = new Group();
            Line vertical = new Line(-lineLength, -lineLength, lineLength, lineLength);
            Line horizontal = new Line(-lineLength, lineLength, lineLength, - lineLength);
            vertical.setStrokeWidth(lineLength / 2);
            vertical.setSmooth(true);
            horizontal.setStrokeWidth(lineLength / 2);
            horizontal.setSmooth(true);
            if (hover) {
                horizontal.setStroke(blue);
                vertical.setStroke(blue);
            } else {
                horizontal.setStroke(Color.BLACK);
                vertical.setStroke(Color.BLACK);
            }
            g.getChildren().addAll(horizontal, vertical);
            return g;
        }

        private Circle getChoiceCircle() {
            Circle c = getCircle(Color.BLACK);
            c.setRadius(80);
            return c;
        }



        private void close() {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        }
    }
}

