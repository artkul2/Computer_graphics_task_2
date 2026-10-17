package main.rasterizationfxapp;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import main.rasterization.*;

import main.rasterization.Rasterization;

public class RasterizationController {

    @FXML
    AnchorPane anchorPane;
    @FXML
    private Canvas canvas;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 400, 100, Color.RED, 150, 450, Color.BLUE, 650, 400, Color.GREEN);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 200, 200, Color.MAGENTA, 200, 500, Color.CYAN, 600, 500, Color.YELLOW);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 400, 150, Color.ORANGE, 200, 500, Color.PURPLE, 600, 500, Color.WHITE);
            Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 200, 200, Color.RED, 600, 200, Color.BLUE, 400, 500, Color.GREEN);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 400, 200, Color.GREEN, 200, 500, Color.RED, 600, 500, Color.BLUE);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 100, 100, Color.YELLOW, 700, 120, Color.BLACK, 150, 500, Color.RED);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 300, 200, Color.BLUE, 150, 400, Color.BLUE, 450, 400, Color.BLUE);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 400, 150, Color.BLACK, 200, 450, Color.WHITE, 600, 450, Color.BLACK);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 100, 300, Color.RED, 400, 300, Color.GREEN, 700, 300, Color.BLUE);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), 300, 300, Color.RED, 300, 300, Color.GREEN, 300, 300, Color.BLUE);
        //Rasterization.drawTriangle(canvas.getGraphicsContext2D(), -50, -50, Color.RED, 400, 200, Color.GREEN, 200, 900, Color.BLUE);
    }

}