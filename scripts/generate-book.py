#!/usr/bin/env python3
from html import escape
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/webapp"

CHAPTERS = [
    ("svg", "SVG drawing", "Retained graphics, element by element.", "Create namespaced SVG shapes, groups, text, arcs, and Bézier paths from Java. The result remains ordinary inspectable DOM.", "svg", "Change palette", "SvgGraphics implements the shared Graphics2D interface and also exposes its root Element. Groups can have SVG transforms; clear() rebuilds a scene without replacing the root.", '''SvgGraphics svg = new SvgGraphics(container, 960, 540);
svg.beginGroup("translate(480 300)");
svg.circle(0, 0, 48, Paint.fill("#ffcb6b"));
svg.path(new Path2D().moveTo(0, 0)
    .bezierCurveTo(20, -80, 100, -80, 120, 0),
    Paint.stroke("#61d6c5", 3));
svg.endGroup();'''),
    ("canvas", "Canvas drawing", "Immediate pixels, direct browser API.", "Use the same drawing primitives on an HTML Canvas, or drop down to TeaVM's CanvasRenderingContext2D for gradients and other native operations.", "canvas", "Clear pointer marks", "CanvasGraphics owns no retained scene. Each render call draws immediately. context() exposes the native TeaVM JSO context, while setSize() handles logical dimensions and device pixel ratio.", '''CanvasGraphics canvas = new CanvasGraphics(canvasElement);
canvas.setSize(960, 540, Window.current().getDevicePixelRatio());
CanvasGradient gradient = canvas.context()
    .createLinearGradient(0, 0, 960, 540);
gradient.addColorStop(0, "#102a42");
gradient.addColorStop(1, "#09111e");
canvas.context().setFillStyle(gradient);
canvas.context().fillRect(0, 0, 960, 540);'''),
    ("bar", "Bar charts", "Categories become measured shapes.", "A band scale maps categories to evenly spaced bars. A linear scale, nice ticks, grid lines, and a zero baseline complete this small chart primitive.", "bar", None, "BarChart accepts ordinary Java records and renders to either backend. This chapter uses SVG; no chart-specific JavaScript library or generated SVG string is involved.", '''List<BarChart.Bar> bars = List.of(
    new BarChart.Bar("Mon", 34, "#61d6c5"),
    new BarChart.Bar("Tue", 52, "#ffcb6b"));
BarChart chart = new BarChart("Weekly activity", bars);
chart.render(new SvgGraphics(container, 960, 540));'''),
    ("line", "Line charts", "Shared data, connected points.", "Multiple named series share Cartesian scales and axes. Each series becomes a path with point markers and a generated legend.", "line", None, "Series contains a name, color, and Point2D values. LineChart derives its domain from all points, including single-point domain padding, then draws through Graphics2D.", '''Series series = new Series("TeaVM", "#61d6c5", List.of(
    new Point2D(1, 18), new Point2D(2, 27),
    new Point2D(3, 25), new Point2D(4, 42)));
LineChart chart = new LineChart("Adoption", List.of(series));
chart.render(graphics);'''),
    ("scatter", "Scatter plots", "The same chart model, now on Canvas.", "Fifty-six samples in two series demonstrate that chart layout is independent of the output backend. Circles are immediate Canvas operations here.", "scatter", None, "ScatterPlot uses exactly the same Series and scales as LineChart. Swap SvgGraphics for CanvasGraphics without changing the data or chart object.", '''ScatterPlot plot = new ScatterPlot(
    "Throughput samples", series);
plot.render(new CanvasGraphics(canvasElement));'''),
    ("pie", "Pie charts", "Arc commands become readable proportions.", "Non-negative slices are normalized into arc wedges with percentage labels, separators, and a legend. Path2D translates center-parameterized arcs for SVG.", "pie", None, "The reusable Path2D arc command maps directly to Canvas arc() and to SVG elliptical arc commands. Full circles and clockwise or counterclockwise direction are supported.", '''PieChart chart = new PieChart("Library code", List.of(
    new PieChart.Slice("Drawing", 38, "#61d6c5"),
    new PieChart.Slice("Charts", 27, "#ffcb6b")));
chart.render(graphics);'''),
    ("animation", "Animation and events", "A frame callback and a moving target.", "TeaVM schedules requestAnimationFrame callbacks. Pointer movement updates Java fields, and the pause button demonstrates ordinary browser event handling.", "animation", "Pause animation", "Animation state stays in Java. Each frame clears and redraws the Canvas; pointer coordinates are converted from CSS pixels to the logical scene.", '''Window.requestAnimationFrame(timestamp -> {
    elapsed += deltaSeconds;
    drawScene();
    Window.requestAnimationFrame(this::animate);
});
canvas.onEvent("pointermove", event -> {
    MouseEvent mouse = (MouseEvent) event;
    pointerX = mouse.getOffsetX();
});'''),
    ("lmcode", "LMCode charts", "Annotated handlers call Java hosts.", "LMCode supplies chart data through typed host functions. Java owns the Bar records and replaces the chart only after the handler completes successfully.", "lmcode", None, "The three buttons run @release, @components, and @failure. The last handler stages a chart and then fails on a second lm2d_use call, leaving the visible chart unchanged.", '''@release
fn releaseChart() -> void implements ChartHandler {
  lm2d_clear();
  lm2d_bar("Core", 42.0, "#61d6c5");
  lm2d_bar("SVG", 68.0, "#ffcb6b");
  lm2d_use();
}'''),
]


def header():
    return '''    <header class="site-header">
        <a class="brand" href="demo.html" aria-label="lm2d demo book">lm<span>2d</span></a>
        <nav class="project-nav" aria-label="Project links">
            <a href="/tools/lm2d/">Quick reference</a>
            <a href="https://github.com/lmctl-ai/lm2d">Source on GitHub</a>
        </nav>
    </header>'''


def footer():
    return '''    <footer>
        <p><strong>lm2d</strong> · Java 21 + TeaVM 0.16.0 · MIT licensed</p>
        <p>No JavaScript drawing or chart library. Application code is generated by TeaVM.</p>
    </footer>'''


def document(title, description, body):
    return f'''<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="{escape(description, quote=True)}">
    <title>{escape(title)}</title>
    <link rel="icon" href="favicon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="demo.css">
</head>
{body}
</html>
'''


cards = []
for number, (slug, title, heading, description, *_rest) in enumerate(CHAPTERS, 1):
    cards.append(f'''            <li><a href="demo-{slug}.html"><div><h2>{escape(title)}</h2><p>{escape(description)}</p></div></a></li>''')
cover_body = f'''<body data-page="cover">
{header()}
    <main class="cover">
        <section class="cover-intro">
            <div><p class="eyebrow">A Java and TeaVM demo book</p><h1>Draw the web.<br><span>One chapter at a time.</span></h1></div>
            <p class="lede">Eight focused pages explore SVG, HTML Canvas, reusable charts, animation, and LMCode host functions. Every chapter is a separate page: the page is the selection.</p>
        </section>
        <ol class="chapter-grid">
{chr(10).join(cards)}
        </ol>
    </main>
{footer()}
    <script src="teavm.js"></script>
    <script src="boot.js"></script>
</body>'''
(OUT / "demo.html").write_text(document("lm2d · A Java drawing book", "A multi-page SVG, Canvas, chart, animation, and LMCode demo written in Java and compiled with TeaVM.", cover_body))

for index, (slug, title, heading, description, page, action, note, code) in enumerate(CHAPTERS):
    nav = []
    for other_slug, other_title, *_ in CHAPTERS:
        current = ' aria-current="page"' if other_slug == slug else ''
        nav.append(f'                <li><a href="demo-{other_slug}.html"{current}>{escape(other_title)}</a></li>')
    if page == "canvas" or page == "scatter" or page == "animation":
        scene = '''                <canvas id="scene-canvas" width="960" height="540" aria-label="Interactive Java-generated Canvas demonstration">This chapter requires Canvas 2D support.</canvas>'''
    else:
        scene = f'                <div id="scene" class="scene" aria-label="Java-generated SVG: {escape(title, quote=True)}"></div>'
    buttons = []
    if action:
        pressed = ' aria-pressed="false"' if page == "animation" else ''
        buttons.append(f'                <button id="action" type="button"{pressed}>{escape(action)}</button>')
    if page == "lmcode":
        buttons = [
            '                <button id="lmcode-release" type="button">Run @release</button>',
            '                <button id="lmcode-components" type="button" class="secondary">Run @components</button>',
            '                <button id="lmcode-failure" type="button" class="secondary">Test failure safety</button>',
        ]
    previous_link = '<a href="demo.html">← Book contents</a>' if index == 0 else f'<a href="demo-{CHAPTERS[index - 1][0]}.html">← {escape(CHAPTERS[index - 1][1])}</a>'
    next_link = '<span></span>' if index == len(CHAPTERS) - 1 else f'<a href="demo-{CHAPTERS[index + 1][0]}.html">{escape(CHAPTERS[index + 1][1])} →</a>'
    body = f'''<body data-page="{page}">
{header()}
    <main class="book-layout">
        <nav class="book-nav" aria-label="Demo chapters">
            <p>lm2d demo book</p>
            <ol>
{chr(10).join(nav)}
            </ol>
        </nav>
        <article class="chapter">
            <header class="chapter-heading">
                <p class="eyebrow">Chapter {index + 1:02d} · Java + TeaVM</p>
                <h1>{escape(heading)}</h1>
                <p class="lede">{escape(description)}</p>
            </header>
            <section class="demo-shell" aria-label="Live {escape(title, quote=True)} example">
{scene}
                <div class="controls">
{chr(10).join(buttons)}
                    <p id="status" class="status" role="status" aria-live="polite">Starting the TeaVM application…</p>
                </div>
            </section>
            <section class="chapter-notes">
                <div><h2>How it works</h2><p>{escape(note)}</p></div>
                <pre><code>{escape(code)}</code></pre>
            </section>
            <nav class="pager" aria-label="Chapter navigation">{previous_link}{next_link}</nav>
        </article>
    </main>
{footer()}
    <noscript>This chapter needs JavaScript enabled. TeaVM generates it from Java.</noscript>
    <script src="teavm.js"></script>
    <script src="boot.js"></script>
</body>'''
    (OUT / f"demo-{slug}.html").write_text(document(f"{title} · lm2d", description, body))

print(f"Generated {len(CHAPTERS) + 1} demo book pages")
