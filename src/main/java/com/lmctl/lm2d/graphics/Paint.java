package com.lmctl.lm2d.graphics;

public record Paint(String fill, String stroke, double strokeWidth, double opacity) {
    public Paint {
        if (fill == null || stroke == null || !Double.isFinite(strokeWidth) || strokeWidth < 0
                || !Double.isFinite(opacity) || opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Invalid paint");
        }
    }

    public static Paint fill(String color) {
        return new Paint(color, "none", 0, 1);
    }

    public static Paint stroke(String color, double width) {
        return new Paint("none", color, width, 1);
    }

    public Paint withFill(String color) {
        return new Paint(color, stroke, strokeWidth, opacity);
    }

    public Paint withStroke(String color, double width) {
        return new Paint(fill, color, width, opacity);
    }

    public Paint withOpacity(double value) {
        return new Paint(fill, stroke, strokeWidth, value);
    }
}
