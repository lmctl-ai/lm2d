package com.lmctl.lm2d.chart;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.TextStyle;
import java.util.List;

final class ChartSupport {
    record Bounds(double minX, double maxX, double minY, double maxY) {
        double paddedMinX() {
            return minX == maxX ? minX - 1 : minX;
        }

        double paddedMaxX() {
            return minX == maxX ? maxX + 1 : maxX;
        }

        double paddedMinY() {
            return minY == maxY ? minY - 1 : minY;
        }

        double paddedMaxY() {
            return minY == maxY ? maxY + 1 : maxY;
        }
    }

    private ChartSupport() {
    }

    static Bounds bounds(List<Series> series) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Series item : series) {
            for (var point : item.points()) {
                minX = Math.min(minX, point.x());
                maxX = Math.max(maxX, point.x());
                minY = Math.min(minY, point.y());
                maxY = Math.max(maxY, point.y());
            }
        }
        return new Bounds(minX, maxX, minY, maxY);
    }

    static void axes(Graphics2D graphics, LinearScale x, LinearScale y, double left, double right,
            double top, double bottom, ChartTheme theme) {
        for (double tick : y.ticks(6)) {
            double position = y.map(tick);
            graphics.line(left, position, right, position, theme.grid(), 1);
            graphics.text(left - 12, position + 4, LinearScale.format(tick),
                    new TextStyle(theme.muted(), 11, "right", "alphabetic", 400));
        }
        for (double tick : x.ticks(7)) {
            double position = x.map(tick);
            graphics.line(position, top, position, bottom, theme.grid(), 1);
            graphics.text(position, bottom + 27, LinearScale.format(tick),
                    TextStyle.label(theme.muted(), 11).centered());
        }
        graphics.line(left, bottom, right, bottom, theme.muted(), 1.2);
        graphics.line(left, top, left, bottom, theme.muted(), 1.2);
    }

    static void legend(Graphics2D graphics, List<Series> series, double x, double y, ChartTheme theme) {
        double offset = x;
        for (Series item : series) {
            graphics.circle(offset + 5, y - 4, 5, com.lmctl.lm2d.graphics.Paint.fill(item.color()));
            graphics.text(offset + 17, y, item.name(), TextStyle.label(theme.muted(), 12));
            offset += 105;
        }
    }
}
