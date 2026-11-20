# lm2d

SVG, HTML Canvas, charts, and plots written in Java and compiled for the browser with TeaVM. The reusable API draws through browser JavaScript APIs without a JavaScript drawing or chart library.

- [Demo book](https://www.lmctl.com/tools/lm2d/demo.html)
- [LLM quick reference](https://www.lmctl.com/tools/lm2d/)
- [LMCode authoring skill](https://www.lmctl.com/tools/lm2d/skill.html)

The demo is a small book rather than a one-page selector. Its eight chapters are separate linked pages: SVG, Canvas, bar charts, line charts, scatter plots, pie charts, animation/events, and LMCode charts.

## Capabilities

- `Graphics2D`, a shared immediate drawing interface for rectangles, circles, lines, paths, and text.
- `SvgGraphics` for retained, namespaced SVG DOM elements, transformed groups, redraw, and root-element access.
- `CanvasGraphics` for immediate Canvas 2D rendering, logical sizing, device pixel ratio, and direct `CanvasRenderingContext2D` access.
- Reusable `Path2D` with move, line, quadratic and cubic Bézier curves, center-parameterized arcs, and close commands. The same path maps to SVG path data or native Canvas calls.
- `Paint`, `TextStyle`, and finite `Point2D` values.
- `LinearScale` with inverted screen ranges and nice ticks, plus padded categorical `BandScale`.
- `BarChart`, `LineChart`, `ScatterPlot`, and `PieChart` with generated axes, grids, labels, and legends. Charts accept either graphics backend.
- `Lm2dHost` typed LMCode functions for clearing, adding, and committing bar-chart data.

This is a focused 2D library, not a browser API compatibility layer or a port of D3. Charts currently use fixed margins, generated numeric ticks, no animation or data-fetching abstraction, and no accessibility table fallback. SVG text measurement and responsive chart relayout remain the application's responsibility.

## Build

Requirements: Java 21, Maven, Node 24.15 or newer, `lmcode-java` commit `b88d3401ee10210a0a6583991d6a81427d5f112a` in the local Maven repository, and LMCode compiler `@lmctl-ai/lmctl` 0.2.482.

```sh
git clone https://github.com/lmctl-ai/lm2d.git
git clone https://github.com/lmctl-ai/lmcode-java.git
(cd lmcode-java && git checkout b88d3401ee10210a0a6583991d6a81427d5f112a && mvn -DskipTests install)
cd lm2d
npm install --prefix compiler @lmctl-ai/lmctl@0.2.482
export LMCODE_COMPILER_MODULE="$PWD/compiler/node_modules/@lmctl-ai/lmctl/dist/dsl.js"
mvn clean package
```

The static demo book is generated under `target/demo/`. Start `demo.html`, then follow the chapter links. Use `mvn install` to make the Java artifact available locally.

To write the demo into another static site:

```sh
mvn -Ddemo.output=/absolute/site/path clean package
```

The chapter HTML sources are deterministic outputs of `scripts/generate-book.py`. Edit the generator and rerun it after changing chapter content. CI verifies that generated pages are synchronized.

## Draw with either backend

```java
import com.lmctl.lm2d.canvas.CanvasGraphics;
import com.lmctl.lm2d.graphics.Paint;
import com.lmctl.lm2d.graphics.Path2D;
import com.lmctl.lm2d.svg.SvgGraphics;

SvgGraphics svg = new SvgGraphics(container, 960, 540);
svg.rectangle(10, 10, 180, 90, Paint.fill("#61d6c5"));
svg.beginGroup("translate(480 270)");
svg.path(new Path2D().moveTo(-80, 0)
    .quadraticCurveTo(0, -120, 80, 0),
    Paint.stroke("#ffcb6b", 4));
svg.endGroup();

CanvasGraphics canvas = new CanvasGraphics(canvasElement);
canvas.setSize(960, 540, Window.current().getDevicePixelRatio());
canvas.circle(480, 270, 80, Paint.fill("#a99cf5"));
canvas.context().setFillStyle("#ffffff");
canvas.context().fillText("Native Canvas API", 430, 275);
```

`SvgGraphics.element()` exposes the root JSO `Element`; `CanvasGraphics.context()` exposes the TeaVM `CanvasRenderingContext2D`. Browser event listeners attach to those elements normally.

## Charts and plots

```java
List<BarChart.Bar> bars = List.of(
    new BarChart.Bar("Mon", 34, "#61d6c5"),
    new BarChart.Bar("Tue", 52, "#ffcb6b"),
    new BarChart.Bar("Wed", 43, "#a99cf5"));
new BarChart("Weekly activity", bars).render(svg);

Series samples = new Series("Java", "#61d6c5", List.of(
    new Point2D(1, 12), new Point2D(2, 24), new Point2D(3, 36)));
new ScatterPlot("Samples", List.of(samples)).render(canvas);
```

`LineChart` and `ScatterPlot` share `Series` and `Point2D`. `PieChart` accepts non-negative `Slice` values and requires a positive total. Chart constructors reject empty or invalid input. `ChartTheme.dark()` is the default; a custom theme can be supplied to every chart.

## LMCode chart handlers

The schema is `src/main/lmcode/charts.schema.json`; handlers are in `src/main/lmcode/charts.lms`.

| Function | Purpose |
| --- | --- |
| `lm2d_clear()` | Clear staged bars |
| `lm2d_bar(label, value, color)` | Append one validated bar |
| `lm2d_use()` | Commit a defensive copy to the Java application |

```text
@release
fn releaseChart() -> void implements ChartHandler {
  lm2d_clear();
  lm2d_bar("Core", 42.0, "#61d6c5");
  lm2d_bar("SVG", 68.0, "#ffcb6b");
  lm2d_use();
}
```

The Maven build compiles LMCode before Java and embeds its bytecode plus exact schema. The demo dispatches by annotation. `@release` and `@components` build charts; `@failure` stages a chart and fails on a second `lm2d_use`, demonstrating failure-atomic replacement.

## Tests

```sh
mvn clean verify
npm ci
npx playwright install --with-deps chromium
npm test
```

The JVM suite covers path conversion, arcs, style validation, scale mapping, ticks, categorical bands, every chart type, negative and single-point data, defensive copies, and LMCode success/failure behavior. The Playwright suite visits the cover and all eight chapters, checks real SVG DOM and Canvas pixels, exercises pointer events, animation pause/resume, LMCode failure safety, mobile overflow, and browser errors. Set `DEMO_URL` to test a deployed book or `PLAYWRIGHT_CHROMIUM_EXECUTABLE` to select an existing browser.

GitHub Actions builds the pinned public LMCode dependencies, checks generated book sources, runs JVM tests, compiles TeaVM, and runs browser integration tests.

## Source layout

- `src/main/java/com/lmctl/lm2d/graphics/` — backend-neutral primitives and paths.
- `src/main/java/com/lmctl/lm2d/svg/` and `canvas/` — browser renderers.
- `src/main/java/com/lmctl/lm2d/chart/` — scales, charts, plots, and themes.
- `src/main/java/com/lmctl/lm2d/lmcode/` — typed Java host functions.
- `src/main/java/com/lmctl/lm2d/demo/` — page dispatcher, examples, and LMCode dispatch.
- `src/main/webapp/` — generated chapter pages, CSS, favicon, and bootstrap.
- `src/main/lmcode/` — schema and annotated handlers, also copied to the demo.
- `scripts/` — deterministic book generator and LMCode compiler bridge.
- `src/test/java/` and `tests/browser.mjs` — JVM and browser integration tests.

## License

MIT. TeaVM is Apache-2.0 licensed; the licenses are compatible. See `LICENSE` and `THIRD_PARTY_NOTICES.md`.
