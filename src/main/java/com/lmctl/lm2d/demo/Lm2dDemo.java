package com.lmctl.lm2d.demo;

import com.lmctl.lm2d.canvas.CanvasGraphics;
import com.lmctl.lm2d.chart.BarChart;
import com.lmctl.lm2d.chart.LineChart;
import com.lmctl.lm2d.chart.PieChart;
import com.lmctl.lm2d.chart.ScatterPlot;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Path2D;
import com.lmctl.lm2d.graphics.Point2D;
import com.lmctl.lm2d.graphics.TextStyle;
import com.lmctl.lm2d.svg.SvgGraphics;
import java.util.ArrayList;
import java.util.List;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasGradient;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.html.HTMLButtonElement;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public final class Lm2dDemo {
    private static final int WIDTH = 960;
    private static final int HEIGHT = 540;
    private static HTMLDocument document;
    private static HTMLElement status;
    private static boolean alternate;
    private static boolean paused;
    private static double elapsed;
    private static double previousTimestamp;
    private static double pointerX = WIDTH / 2;
    private static double pointerY = HEIGHT / 2;
    private static final List<Point2D> canvasMarks = new ArrayList<>();
    private static CanvasGraphics animationGraphics;

    private Lm2dDemo() {
    }

    public static void main(String[] args) {
        document = Window.current().getDocument();
        status = document.getElementById("status");
        try {
            String page = document.getBody().getAttribute("data-page");
            switch (page) {
                case "cover" -> { }
                case "svg" -> svg();
                case "canvas" -> canvas();
                case "bar" -> barChart();
                case "line" -> lineChart();
                case "scatter" -> scatterPlot();
                case "pie" -> pieChart();
                case "animation" -> animation();
                case "lmcode" -> lmcode();
                default -> throw new IllegalArgumentException("Unknown lm2d demo page: " + page);
            }
        } catch (RuntimeException e) {
            status.setInnerText("Unable to start this chapter: " + e.getMessage());
            throw e;
        }
    }

    private static void svg() {
        SvgGraphics graphics = new SvgGraphics(scene(), WIDTH, HEIGHT);
        HTMLButtonElement action = button("action");
        action.onClick(event -> {
            alternate = !alternate;
            drawSvg(graphics);
            status.setInnerText("Ready: SVG " + (alternate ? "alternate palette" : "paths and groups")
                    + ". Java rebuilt the retained SVG DOM.");
        });
        drawSvg(graphics);
        status.setInnerText("Ready: SVG paths and groups. Every shape is a namespaced DOM element created by Java.");
    }

    private static void drawSvg(SvgGraphics graphics) {
        graphics.clear();
        graphics.rectangle(0, 0, WIDTH, HEIGHT, Paint.fill("#0b1726"));
        graphics.text(42, 56, "Retained SVG, constructed by Java", TextStyle.label("#edf5ff", 25).bold());
        graphics.text(42, 84, "Paths, groups, strokes, opacity, and accessible DOM output",
                TextStyle.label("#91a5bd", 14));
        graphics.beginGroup("translate(480 300) rotate(" + (alternate ? "18" : "-8") + ")");
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            double x = Math.cos(angle) * 150;
            double y = Math.sin(angle) * 150;
            Path2D petal = new Path2D().moveTo(0, 0)
                    .bezierCurveTo(x * 0.2 - 65, y * 0.2 - 65, x + 65, y - 65, x, y)
                    .bezierCurveTo(x - 65, y + 65, x * 0.2 + 65, y * 0.2 + 65, 0, 0).close();
            graphics.path(petal, Paint.fill(alternate ? "#a99cf5" : "#61d6c5")
                    .withStroke("#edf5ff", 1.5).withOpacity(0.18 + i * 0.07));
        }
        graphics.circle(0, 0, 47, Paint.fill(alternate ? "#ffcb6b" : "#ff7f9f"));
        graphics.endGroup();
        Path2D curve = new Path2D().moveTo(40, 455)
                .quadraticCurveTo(250, 350, 430, 455).quadraticCurveTo(660, 565, 920, 435);
        graphics.path(curve, Paint.stroke(alternate ? "#61d6c5" : "#ffcb6b", 3));
    }

    private static void canvas() {
        CanvasGraphics graphics = canvasGraphics();
        drawCanvas(graphics);
        graphics.canvas().onEvent("pointerdown", event -> {
            MouseEvent mouse = (MouseEvent) event;
            canvasMarks.add(new Point2D(mouse.getOffsetX() * WIDTH / graphics.canvas().getOffsetWidth(),
                    mouse.getOffsetY() * HEIGHT / graphics.canvas().getOffsetHeight()));
            drawCanvas(graphics);
            status.setInnerText("Ready: Canvas drawing with " + canvasMarks.size() + " pointer mark(s).");
        });
        button("action").onClick(event -> {
            canvasMarks.clear();
            drawCanvas(graphics);
            status.setInnerText("Ready: Canvas cleared. Click the drawing to add a pointer mark.");
        });
        status.setInnerText("Ready: Canvas immediate-mode drawing. Click the scene to add a pointer mark.");
    }

    private static void drawCanvas(CanvasGraphics graphics) {
        graphics.clear();
        CanvasGradient gradient = graphics.context().createLinearGradient(0, 0, WIDTH, HEIGHT);
        gradient.addColorStop(0, "#102a42");
        gradient.addColorStop(1, "#09111e");
        graphics.context().setFillStyle(gradient);
        graphics.context().fillRect(0, 0, WIDTH, HEIGHT);
        graphics.text(42, 56, "HTML Canvas, driven by Java", TextStyle.label("#edf5ff", 25).bold());
        graphics.text(42, 84, "Native gradients, arcs, Bézier paths, and pointer events",
                TextStyle.label("#91a5bd", 14));
        for (int i = 0; i < 11; i++) {
            double x = 90 + i * 77;
            double y = 295 + Math.sin(i * 0.82) * 105;
            graphics.circle(x, y, 23 + i % 3 * 8,
                    Paint.fill(i % 2 == 0 ? "#61d6c5" : "#a99cf5").withOpacity(0.65));
            if (i > 0) {
                double previousX = 90 + (i - 1) * 77;
                double previousY = 295 + Math.sin((i - 1) * 0.82) * 105;
                graphics.line(previousX, previousY, x, y, "#ffcb6b", 3);
            }
        }
        for (Point2D mark : canvasMarks) {
            graphics.circle(mark.x(), mark.y(), 13, Paint.fill("#ff7f9f").withStroke("#ffffff", 2));
        }
    }

    private static void barChart() {
        SvgGraphics graphics = new SvgGraphics(scene(), WIDTH, HEIGHT);
        new BarChart("Weekly rendering activity", ExampleData.bars()).render(graphics);
        status.setInnerText("Ready: bar chart with 6 bars, band scale, zero baseline, and generated axes.");
    }

    private static void lineChart() {
        SvgGraphics graphics = new SvgGraphics(scene(), WIDTH, HEIGHT);
        new LineChart("Adoption over six releases", ExampleData.lines()).render(graphics);
        status.setInnerText("Ready: line chart with 2 series, 12 points, linear scales, grid, and legend.");
    }

    private static void scatterPlot() {
        CanvasGraphics graphics = canvasGraphics();
        new ScatterPlot("Throughput samples", ExampleData.scatter()).render(graphics);
        status.setInnerText("Ready: scatter plot with 56 samples rendered through the Canvas 2D API.");
    }

    private static void pieChart() {
        SvgGraphics graphics = new SvgGraphics(scene(), WIDTH, HEIGHT);
        new PieChart("Where the library spends its code", ExampleData.slices()).render(graphics);
        status.setInnerText("Ready: pie chart with 4 arc wedges, percentages, and a legend.");
    }

    private static void animation() {
        animationGraphics = canvasGraphics();
        animationGraphics.canvas().onEvent("pointermove", event -> {
            MouseEvent mouse = (MouseEvent) event;
            pointerX = mouse.getOffsetX() * WIDTH / animationGraphics.canvas().getOffsetWidth();
            pointerY = mouse.getOffsetY() * HEIGHT / animationGraphics.canvas().getOffsetHeight();
        });
        button("action").onClick(event -> {
            paused = !paused;
            previousTimestamp = 0;
            button("action").setInnerText(paused ? "Resume animation" : "Pause animation");
            button("action").setAttribute("aria-pressed", Boolean.toString(paused));
            status.setInnerText(paused ? "Animation paused. Pointer tracking remains active."
                    : "Animation resumed through requestAnimationFrame.");
        });
        drawAnimation();
        status.setInnerText("Ready: requestAnimationFrame animation. Move the pointer to attract the particles.");
        Window.requestAnimationFrame(Lm2dDemo::animate);
    }

    private static void animate(double timestamp) {
        if (!paused) {
            if (previousTimestamp > 0) {
                elapsed += Math.min(0.05, (timestamp - previousTimestamp) / 1000);
            }
            previousTimestamp = timestamp;
            drawAnimation();
        }
        Window.requestAnimationFrame(Lm2dDemo::animate);
    }

    private static void drawAnimation() {
        CanvasGraphics graphics = animationGraphics;
        graphics.clear();
        graphics.rectangle(0, 0, WIDTH, HEIGHT, Paint.fill("#0b1726"));
        graphics.text(38, 52, "A little motion, no JavaScript framework", TextStyle.label("#edf5ff", 23).bold());
        for (int i = 0; i < 24; i++) {
            double phase = elapsed * (0.6 + i % 5 * 0.08) + i * 0.63;
            double orbit = 70 + i * 6;
            double targetX = pointerX + Math.cos(phase) * orbit;
            double targetY = pointerY + Math.sin(phase * 1.13) * orbit * 0.55;
            graphics.circle(targetX, targetY, 5 + i % 4,
                    Paint.fill(i % 3 == 0 ? "#ffcb6b" : i % 3 == 1 ? "#61d6c5" : "#a99cf5")
                            .withOpacity(0.55 + i % 3 * 0.15));
        }
        graphics.circle(pointerX, pointerY, 16, Paint.stroke("#edf5ff", 2));
    }

    private static void lmcode() {
        SvgGraphics graphics = new SvgGraphics(scene(), WIDTH, HEIGHT);
        runLmcode(graphics, "release");
        button("lmcode-release").onClick(event -> runLmcode(graphics, "release"));
        button("lmcode-components").onClick(event -> runLmcode(graphics, "components"));
        button("lmcode-failure").onClick(event -> runLmcode(graphics, "failure"));
    }

    private static void runLmcode(SvgGraphics graphics, String annotation) {
        List<BarChart.Bar>[] staged = new List[1];
        try {
            LmcodeCharts.run(annotation, bars -> staged[0] = bars);
            graphics.clear();
            new BarChart("Authored by LMCode @" + annotation, staged[0]).render(graphics);
            status.setInnerText("Ready: LMCode @" + annotation + " produced " + staged[0].size()
                    + " bars through typed Java host functions.");
        } catch (RuntimeException e) {
            status.setInnerText("LMCode @" + annotation + " failed: " + e.getMessage()
                    + ". The previous chart is still visible.");
        }
    }

    private static CanvasGraphics canvasGraphics() {
        HTMLCanvasElement canvas = (HTMLCanvasElement) document.getElementById("scene-canvas");
        CanvasGraphics graphics = new CanvasGraphics(canvas);
        graphics.setSize(WIDTH, HEIGHT, Window.current().getDevicePixelRatio());
        return graphics;
    }

    private static HTMLElement scene() {
        return document.getElementById("scene");
    }

    private static HTMLButtonElement button(String id) {
        return (HTMLButtonElement) document.getElementById(id);
    }
}
