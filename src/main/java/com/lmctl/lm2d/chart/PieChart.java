package com.lmctl.lm2d.chart;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Path2D;
import com.lmctl.lm2d.graphics.TextStyle;
import java.util.List;

public final class PieChart {
    public record Slice(String label, double value, String color) {
        public Slice {
            if (label == null || !Double.isFinite(value) || value < 0 || color == null) {
                throw new IllegalArgumentException("Pie slices require a label, non-negative value, and color");
            }
        }
    }

    private final String title;
    private final List<Slice> slices;
    private final ChartTheme theme;

    public PieChart(String title, List<Slice> slices) {
        this(title, slices, ChartTheme.dark());
    }

    public PieChart(String title, List<Slice> slices, ChartTheme theme) {
        if (title == null || slices == null || slices.isEmpty() || theme == null
                || slices.stream().mapToDouble(Slice::value).sum() <= 0) {
            throw new IllegalArgumentException("A pie chart requires a title, positive data, and theme");
        }
        this.title = title;
        this.slices = List.copyOf(slices);
        this.theme = theme;
    }

    public void render(Graphics2D graphics) {
        int width = graphics.width();
        int height = graphics.height();
        graphics.rectangle(0, 0, width, height, Paint.fill(theme.background()));
        graphics.text(32, 44, title, TextStyle.label(theme.foreground(), 23).bold());
        double centerX = width * 0.36;
        double centerY = height * 0.56;
        double radius = Math.min(width, height) * 0.3;
        double total = slices.stream().mapToDouble(Slice::value).sum();
        double angle = -Math.PI / 2;
        for (Slice slice : slices) {
            double end = angle + slice.value() / total * Math.PI * 2;
            Path2D wedge = new Path2D().moveTo(centerX, centerY)
                    .arc(centerX, centerY, radius, angle, end, false).close();
            graphics.path(wedge, Paint.fill(slice.color()).withStroke(theme.background(), 3));
            double middle = (angle + end) / 2;
            if (slice.value() / total > 0.07) {
                graphics.text(centerX + Math.cos(middle) * radius * 0.65,
                        centerY + Math.sin(middle) * radius * 0.65,
                        Math.round(slice.value() / total * 100) + "%",
                        TextStyle.label("#07101c", 14).centered().middle().bold());
            }
            angle = end;
        }
        double legendY = 128;
        for (Slice slice : slices) {
            graphics.rectangle(width * 0.68, legendY - 11, 12, 12, Paint.fill(slice.color()));
            graphics.text(width * 0.68 + 24, legendY, slice.label(),
                    TextStyle.label(theme.foreground(), 13).bold());
            graphics.text(width * 0.68 + 24, legendY + 20, LinearScale.format(slice.value()),
                    TextStyle.label(theme.muted(), 12));
            legendY += 64;
        }
    }
}
