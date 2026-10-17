package main.rasterization;

import com.sun.scenario.effect.impl.hw.mtl.MTLShaderSource;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class Rasterization {

    public static void drawLine(
            final GraphicsContext graphicsContext,
            final int x0, final int y0,
            final int x1, final int y1,
            final Color color)
    {
        final PixelWriter pixelWriter = graphicsContext.getPixelWriter();
        pixelWriter.setColor(x0, y0, color);

        int del_x = Math.abs(x1 - x0);
        int del_y = Math.abs(y1 - y0);

        int dir_x = (x0 < x1) ? 1 : -1;
        int dir_y = (y0 < y1) ? 1 : -1;

        int x = x0;
        int y = y0;

        int del_err = del_x - del_y;

        while (true) {
            pixelWriter.setColor(x, y, color);

            if (x == x1 && y == y1) {
                break;
            }

            int error = 2 * del_err;

            if (error > -del_y) {
                del_err -= del_y;
                x += dir_x;
            }

            if (error < del_x) {
                del_err += del_x;
                y += dir_y;
            }
        }
    }
    public static void drawTriangle(
            final GraphicsContext graphicsContext,
            final int first_x0, final int first_y0, final Color first_color0,
            final int first_x1, final int first_y1, final Color first_color1,
            final int first_x2, final int first_y2, final Color first_color2){

        final PixelWriter pixelWriter = graphicsContext.getPixelWriter();

        int mid_y = Math.min(Math.max(first_y1, first_y2), Math.min(Math.max(first_y0, first_y2), Math.max(first_y0, first_y1)));

        int x0 = first_x0; int x1 = first_x1; int x2 = first_x2;
        int y0 = first_y0; int y1 = first_y1; int y2 = first_y2;
        Color color0 = first_color0; Color color1 = first_color1; Color color2 = first_color2;

        if (mid_y < first_y1){
            if (mid_y == first_y0){
                x0 = first_x2; y0 = first_y2; color0 = first_color2;
                x1 = first_x0; y1 = first_y0; color1 = first_color0;
                x2 = first_x1; y2 = first_y1; color2 = first_color1;
            }else{
                x1 = first_x2; y1 = first_y2; color1 = first_color2;
                x2 = first_x1; y2 = first_y1; color2 = first_color1;
            }
        } else if (mid_y > first_y1) {
            if (mid_y == first_y0){
                x0 = first_x1; y0 = first_y1; color0 = first_color1;
                x1 = first_x0; y1 = first_y0; color1 = first_color0;
            }else{
                x0 = first_x1; y0 = first_y1; color0 = first_color1;
                x1 = first_x2; y1 = first_y2; color1 = first_color2;
                x2 = first_x0; y2 = first_y0; color2 = first_color0;
            }
        }


        //drawLine(graphicsContext, x0, y0, x1, y1, Color.BLACK);
        //drawLine(graphicsContext, x0, y0, x2, y2, Color.BLACK);
        //drawLine(graphicsContext, x2, y2, x1, y1, Color.BLACK);




        int weight;
        int left_edge_x;

        for(int y = y1; y < y2; y++){
            int one_edge_x = (y - y0) * (x2 - x0) / (y2 - y0) + x0;
            int sec_edge_x = (y - y2) * (x1 - x2) / (y1 - y2) + x2;
            left_edge_x = Math.min(one_edge_x, sec_edge_x);
            weight = Math.abs(one_edge_x - sec_edge_x) + left_edge_x;
            for(int x = left_edge_x; x < weight; x++){
                pixelWriter.setColor(x, y, getColor(x, y, x0, y0, color0, x1, y1, color1, x2, y2, color2));
            }
        }

        for(int y = y1; y > y0; y--){
            int one_edge_x = (y - y0) * (x2 - x0) / (y2 - y0) + x0;
            int sec_edge_x = (y - y0) * (x1 - x0) / (y1 - y0) + x0;
            left_edge_x = Math.min(one_edge_x, sec_edge_x);
            weight = Math.abs(one_edge_x - sec_edge_x) + left_edge_x;
            for(int x = left_edge_x; x < weight; x++){
                pixelWriter.setColor(x, y, getColor(x, y, x0, y0, color0, x1, y1, color1, x2, y2, color2));
            }
        }
    }

    private static Color getColor(final int x, final int y,
                                  final int x0, final int y0, final Color colorA,
                                  final int x1, final int y1, final Color colorB,
                                  final int x2, final int y2, final Color colorC
    ){

        double det = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);


        if (Math.abs(det) < 1e-9) {
            return Color.BLACK;
        }

        double A = ((y1 - y2) * (x - x2) + (x2 - x1) * (y - y2)) / det;
        double B = ((y2 - y0) * (x - x2) + (x0 - x2) * (y - y2)) / det;
        double C = 1.0 - A - B;

        if (A < 0 || B < 0 || C < 0) {
            return Color.TRANSPARENT;
        }

        double rA = colorA.getRed() * 255;
        double gA = colorA.getGreen() * 255;
        double bA = colorA.getBlue() * 255;

        double rB = colorB.getRed() * 255;
        double gB = colorB.getGreen() * 255;
        double bB = colorB.getBlue() * 255;

        double rC = colorC.getRed() * 255;
        double gC = colorC.getGreen() * 255;
        double bC = colorC.getBlue() * 255;

        int r = (int) Math.round(A * rA + B * rB + C * rC);
        int g = (int) Math.round(A * gA + B * gB + C * gC);
        int b = (int) Math.round(A * bA + B * bB + C * bC);

        return Color.rgb(r, g, b);
    }

    public static void clear(final GraphicsContext graphicsContext){

    }
}
