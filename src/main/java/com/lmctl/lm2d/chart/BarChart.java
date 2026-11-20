package com.lmctl.lm2d.chart;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.TextStyle;
import java.util.List;

public final class BarChart {
    public record Bar(String label, double value, String color) {
        public Bar {
            if (label == null || !Double.isFinite(value) || color == null) {
                throw new IllegalArgumentException("Invalid bar");
            }
        }
    }

    private final List<Bar> bars;
    private final String title;
    private final ChartTheme theme;

    public BarChart(String title, List<Bar> bars) {
        this(title, bars, ChartTheme.dark());
    }

    public BarChart(String title, List<Bar> bars, ChartTheme theme) {
        if (title == null || bars == null || bars.isEmpty() || theme == null) {
            throw new IllegalArgumentException("A bar chart requires a title, data, and theme");
        }
        this.title = title;
        this.bars = List.copyOf(bars);
        this.theme = theme;
    }

    public void render(Graphics2D graphics) {
        int width = graphics.width();
        int height = graphics.height();
        double left = 76;
        double right = width - 30;
        double top = 82;
        double bottom = height - 62;
        graphics.rectangle(0, 0, width, height, Paint.fill(theme.background()));
        graphics.text(32, 44, title, TextStyle.label(theme.foreground(), 23).bold());
        double minimum = bars.stream().mapToDouble(Bar::value).min().orElse(0);
        double maximum = bars.stream().mapToDouble(Bar::value).max().orElse(1);
        minimum = Math.min(0, minimum);
        maximum = Math.max(0, maximum);
        if (maximum == minimum) {
            maximum = minimum + 1;
        }
        LinearScale y = new LinearScale(minimum, maximum, bottom, top);
        BandScale x = new BandScale(bars.stream().map(Bar::label).toList(), left, right, 0.28);
        drawYAxis(graphics, y, left, right, top, bottom);
        double zero = y.map(0);
        graphics.line(left, zero, right, zero, theme.muted(), 1.2);
        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            double value = y.map(bar.value());
            graphics.rectangle(x.start(i), Math.min(zero, value), x.bandwidth(), Math.abs(zero - value),
                    Paint.fill(bar.color()));
            graphics.text(x.center(i), bottom + 27, bar.label(),
                    TextStyle.label(theme.muted(), 12).centered());
        }
    }

    static void drawYAxis(Graphics2D graphics, LinearScale y, double left, double right, double top, double bottom) {
        ChartTheme theme = ChartTheme.dark();
        drawYAxis(graphics, y, left, right, top, bottom, theme);
    }

    static void drawYAxis(Graphics2D graphics, LinearScale y, double left, double right, double top, double bottom,
            ChartTheme theme) {
        for (double tick : y.ticks(6)) {
            double position = y.map(tick);
            graphics.line(left, position, right, position, theme.grid(), 1);
            graphics.text(left - 12, position + 4, LinearScale.format(tick),
                    new TextStyle(theme.muted(), 11, "right", "alphabetic", 400));
        }
    }
}
