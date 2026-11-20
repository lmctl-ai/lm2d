package com.lmctl.lm2d.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Path2D;
import com.lmctl.lm2d.graphics.Point2D;
import com.lmctl.lm2d.graphics.TextStyle;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChartTest {
    private static final class RecordingGraphics implements Graphics2D {
        private final List<String> operations = new ArrayList<>();

        @Override
        public void clear() {
            operations.add("clear");
        }

        @Override
        public void rectangle(double x, double y, double width, double height, Paint paint) {
            operations.add("rect");
        }

        @Override
        public void circle(double centerX, double centerY, double radius, Paint paint) {
            operations.add("circle");
        }

        @Override
        public void line(double x1, double y1, double x2, double y2, String color, double width) {
            operations.add("line");
        }

        @Override
        public void path(Path2D path, Paint paint) {
            operations.add("path");
        }

        @Override
        public void text(double x, double y, String value, TextStyle style) {
            operations.add("text:" + value);
        }

        @Override
        public int width() {
            return 960;
        }

        @Override
        public int height() {
            return 540;
        }

        private long count(String operation) {
            return operations.stream().filter(item -> item.equals(operation)).count();
        }
    }

    @Test
    void barChartDrawsBarsIncludingNegativeValues() {
        RecordingGraphics graphics = new RecordingGraphics();
        new BarChart("Balance", List.of(new BarChart.Bar("A", -5, "#fff"),
                new BarChart.Bar("B", 15, "#000"))).render(graphics);
        assertEquals(3, graphics.count("rect"));
        assertTrue(graphics.operations.contains("text:A"));
        assertTrue(graphics.count("line") >= 3);
    }

    @Test
    void lineChartDrawsOnePathAndMarkersPerSeries() {
        RecordingGraphics graphics = new RecordingGraphics();
        List<Series> series = List.of(
                new Series("A", "#fff", List.of(new Point2D(1, 2), new Point2D(2, 4))),
                new Series("B", "#000", List.of(new Point2D(1, 3), new Point2D(2, 1))));
        new LineChart("Lines", series).render(graphics);
        assertEquals(2, graphics.count("path"));
        assertEquals(6, graphics.count("circle"));
        assertTrue(graphics.operations.contains("text:A"));
    }

    @Test
    void scatterPlotDrawsEveryPoint() {
        RecordingGraphics graphics = new RecordingGraphics();
        Series series = new Series("Samples", "#fff",
                List.of(new Point2D(1, 2), new Point2D(2, 4), new Point2D(3, 8)));
        new ScatterPlot("Samples", List.of(series)).render(graphics);
        assertEquals(4, graphics.count("circle"));
    }

    @Test
    void pieChartNormalizesSlicesAndRejectsInvalidData() {
        RecordingGraphics graphics = new RecordingGraphics();
        new PieChart("Share", List.of(new PieChart.Slice("A", 1, "#fff"),
                new PieChart.Slice("B", 3, "#000"))).render(graphics);
        assertEquals(2, graphics.count("path"));
        assertTrue(graphics.operations.contains("text:25%"));
        assertTrue(graphics.operations.contains("text:75%"));
        assertThrows(IllegalArgumentException.class,
                () -> new PieChart("Empty", List.of(new PieChart.Slice("Zero", 0, "#fff"))));
        assertThrows(IllegalArgumentException.class, () -> new PieChart.Slice("Bad", -1, "#fff"));
    }

    @Test
    void singlePointSeriesReceivesNonEmptyDomains() {
        RecordingGraphics graphics = new RecordingGraphics();
        Series one = new Series("One", "#fff", List.of(new Point2D(5, 5)));
        new LineChart("One", List.of(one)).render(graphics);
        assertEquals(1, graphics.count("path"));
    }

    @Test
    void emptyChartsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BarChart("Empty", List.of()));
        assertThrows(IllegalArgumentException.class, () -> new LineChart("Empty", List.of()));
    }
}
