package com.lmctl.lm2d.graphics;

public record Point2D(double x, double y) {
    public Point2D {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Point coordinates must be finite");
        }
    }
}
