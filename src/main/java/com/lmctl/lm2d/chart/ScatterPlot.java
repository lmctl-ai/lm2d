package com.lmctl.lm2d.chart;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Point2D;
import com.lmctl.lm2d.graphics.TextStyle;
import java.util.List;

public final class ScatterPlot {
    private final String title;
    private final List<Series> series;
    private final ChartTheme theme;
    private final double radius;

    public ScatterPlot(String title, List<Series> series) {
        this(title, series, ChartTheme.dark(), 6);
    }

    public ScatterPlot(String title, List<Series> series, ChartTheme theme, double radius) {
        if (title == null || series == null || series.isEmpty() || theme == null
                || !Double.isFinite(radius) || radius <= 0) {
            throw new IllegalArgumentException("A scatter plot requires a title, series, theme, and radius");
        }
        this.title = title;
        this.series = List.copyOf(series);
        this.theme = theme;
        this.radius = radius;
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
            for (Point2D point : item.points()) {
                graphics.circle(x.map(point.x()), y.map(point.y()), radius,
                        Paint.fill(item.color()).withStroke(theme.background(), 2).withOpacity(0.85));
            }
        }
    }
}
