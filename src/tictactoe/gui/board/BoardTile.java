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

import static misc.Config.*;

public class BoardTile extends StackPane {
    private int row;
    private int col;
    private int tilesize;
    private int clickMode;
    private boolean negated; // used for fft
    private boolean mandatory; // used for fft
    private boolean isAction; // used for fft
    private Stage tileOptionsStage;
    public TileOptionsPane tileOptionsPane;

    private Controller cont;
    private Color gray = new Color(0.2, 0.2, 0.2, 1.0);
    private Node piece;
    int team;
    Group redCross;

    public static final  String whiteStr = "-fx-background-color: rgb(255, 255, 255);";
    public static final  String grayStr = "-fx-background-color: rgb(150,150,150);";
    public static final String greenStr = "-fx-background-color: rgb(0, 225, 0);";
    public static final String blueStr = "-fx-background-color: rgb(0, 0, 225);";

    public BoardTile(int row, int col, int tilesize, int clickMode, Controller cont) {
        this.row = row;
        this.col = col;
        this.team = -1;
        this.tilesize = tilesize;
        this.clickMode = clickMode;
        this.cont = cont;
        setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setAlignment(Pos.CENTER);
        setPrefSize(tilesize, tilesize);
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setOnMouseEntered(me -> {
            if (isClickable() && getChildren().isEmpty()) {
                addHover(cont.getState().getTurn());
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
                addPiece(cont.getState().getTurn());
                cont.doHumanTurn(row, col);

            }
        });


        if (clickMode == CLICK_INTERACTIVE) {
            tileOptionsPane = new TileOptionsPane(this);
            tileOptionsStage = new Stage();
            tileOptionsStage.setScene(new Scene(tileOptionsPane, 400, 400));
            tileOptionsStage.initModality(Modality.APPLICATION_MODAL);
            tileOptionsStage.initOwner(cont.getWindow());
        }
    }

    private void showInteractiveOptions() {
        tileOptionsStage.show();
    }

    private boolean isClickable() {
        if (clickMode == CLICK_INTERACTIVE)
            return false;
        return clickMode != CLICK_DISABLED && (cont.getPlayerInstance(cont.getState().getTurn()) == HUMAN ||
                (cont.getPlayerInstance(cont.getState().getTurn()) == FFT && cont.getFFTAllowInteraction()));
    }

    public void highlight(String color) {
        setStyle(color);
    }

    public void addPiece(int team) {
        Node n;
        if (team == PLAYER1) {
            n = getCircle(false);
        } else {
            n = getCross(false);
        }
        getChildren().add(n);
        this.team = team;
        this.piece = n;
    }

    private void addHover(int team) {
        Node n;
        if (team == PLAYER1) {
            n = getCircle(true);
        } else {
            n = getCross(true);
        }
        getChildren().add(n);
    }

    public int getTeam() {
        return team;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    private Group getCross(boolean hover) {
        Group g = new Group();
        Line vertical = new Line(-15, -15, 15, 15);
        Line horizontal = new Line(-15, 15, 15, - 15);
        vertical.setStrokeWidth(7);
        vertical.setSmooth(true);
        horizontal.setStrokeWidth(7);
        horizontal.setSmooth(true);
        if (hover) {
            horizontal.setStroke(gray);
            vertical.setStroke(gray);
        } else {
            horizontal.setStroke(Color.BLACK);
            vertical.setStroke(Color.BLACK);
        }
        g.getChildren().addAll(horizontal, vertical);
        return g;
    }

    private Circle getCircle(boolean hover) {
        Circle c = new Circle();
        c.setRadius(tilesize/2 - (tilesize/12));
        c.setStrokeWidth(0);
        if (hover)
            c.setFill(gray);
        else
            c.setFill(Color.BLACK);
        return c;
    }

    private void drawRedCross() {
        if (redCross != null)
            return;
        redCross = new Group();
        Line vertical = new Line(-20, -20, 20, 20);
        Line horizontal = new Line(-20, 20, 20, - 20);
        vertical.setStrokeWidth(1);
        vertical.setSmooth(true);
        horizontal.setStrokeWidth(1);
        horizontal.setSmooth(true);
        horizontal.setStroke(Color.RED);
        vertical.setStroke(Color.RED);

        redCross.getChildren().addAll(horizontal, vertical);

        getChildren().add(redCross);
    }

    private void removeRedCross() {
        getChildren().remove(redCross);
        redCross = null;
    }

    private void removePiece() {
        if (piece != null) {
            getChildren().remove(piece);
        }
        piece = null;
        team = -1;
    }

    private void removeHover() {
        getChildren().clear();
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

    public void update() {
        if (clickMode == CLICK_INTERACTIVE)
            setMandatory(piece != null);
    }

    public void setAction(boolean isAction) {
        this.isAction = isAction;
        tileOptionsPane.actionCheckBox.setSelected(isAction);
        removePiece();

        if (isAction) {
            setMandatory(true);
            highlight(blueStr);
            int team = cont.getInteractiveState().getPerspective();
            addPiece(team);
            cont.getInteractiveState().setActionTile(this);
        } else {
            highlight(whiteStr);
        }
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        tileOptionsPane.mandatoryCheckBox.setSelected(mandatory);

        if (!mandatory) {
            negated = false;
            setStyle(whiteStr);
            setAction(false);
            removePiece();
            drawRedCross();
        } else {
            removeRedCross();
        }
    }

    public boolean isAction() {
        return isAction;
    }

    public class TileOptionsPane extends BorderPane {
        BoardTile bt;
        GridPane interactiveGrid;
        CheckBox mandatoryCheckBox;
        CheckBox actionCheckBox;

        TileOptionsPane(BoardTile bt) {
            this.bt = bt;
            interactiveGrid = makeInteractiveGrid();
            BorderPane.setMargin(interactiveGrid, new Insets(10));
            setCenter(interactiveGrid);
            setBottom(makeBottomPane());
        }

        private VBox makeBottomPane() {
            VBox bottomPane = new VBox(10);
            bottomPane.setAlignment(Pos.CENTER);

            // Mandatory box
            Label mandatoryLabel = new Label("Use this tile in the rule?");
            mandatoryLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            mandatoryCheckBox = new CheckBox();
            mandatoryCheckBox.pressedProperty().addListener((observableValue, oldValue, newValue) -> {
                setMandatory(!mandatory);
                cont.getInteractiveState().updateRuleFromTile(bt);
                close();
            });
            HBox mandatoryHBox = new HBox(5, mandatoryLabel, mandatoryCheckBox);
            mandatoryHBox.setAlignment(Pos.CENTER);
            bottomPane.getChildren().add(mandatoryHBox);

            // Action box
            Label actionLabel = new Label("Mark this tile as your action?");
            actionLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            actionCheckBox = new CheckBox();
            actionCheckBox.pressedProperty().addListener((observable, oldValue, newValue) -> {
                boolean action = !isAction;
                BoardTile actionTile = cont.getInteractiveState().getActionTile();
                if (actionTile != null) {
                    actionTile.setMandatory(false);
                    cont.getInteractiveState().setActionTile(null);

                }
                if (action) {
                    setMandatory(true);
                    setAction(true);
                }
                cont.getInteractiveState().updateRuleFromTile(bt);

                close();
            });

            HBox actionBox = new HBox(5, actionLabel, actionCheckBox);
            actionBox.setAlignment(Pos.CENTER);
            bottomPane.getChildren().add(actionBox);

            // Information
            Label infoLabel = new Label("The grey tile negates what is on the tile.\n" +
                    "The while tile clears the selection.");
            infoLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            infoLabel.setTextAlignment(TextAlignment.CENTER);
            bottomPane.getChildren().add(infoLabel);
            return bottomPane;
        }

        private GridPane makeInteractiveGrid() {
            interactiveGrid = new GridPane();
            interactiveGrid.setAlignment(Pos.CENTER);
            interactiveGrid.setStyle(whiteStr);
            interactiveGrid.setGridLinesVisible(true);

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
            interactiveGrid.getColumnConstraints().addAll(cc1, cc2);
            interactiveGrid.getRowConstraints().addAll(rc1, rc2);

            Node circle = getCircle(false);
            Node cross = getCross(false);
            StackPane bpPane1 = new StackPane();
            StackPane bpPane2 = new StackPane();
            bpPane1.setAlignment(Pos.CENTER);
            bpPane2.setAlignment(Pos.CENTER);
            bpPane1.getChildren().add(circle);
            bpPane2.getChildren().add(cross);

            bpPane1.setOnMouseClicked(event -> {
                bt.removePiece();
                bt.addPiece(PLAYER1);
                close();
                setMandatory(true);
                cont.getInteractiveState().updateRuleFromTile(bt);
            });

            bpPane2.setOnMouseClicked(event -> {
                bt.removePiece();
                bt.addPiece(PLAYER2);
                close();
                setMandatory(true);
                cont.getInteractiveState().updateRuleFromTile(bt);
            });

            VBox paneGray = new VBox();
            paneGray.setStyle(grayStr);
            paneGray.setOnMouseClicked(event -> {
                negated = true;
                bt.setStyle(grayStr);
                close();
                setMandatory(true);
                cont.getInteractiveState().updateRuleFromTile(bt);
            });

            VBox paneWhite = new VBox();
            paneWhite.setStyle(whiteStr);
            paneWhite.setOnMouseClicked(event -> {
                negated = false;
                bt.setStyle(whiteStr);
                bt.removePiece();
                close();
                highlight(whiteStr);
                cont.getInteractiveState().updateRuleFromTile(bt);

            });

            interactiveGrid.add(bpPane1, 0, 0);
            interactiveGrid.add(bpPane2, 1, 0);
            interactiveGrid.add(paneWhite, 0, 1);
            interactiveGrid.add(paneGray, 1, 1);
            return interactiveGrid;
        }

        private void close() {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        }
    }
}

