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




        // Вершины упорядочиваются по возрастанию Y: top (y == minY), middle, bottom (y == maxY).
        // Это гарантирует корректную растеризацию как для «острого» (верхняя точка),
        // так и для «перевёрнутого» (нижняя точка) треугольника.
        int topX = x0, topY = y0; Color topColor = color0;
        int midX = x1, midY = y1; Color midColor = color1;
        int botX = x2, botY = y2; Color botColor = color2;

        if (topY > midY) { int tx = topX; topX = midX; midX = tx;
                           int ty = topY; topY = midY; midY = ty;
                           Color tc = topColor; topColor = midColor; midColor = tc; }
        if (midY > botY) { int tx = midX; midX = botX; botX = tx;
                           int ty = midY; midY = botY; botY = ty;
                           Color tc = midColor; midColor = botColor; botColor = tc; }
        if (topY > midY) { int tx = topX; topX = midX; midX = tx;
                           int ty = topY; topY = midY; midY = ty;
                           Color tc = topColor; topColor = midColor; midColor = tc; }

        // Обход top -> middle -> bottom должен быть против часовой стрелки в экранных
        // координатах (Y вниз): тогда edge-функции положительны внутри треугольника.
        // Если порядок обхода противоположный, разворачиваем его, меняя местами среднюю
        // и нижнюю вершины (вместе с их цветами). Без этого треугольники с обходом по
        // часовой стрелке не рисовались вообще («треугольник исчезает»).
        boolean orientationFlipped = false;
        double signedArea2 = (double) (midX - topX) * (botY - topY)
                           - (double) (botX - topX) * (midY - topY);
        if (signedArea2 < 0) {
            int tx = midX; midX = botX; botX = tx;
            int ty = midY; midY = botY; botY = ty;
            Color tc = midColor; midColor = botColor; botColor = tc;
            orientationFlipped = true;
        }

        // Вырожденный треугольник (все вершины на одной горизонтали) — рисуем горизонтальную линию
        if (topY == botY) {
            int lx = Math.min(topX, Math.min(midX, botX));
            int rx = Math.max(topX, Math.max(midX, botX));
            for (int x = lx; x <= rx; x++) {
                pixelWriter.setColor(x, topY, Color.BLACK);
            }
            return;
        }

        final double dTopX = topX, dTopY = topY;
        final double dMidX = midX, dMidY = midY;
        final double dBotX = botX, dBotY = botY;

        // Наклонные рёбра: long — от верхней вершины до нижней,
        // short — ломаная верх -> середина -> низ.
        final double longSlope  = (dBotX - dTopX) / (dBotY - dTopY);
        final double upperSlope = (dMidY == dTopY) ? 0 : (dMidX - dTopX) / (dMidY - dTopY);
        final double lowerSlope = (dBotY == dMidY) ? 0 : (dBotX - dMidX) / (dBotY - dMidY);

        // Последняя закрашиваемая строка: нижняя вершина включается только
        // если при ней есть горизонтальное ребро (botY == midY)
        final int lastPaintedY = (botY == midY) ? botY : botY - 1;

        // Эталонное правило растеризации (top-left fill rule): пиксел закрашивается,
        // если его центр (x+0.5, y+0.5) лежит внутри треугольника, либо на его
        // «верхне-левом» краю. Обход top -> middle -> bottom всегда ориентирован
        // против часовой стрелки в экранных координатах (Y вниз), поэтому центры
        // треугольника дают положительную edge function w = dx*(yc-y1) - dy*(xc-x1).
        final double eABx = dMidX - dTopX, eABy = dMidY - dTopY; // top -> middle
        final double eBCx = dBotX - dMidX, eBCy = dBotY - dMidY; // middle -> bottom
        final double eCAx = dTopX - dBotX, eCAy = dTopY - dBotY; // bottom -> top

        // Барицентрические веса вычисляются напрямую из edge-функций:
        // вес top = wBC / area2, вес middle = wCA / area2, вес bottom = wAB / area2,
        // где area2 — удвоенная площадь треугольника (при CCW-обходе > 0).
        // Это исключает случайные «TRANSPARENT/BLACK», которые давал getColor() при
        // расхождении спана и теста покрытия, и гарантирует корректный градиент
        // независимо от порядка обхода вершин.
        final double area2v = (double) eABy * eCAx - (double) eABx * eCAy; // > 0 после нормализации ориентации

        for (int y = topY; y <= lastPaintedY; y++) {
            final double yc = y + 0.5;

            double lx = dTopX + (yc - dTopY) * longSlope;              // x ребра long на уровне yc
            double sx = (yc <= dMidY)                                  // x ломаного short-ребра
                        ? dTopX + (yc - dTopY) * upperSlope
                        : dMidX + (yc - dMidY) * lowerSlope;

            int start = (int) Math.ceil(Math.min(lx, sx) - 0.5);
            int end   = (int) Math.ceil(Math.max(lx, sx) - 0.5);

            // Горизонтальные рёбра всегда принадлежат треугольнику — расширяем спан
            boolean includeTopEdge    = (y == topY && midY == topY);
            boolean includeMiddleEdge = (y == midY && midY != topY);
            boolean includeBottomEdge = (y == botY && botY == midY);

            if (includeMiddleEdge || includeBottomEdge) {
                start = Math.min(start, Math.min(midX, botX));
                end   = Math.max(end,   Math.max(midX, botX) + 1);
            } else if (includeTopEdge) {
                start = Math.min(start, Math.min(topX, midX));
                end   = Math.max(end,   Math.max(topX, midX) + 1);
            }

            for (int x = start; x < end; x++) {
                final double xc = x + 0.5;

                double wAB = eABx * (yc - dTopY) - eABy * (xc - dTopX);
                double wBC = eBCx * (yc - dMidY) - eBCy * (xc - dMidX);
                double wCA = eCAx * (yc - dBotY) - eCAy * (xc - dBotX);

                final double eps = 1e-9;
                boolean inAB = (wAB > -eps) && (Math.abs(wAB) > eps || isTopLeftEdge(eABx, eABy));
                boolean inBC = (wBC > -eps) && (Math.abs(wBC) > eps || isTopLeftEdge(eBCx, eBCy));
                boolean inCA = (wCA > -eps) && (Math.abs(wCA) > eps || isTopLeftEdge(eCAx, eCAy));

                if (!(inAB && inBC && inCA)) continue;

                double wa = Math.max(0, Math.min(1, wBC / area2v)); // вес top
                double wb = Math.max(0, Math.min(1, wCA / area2v)); // вес middle
                double wc = Math.max(0, Math.min(1, wAB / area2v)); // вес bottom

                pixelWriter.setColor(x, y, interpolateColor(topColor, midColor, botColor, wa, wb, wc));
            }
        }
    }

    // Правило «верхне-левшего» края для edge function с направлением (dx, dy):
    // такое ребро включает лежащие на нём пикселы, если оно восходящее (dy < 0)
    // или горизонтальное, направленное вправо (dy == 0 и dx > 0).
    private static boolean isTopLeftEdge(final double dx, final double dy) {
        return dy < 0 || (dy == 0 && dx > 0);
    }

    // Интерполяция цвета по барицентрическим весам вершин (wa + wb + wc == 1).
    private static Color interpolateColor(final Color a, final Color b, final Color c,
                                          final double wa, final double wb, final double wc) {
        int r = (int) Math.round((a.getRed()   * wa + b.getRed()   * wb + c.getRed()   * wc) * 255);
        int g = (int) Math.round((a.getGreen() * wa + b.getGreen() * wb + c.getGreen() * wc) * 255);
        int bl= (int) Math.round((a.getBlue()  * wa + b.getBlue()  * wb + c.getBlue()  * wc) * 255);
        return Color.rgb(r, g, bl);
    }

    public static void clear(final GraphicsContext graphicsContext){

    }
}
