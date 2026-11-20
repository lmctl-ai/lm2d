import assert from 'node:assert/strict';
import { readFile } from 'node:fs';
import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { chromium } from 'playwright';

const repository = fileURLToPath(new URL('../', import.meta.url));
const root = path.join(repository, 'target/demo');
const types = { '.css': 'text/css', '.html': 'text/html', '.js': 'text/javascript', '.svg': 'image/svg+xml', '.json': 'application/json', '.lms': 'text/plain' };
const server = http.createServer((request, response) => {
  const pathname = decodeURIComponent(new URL(request.url, 'http://localhost').pathname);
  const file = path.resolve(root, '.' + (pathname === '/' ? '/demo.html' : pathname));
  if (!file.startsWith(root + path.sep)) {
    response.writeHead(403).end();
    return;
  }
  readFile(file, (error, content) => {
    if (error) {
      response.writeHead(404).end();
      return;
    }
    response.writeHead(200, { 'Content-Type': types[path.extname(file)] || 'application/octet-stream' });
    response.end(content);
  });
});
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
const address = server.address();
const base = process.env.DEMO_URL ?? `http://127.0.0.1:${address.port}/demo.html`;
const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || undefined
});
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
const errors = [];
const requests = [];
page.on('pageerror', error => errors.push(`pageerror: ${error.message}`));
page.on('console', message => {
  if (message.type() === 'error') errors.push(`console: ${message.text()}`);
});
page.on('response', response => {
  if (response.status() >= 400) errors.push(`HTTP ${response.status()}: ${response.url()}`);
});
page.on('request', request => requests.push(request.url()));

const chapterUrl = slug => new URL(`demo-${slug}.html`, base).href;
async function open(slug, ready) {
  await page.goto(chapterUrl(slug));
  await page.waitForFunction(text => document.querySelector('#status')?.textContent.startsWith(text), ready);
  assert.equal(await page.locator('select').count(), 0, `${slug} uses a page, not a selector`);
  assert.deepEqual(await page.locator('script[src]').evaluateAll(scripts => scripts.map(script => script.getAttribute('src'))), ['teavm.js', 'boot.js']);
  assert.equal(await page.locator('.book-nav a').count(), 8);
  assert.equal(await page.locator('.book-nav a[aria-current="page"]').count(), 1);
}
async function canvasImage() {
  return page.locator('#scene-canvas').evaluate(canvas => canvas.toDataURL());
}
async function canvasColors() {
  return page.locator('#scene-canvas').evaluate(canvas => {
    const context = canvas.getContext('2d');
    const pixels = context.getImageData(0, 0, canvas.width, canvas.height).data;
    const colors = new Set();
    for (let i = 0; i < pixels.length; i += 400) colors.add(`${pixels[i]},${pixels[i + 1]},${pixels[i + 2]},${pixels[i + 3]}`);
    return colors.size;
  });
}

await page.goto(base);
assert.equal(await page.locator('.chapter-grid a').count(), 8);
assert.deepEqual(await page.locator('script[src]').evaluateAll(scripts => scripts.map(script => script.getAttribute('src'))), ['teavm.js', 'boot.js']);
assert.equal(await page.locator('select').count(), 0);
await page.screenshot({ path: path.join(repository, 'target/lm2d-book-cover.png'), fullPage: true });

await open('svg', 'Ready: SVG paths and groups');
assert.equal(await page.locator('#scene svg').count(), 1);
assert.equal(await page.locator('#scene svg path').count(), 7);
assert.equal(await page.locator('#scene svg g').count(), 1);
const svgBefore = await page.locator('#scene svg').innerHTML();
await page.locator('#action').click();
assert.match(await page.locator('#status').innerText(), /alternate palette/);
assert.notEqual(await page.locator('#scene svg').innerHTML(), svgBefore);
assert.equal(await page.locator('#scene svg').count(), 1, 'redraw retains the SVG root');
await page.screenshot({ path: path.join(repository, 'target/lm2d-svg.png'), fullPage: true });

await open('canvas', 'Ready: Canvas immediate-mode drawing');
assert.ok(await canvasColors() > 20, 'Canvas contains real gradients and shapes');
await page.locator('#scene-canvas').click({ position: { x: 300, y: 180 } });
assert.match(await page.locator('#status').innerText(), /1 pointer mark/);
await page.locator('#scene-canvas').click({ position: { x: 350, y: 220 } });
assert.match(await page.locator('#status').innerText(), /2 pointer mark/);
await page.locator('#action').click();
assert.match(await page.locator('#status').innerText(), /Canvas cleared/);

await open('bar', 'Ready: bar chart with 6 bars');
assert.equal(await page.locator('#scene svg rect').count(), 7);
assert.ok(await page.locator('#scene svg line').count() >= 5);
assert.equal(await page.locator('#scene svg text').filter({ hasText: 'Weekly rendering activity' }).count(), 1);

await open('line', 'Ready: line chart with 2 series');
assert.equal(await page.locator('#scene svg path').count(), 2);
assert.equal(await page.locator('#scene svg circle').count(), 14);
assert.equal(await page.locator('#scene svg text').filter({ hasText: 'LMCode' }).count(), 1);
await page.screenshot({ path: path.join(repository, 'target/lm2d-line-chart.png'), fullPage: true });

await open('scatter', 'Ready: scatter plot with 56 samples');
assert.ok(await canvasColors() > 20);

await open('pie', 'Ready: pie chart with 4 arc wedges');
assert.equal(await page.locator('#scene svg path').count(), 4);
assert.equal(await page.locator('#scene svg text').filter({ hasText: '38%' }).count(), 1);
assert.equal(await page.locator('#scene svg text').filter({ hasText: 'Drawing' }).count(), 1);

await open('animation', 'Ready: requestAnimationFrame animation');
const moving1 = await canvasImage();
await page.waitForTimeout(180);
assert.notEqual(await canvasImage(), moving1, 'requestAnimationFrame redraws the scene');
await page.locator('#action').click();
assert.equal(await page.locator('#action').innerText(), 'Resume animation');
await page.waitForTimeout(80);
const stopped1 = await canvasImage();
await page.waitForTimeout(180);
assert.equal(await canvasImage(), stopped1, 'pause freezes animation frames');
const bounds = await page.locator('#scene-canvas').boundingBox();
await page.mouse.move(bounds.x + bounds.width * 0.3, bounds.y + bounds.height * 0.4);
await page.locator('#action').press('Space');
assert.equal(await page.locator('#action').innerText(), 'Pause animation');
await page.waitForTimeout(100);
assert.notEqual(await canvasImage(), stopped1, 'keyboard resumes animation');

await open('lmcode', 'Ready: LMCode @release');
assert.equal(await page.locator('#scene svg rect').count(), 5);
assert.equal(await page.locator('#scene svg text').filter({ hasText: 'Authored by LMCode @release' }).count(), 1);
const release = await page.locator('#scene svg').innerHTML();
await page.locator('#lmcode-components').click();
assert.match(await page.locator('#status').innerText(), /@components produced 4 bars/);
const components = await page.locator('#scene svg').innerHTML();
assert.notEqual(components, release);
await page.locator('#lmcode-failure').click();
assert.match(await page.locator('#status').innerText(), /previous chart is still visible/);
assert.equal(await page.locator('#scene svg').innerHTML(), components, 'failed LMCode does not replace the chart');

await page.goto(base);
await page.setViewportSize({ width: 390, height: 844 });
assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), 'cover has no mobile overflow');
await open('svg', 'Ready: SVG paths and groups');
assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), 'chapter has no mobile overflow');
await page.screenshot({ path: path.join(repository, 'target/lm2d-mobile.png'), fullPage: true });

assert.deepEqual(errors, []);
const allowedOrigins = new Set([new URL(base).origin]);
assert.deepEqual(requests.filter(url => !allowedOrigins.has(new URL(url).origin)), [], 'book loads no third-party resources');
console.log(JSON.stringify({ pages: 9, chapters: ['SVG', 'Canvas', 'bar', 'line', 'scatter', 'pie', 'animation', 'LMCode'], svg: 'DOM shapes, groups, paths, palette redraw', canvas: 'pixels, gradients, pointer marks', charts: '6 bars, 2 lines, 56 samples, 4 wedges', animation: 'frames, pause, keyboard resume', lmcode: '@release, @components, failure-atomic replacement', mobile: 'no horizontal overflow', errors }, null, 2));
await browser.close();
server.close();
