package kulibrat.gui.board;

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
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kulibrat.game.Controller;
import kulibrat.game.Move;

import static misc.Config.*;

public class BoardTile extends StackPane {
    private int row;
    private int col;
    boolean highlight;
    private boolean bestMove;
    private boolean help;
    private Label turnsToTerminalLabel;
    private Controller cont;
    private boolean negated; // used for fft
    private boolean mandatory; // used for fft
    private Stage tileOptionsStage;
    private TileOptionsPane tileOptionsPane;
    private Group cross;
    int clickMode;

    private String white = "-fx-background-color: rgb(255, 255, 255);";
    private String gray = "-fx-background-color: rgb(150,150,150);";
    private String green = "-fx-background-color: rgb(0, 225, 0);";
    private String red = "-fx-background-color: rgb(255,0,0);";
    private String yellow = "-fx-background-color: rgb(255,200,0);";

    public BoardTile(int row, int col, int tilesize, int clickMode, Controller cont) {
        this.row = row;
        this.col = col;
        this.cont = cont;
        this.clickMode = clickMode;
        setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setAlignment(Pos.CENTER);
        setPrefSize(tilesize, tilesize);
        setMaxWidth(tilesize);
        setStyle(white);
        turnsToTerminalLabel = new Label("");
        turnsToTerminalLabel.setFont(Font.font("Verdana", 15));
        turnsToTerminalLabel.setTextFill(Color.BLACK);
        setOnMouseEntered(me -> {
            if (highlight && bestMove) {
                setStyle(green);
            } else if (highlight) {
                if (help) setStyle(red);
                else setStyle(yellow);
            }
        });

        setOnMouseExited(me -> {
            if (highlight && bestMove) {
                setStyle("-fx-background-color: rgb(0, 150, 0);");
            } else if (highlight) {
                if (help) setStyle("-fx-background-color: rgb(150,0,0);");
                else setStyle("-fx-background-color: rgb(200,150,0);");
            }
        });

        setOnMouseClicked(me -> {
            if (clickMode == CLICK_INTERACTIVE) {
                if (highlight) {
                    cont.getInteractiveState().setArrowEndpoint(row, col);
                }
                else {
                    showInteractiveOptions();
                }
            }
            else if (clickMode == CLICK_DEFAULT) {
                if (highlight) {
                    BoardPiece piece = cont.getSelected();
                    cont.doHumanTurn(new Move(piece.getRow(), piece.getCol(), row, col, piece.getTeam()));
                }
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

    public void setHighlight(boolean highlight, boolean help, boolean bestMove, String turns) {
        this.highlight = highlight;
        this.bestMove = bestMove;
        this.help = help;
        if (highlight && bestMove) {
            setStyle("-fx-background-color: rgb(0, 150, 0);");
        } else if (highlight) {
            if (help) setStyle("-fx-background-color: rgb(150,0,0);");
            else setStyle("-fx-background-color: rgb(200,150,0);");
        } else {
            setStyle(white);
        }

        if (turns.isEmpty()) {
            getChildren().remove(turnsToTerminalLabel);
        } else {
            turnsToTerminalLabel.setText(turns);
            turnsToTerminalLabel.setTextFill(Color.BLACK);
            getChildren().add(turnsToTerminalLabel);
            if (getChildren().size() > 1) {
                BoardPiece bp = (BoardPiece) getChildren().get(0);
                if (bp.getTeam() == PLAYER2) {
                    turnsToTerminalLabel.setTextFill(Color.WHITE);
                }
            }
        }
    }

    private void drawCross() {
        cross = new Group();
        Line vertical = new Line(-20, -20, 20, 20);
        Line horizontal = new Line(-20, 20, 20, - 20);
        vertical.setStrokeWidth(1);
        vertical.setSmooth(true);
        horizontal.setStrokeWidth(1);
        horizontal.setSmooth(true);
        horizontal.setStroke(Color.RED);
        vertical.setStroke(Color.RED);

        cross.getChildren().addAll(horizontal, vertical);

        getChildren().add(cross);
    }

    private void removeCross() {
        getChildren().remove(cross);
    }



    public void update() {
        if (clickMode == CLICK_INTERACTIVE) {
            BoardPiece p = getPiece();
            tileOptionsPane.setMandatory(p != null);
        }
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isNegated() {
        return negated;
    }

    public BoardPiece getPiece() {
        for (Node n : getChildren())
            if (n instanceof BoardPiece)
                return (BoardPiece) n;
        return null;
    }

    public int getTeam() {
        BoardPiece p = getPiece();
        if (p == null)
            return -1;
        else return p.getTeam();
    }

    private class TileOptionsPane extends BorderPane {
        BoardTile bt;
        GridPane interactiveGrid;
        CheckBox mandatoryCheckBox;

        TileOptionsPane(BoardTile bt) {
            this.bt = bt;
            interactiveGrid = makeInteractiveGrid();
            BorderPane.setMargin(interactiveGrid, new Insets(10));
            setCenter(interactiveGrid);
            setBottom(makeBottomPane());

        }

        void setMandatory(boolean mandatory) {
            bt.mandatory = mandatory;
            mandatoryCheckBox.setSelected(mandatory);

            if (!mandatory) {
                negated = false;
                bt.setStyle(white);
                BoardPiece piece = getPiece();
                if (piece != null) {
                    bt.getChildren().remove(piece);
                }
                drawCross();
                cont.getInteractiveState().getPlayBox().removeArrows();
            } else {
                removeCross();
            }
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
                cont.getInteractiveState().update(bt);
                close();
            });
            HBox mandatoryHBox = new HBox(5, mandatoryLabel, mandatoryCheckBox);
            mandatoryHBox.setAlignment(Pos.CENTER);
            bottomPane.getChildren().add(mandatoryHBox);

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
            interactiveGrid.setStyle(white);
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

            BoardPiece bp1 = new BoardPiece(PLAYER1, cont, row, col,50, CLICK_DISABLED);
            BoardPiece bp2 = new BoardPiece(PLAYER2, cont, row, col, 50, CLICK_DISABLED);
            StackPane bpPane1 = new StackPane();
            StackPane bpPane2 = new StackPane();
            bpPane1.setAlignment(Pos.CENTER);
            bpPane2.setAlignment(Pos.CENTER);
            bpPane1.getChildren().add(bp1);
            bpPane2.getChildren().add(bp2);

            bpPane1.setOnMouseClicked(event -> {
                BoardPiece piece = getPiece();
                if (piece != null) {
                    bt.getChildren().remove(piece);
                }
                BoardPiece bp = new BoardPiece(PLAYER1, cont, row, col, 20, CLICK_INTERACTIVE);
                bt.getChildren().add(bp);
                close();
                setMandatory(true);
                cont.getInteractiveState().update(bt);
            });

            bpPane2.setOnMouseClicked(event -> {
                BoardPiece piece = getPiece();
                if (piece != null) {
                    bt.getChildren().remove(piece);
                }
                BoardPiece bp = new BoardPiece(PLAYER2, cont, row, col,20, CLICK_INTERACTIVE);
                bt.getChildren().add(bp);
                close();
                setMandatory(true);
                cont.getInteractiveState().update(bt);
            });

            VBox paneGray = new VBox();
            paneGray.setStyle(gray);
            paneGray.setOnMouseClicked(event -> {
                negated = true;
                bt.setStyle(gray);
                close();
                setMandatory(true);
                cont.getInteractiveState().update(bt);
            });

            VBox paneWhite = new VBox();
            paneWhite.setStyle(white);
            paneWhite.setOnMouseClicked(event -> {
                negated = false;
                bt.setStyle(white);
                BoardPiece piece = getPiece();
                if (piece != null)
                    bt.getChildren().remove(piece);
                close();
                cont.getInteractiveState().getPlayBox().removeArrows();
                cont.getInteractiveState().update(bt);

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

