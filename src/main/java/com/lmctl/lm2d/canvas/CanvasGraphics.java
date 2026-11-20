package com.lmctl.lm2d.canvas;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Path2D;
import com.lmctl.lm2d.graphics.TextStyle;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

public final class CanvasGraphics implements Graphics2D {
    private final HTMLCanvasElement canvas;
    private final CanvasRenderingContext2D context;
    private int width;
    private int height;

    public CanvasGraphics(HTMLCanvasElement canvas) {
        if (canvas == null) {
            throw new IllegalArgumentException("Canvas is required");
        }
        this.canvas = canvas;
        context = (CanvasRenderingContext2D) canvas.getContext("2d");
        if (context == null) {
            throw new IllegalArgumentException("A Canvas 2D context is not available");
        }
        width = canvas.getWidth();
        height = canvas.getHeight();
    }

    public CanvasRenderingContext2D context() {
        return context;
    }

    public HTMLCanvasElement canvas() {
        return canvas;
    }

    public void setSize(int logicalWidth, int logicalHeight, double pixelRatio) {
        if (logicalWidth <= 0 || logicalHeight <= 0 || !Double.isFinite(pixelRatio) || pixelRatio <= 0) {
            throw new IllegalArgumentException("Positive canvas dimensions and pixel ratio are required");
        }
        width = logicalWidth;
        height = logicalHeight;
        canvas.setWidth((int) Math.round(logicalWidth * pixelRatio));
        canvas.setHeight((int) Math.round(logicalHeight * pixelRatio));
        context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
    }

    @Override
    public void clear() {
        context.clearRect(0, 0, width, height);
    }

    @Override
    public void rectangle(double x, double y, double width, double height, Paint paint) {
        apply(paint);
        context.beginPath();
        context.rect(x, y, width, height);
        finish(paint);
    }

    @Override
    public void circle(double centerX, double centerY, double radius, Paint paint) {
        apply(paint);
        context.beginPath();
        context.arc(centerX, centerY, radius, 0, Math.PI * 2);
        finish(paint);
    }

    @Override
    public void line(double x1, double y1, double x2, double y2, String color, double width) {
        context.beginPath();
        context.moveTo(x1, y1);
        context.lineTo(x2, y2);
        context.setStrokeStyle(color);
        context.setLineWidth(width);
        context.setLineCap("round");
        context.stroke();
    }

    @Override
    public void path(Path2D path, Paint paint) {
        apply(paint);
        context.beginPath();
        replay(path);
        finish(paint);
    }

    @Override
    public void text(double x, double y, String value, TextStyle style) {
        context.setFillStyle(style.color());
        context.setFont(style.weight() + " " + style.size() + "px system-ui, sans-serif");
        context.setTextAlign(style.align());
        context.setTextBaseline(style.baseline());
        context.fillText(value, x, y);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    private void replay(Path2D path) {
        for (Path2D.Command command : path.commands()) {
            double[] v = command.values();
            switch (command.type()) {
                case "M" -> context.moveTo(v[0], v[1]);
                case "L" -> context.lineTo(v[0], v[1]);
                case "Q" -> context.quadraticCurveTo(v[0], v[1], v[2], v[3]);
                case "C" -> context.bezierCurveTo(v[0], v[1], v[2], v[3], v[4], v[5]);
                case "A" -> context.arc(v[0], v[1], v[2], v[3], v[4], v[5] == 1);
                case "Z" -> context.closePath();
                default -> throw new IllegalStateException("Unknown path command: " + command.type());
            }
        }
    }

    private void apply(Paint paint) {
        context.setFillStyle(paint.fill());
        context.setStrokeStyle(paint.stroke());
        context.setLineWidth(paint.strokeWidth());
        context.setGlobalAlpha(paint.opacity());
        context.setLineJoin("round");
        context.setLineCap("round");
    }

    private void finish(Paint paint) {
        if (!paint.fill().equals("none")) {
            context.fill();
        }
        if (!paint.stroke().equals("none") && paint.strokeWidth() > 0) {
            context.stroke();
        }
        context.setGlobalAlpha(1);
    }
}
