package com.lmctl.lm2d.svg;

import com.lmctl.lm2d.graphics.Graphics2D;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Path2D;
import com.lmctl.lm2d.graphics.TextStyle;
import java.util.ArrayDeque;
import java.util.Deque;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Document;
import org.teavm.jso.dom.xml.Element;

public final class SvgGraphics implements Graphics2D {
    public static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private final Element root;
    private final Deque<Element> parents = new ArrayDeque<>();
    private Element parent;

    public SvgGraphics(HTMLElement container, int width, int height) {
        if (container == null || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("SVG container and positive dimensions are required");
        }
        Document document = container.getOwnerDocument();
        root = document.createElementNS(SVG_NAMESPACE, "svg");
        root.setAttribute("viewBox", "0 0 " + width + " " + height);
        root.setAttribute("width", Integer.toString(width));
        root.setAttribute("height", Integer.toString(height));
        root.setAttribute("role", "img");
        container.appendChild(root);
        parent = root;
    }

    public Element element() {
        return root;
    }

    public Element beginGroup(String transform) {
        Element group = root.getOwnerDocument().createElementNS(SVG_NAMESPACE, "g");
        if (transform != null && !transform.isEmpty()) {
            group.setAttribute("transform", transform);
        }
        parent.appendChild(group);
        parents.push(parent);
        parent = group;
        return group;
    }

    public void endGroup() {
        if (parents.isEmpty()) {
            throw new IllegalStateException("No SVG group is open");
        }
        parent = parents.pop();
    }

    @Override
    public void clear() {
        while (root.getFirstChild() != null) {
            root.removeChild(root.getFirstChild());
        }
        parents.clear();
        parent = root;
    }

    @Override
    public void rectangle(double x, double y, double width, double height, Paint paint) {
        Element element = create("rect");
        element.setAttribute("x", Path2D.number(x));
        element.setAttribute("y", Path2D.number(y));
        element.setAttribute("width", Path2D.number(width));
        element.setAttribute("height", Path2D.number(height));
        apply(element, paint);
    }

    @Override
    public void circle(double centerX, double centerY, double radius, Paint paint) {
        Element element = create("circle");
        element.setAttribute("cx", Path2D.number(centerX));
        element.setAttribute("cy", Path2D.number(centerY));
        element.setAttribute("r", Path2D.number(radius));
        apply(element, paint);
    }

    @Override
    public void line(double x1, double y1, double x2, double y2, String color, double width) {
        Element element = create("line");
        element.setAttribute("x1", Path2D.number(x1));
        element.setAttribute("y1", Path2D.number(y1));
        element.setAttribute("x2", Path2D.number(x2));
        element.setAttribute("y2", Path2D.number(y2));
        element.setAttribute("stroke", color);
        element.setAttribute("stroke-width", Path2D.number(width));
        element.setAttribute("stroke-linecap", "round");
    }

    @Override
    public void path(Path2D path, Paint paint) {
        Element element = create("path");
        element.setAttribute("d", path.toSvgPathData());
        apply(element, paint);
    }

    @Override
    public void text(double x, double y, String value, TextStyle style) {
        Element element = create("text");
        element.setAttribute("x", Path2D.number(x));
        element.setAttribute("y", Path2D.number(y));
        element.setAttribute("fill", style.color());
        element.setAttribute("font-size", Path2D.number(style.size()));
        element.setAttribute("font-family", "system-ui, sans-serif");
        element.setAttribute("font-weight", Integer.toString(style.weight()));
        element.setAttribute("text-anchor", svgAlign(style.align()));
        element.setAttribute("dominant-baseline", svgBaseline(style.baseline()));
        element.setTextContent(value);
    }

    @Override
    public int width() {
        return Integer.parseInt(root.getAttribute("width"));
    }

    @Override
    public int height() {
        return Integer.parseInt(root.getAttribute("height"));
    }

    private Element create(String name) {
        Element element = root.getOwnerDocument().createElementNS(SVG_NAMESPACE, name);
        parent.appendChild(element);
        return element;
    }

    private static void apply(Element element, Paint paint) {
        element.setAttribute("fill", paint.fill());
        element.setAttribute("stroke", paint.stroke());
        element.setAttribute("stroke-width", Path2D.number(paint.strokeWidth()));
        element.setAttribute("opacity", Path2D.number(paint.opacity()));
        element.setAttribute("stroke-linejoin", "round");
        element.setAttribute("stroke-linecap", "round");
    }

    private static String svgAlign(String align) {
        return switch (align) {
            case "center" -> "middle";
            case "right", "end" -> "end";
            default -> "start";
        };
    }

    private static String svgBaseline(String baseline) {
        return switch (baseline) {
            case "middle" -> "middle";
            case "top" -> "text-before-edge";
            default -> "alphabetic";
        };
    }
}
