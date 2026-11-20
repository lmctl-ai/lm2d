package com.lmctl.lm2d.graphics;

public record TextStyle(String color, double size, String align, String baseline, int weight) {
    public TextStyle {
        if (color == null || !Double.isFinite(size) || size <= 0 || align == null || baseline == null
                || weight < 100 || weight > 900) {
            throw new IllegalArgumentException("Invalid text style");
        }
    }

    public static TextStyle label(String color, double size) {
        return new TextStyle(color, size, "start", "alphabetic", 400);
    }

    public TextStyle centered() {
        return new TextStyle(color, size, "center", baseline, weight);
    }

    public TextStyle middle() {
        return new TextStyle(color, size, align, "middle", weight);
    }

    public TextStyle bold() {
        return new TextStyle(color, size, align, baseline, 700);
    }
}
