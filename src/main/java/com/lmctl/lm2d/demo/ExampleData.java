package com.lmctl.lm2d.demo;

import com.lmctl.lm2d.chart.BarChart;
import com.lmctl.lm2d.chart.PieChart;
import com.lmctl.lm2d.chart.Series;
import com.lmctl.lm2d.graphics.Point2D;
import java.util.ArrayList;
import java.util.List;

final class ExampleData {
    private ExampleData() {
    }

    static List<BarChart.Bar> bars() {
        return List.of(
                new BarChart.Bar("Mon", 34, "#61d6c5"),
                new BarChart.Bar("Tue", 52, "#ffcb6b"),
                new BarChart.Bar("Wed", 43, "#a99cf5"),
                new BarChart.Bar("Thu", 71, "#ff7f9f"),
                new BarChart.Bar("Fri", 64, "#78b9ff"),
                new BarChart.Bar("Sat", 28, "#9ce07b"));
    }

    static List<Series> lines() {
        return List.of(
                new Series("TeaVM", "#61d6c5", List.of(
                        new Point2D(1, 18), new Point2D(2, 27), new Point2D(3, 25),
                        new Point2D(4, 42), new Point2D(5, 51), new Point2D(6, 66))),
                new Series("LMCode", "#ffcb6b", List.of(
                        new Point2D(1, 8), new Point2D(2, 16), new Point2D(3, 24),
                        new Point2D(4, 23), new Point2D(5, 38), new Point2D(6, 48))));
    }

    static List<Series> scatter() {
        List<Point2D> java = new ArrayList<>();
        List<Point2D> browser = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
            double x = 1 + i * 0.32;
            java.add(new Point2D(x, 12 + x * 3.6 + Math.sin(i * 1.7) * 6));
            browser.add(new Point2D(x, 20 + x * 2.1 + Math.cos(i * 1.31) * 7));
        }
        return List.of(new Series("Java", "#61d6c5", java),
                new Series("Browser", "#a99cf5", browser));
    }

    static List<PieChart.Slice> slices() {
        return List.of(
                new PieChart.Slice("Drawing", 38, "#61d6c5"),
                new PieChart.Slice("Charts", 27, "#ffcb6b"),
                new PieChart.Slice("Events", 21, "#a99cf5"),
                new PieChart.Slice("Other", 14, "#ff7f9f"));
    }
}
