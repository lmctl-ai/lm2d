package com.lmctl.lm2d.chart;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Path2D;
import com.lmctl.lm2d.graphics.Point2D;
import com.lmctl.lm2d.graphics.TextStyle;
import java.util.List;

public final class LineChart {
    private final String title;
    private final List<Series> series;
    private final ChartTheme theme;

    public LineChart(String title, List<Series> series) {
        this(title, series, ChartTheme.dark());
    }

    public LineChart(String title, List<Series> series, ChartTheme theme) {
        if (title == null || series == null || series.isEmpty() || theme == null) {
            throw new IllegalArgumentException("A line chart requires a title, series, and theme");
        }
        this.title = title;
        this.series = List.copyOf(series);
        this.theme = theme;
    }

    public void render(Graphics2D graphics) {
        int width = graphics.width();
        int height = graphics.height();
        double left = 76;
        double right = width - 32;
        double top = 104;
        double bottom = height - 62;
        graphics.rectangle(0, 0, width, height, Paint.fill(theme.background()));
        graphics.text(32, 42, title, TextStyle.label(theme.foreground(), 23).bold());
        ChartSupport.legend(graphics, series, 34, 72, theme);
        ChartSupport.Bounds bounds = ChartSupport.bounds(series);
        LinearScale x = new LinearScale(bounds.paddedMinX(), bounds.paddedMaxX(), left, right);
        LinearScale y = new LinearScale(bounds.paddedMinY(), bounds.paddedMaxY(), bottom, top);
        ChartSupport.axes(graphics, x, y, left, right, top, bottom, theme);
        for (Series item : series) {
            Path2D path = new Path2D();
            for (int i = 0; i < item.points().size(); i++) {
                Point2D point = item.points().get(i);
                double px = x.map(point.x());
                double py = y.map(point.y());
                if (i == 0) {
                    path.moveTo(px, py);
                } else {
                    path.lineTo(px, py);
                }
                graphics.circle(px, py, 4, Paint.fill(item.color()).withStroke(theme.background(), 2));
            }
            graphics.path(path, Paint.stroke(item.color(), 2.5));
        }
    }
}
