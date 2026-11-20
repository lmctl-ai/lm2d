package com.lmctl.lm2d.chart;

import com.lmctl.lm2d.graphics.Point2D;
import java.util.List;

public record Series(String name, String color, List<Point2D> points) {
    public Series {
        if (name == null || color == null || points == null || points.isEmpty()) {
            throw new IllegalArgumentException("A series requires a name, color, and points");
        }
        points = List.copyOf(points);
    }
}
