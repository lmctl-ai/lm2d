package com.lmctl.lm2d.graphics;

public interface Graphics2D {
    void clear();

    void rectangle(double x, double y, double width, double height, Paint paint);

    void circle(double centerX, double centerY, double radius, Paint paint);

    void line(double x1, double y1, double x2, double y2, String color, double width);

    void path(Path2D path, Paint paint);

    void text(double x, double y, String value, TextStyle style);

    int width();

    int height();
}
