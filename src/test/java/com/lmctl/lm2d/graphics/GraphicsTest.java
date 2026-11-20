package com.lmctl.lm2d.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GraphicsTest {
    @Test
    void pathConvertsEveryCommandToSvg() {
        Path2D path = new Path2D().moveTo(1, 2).lineTo(3, 4)
                .quadraticCurveTo(5, 6, 7, 8)
                .bezierCurveTo(9, 10, 11, 12, 13, 14).close();
        assertEquals("M 1 2 L 3 4 Q 5 6 7 8 C 9 10 11 12 13 14 Z", path.toSvgPathData());
        assertEquals(5, path.commands().size());
        assertThrows(UnsupportedOperationException.class, () -> path.commands().clear());
    }

    @Test
    void clockwiseArcUsesEndpointParameterization() {
        String data = new Path2D().arc(100, 100, 50, 0, Math.PI / 2, false).toSvgPathData();
        assertEquals("M 150 100 A 50 50 0 0 1 100 150", data);
    }

    @Test
    void fullCircleNeedsTwoSvgArcs() {
        String data = new Path2D().arc(0, 0, 10, 0, Math.PI * 2, false).toSvgPathData();
        assertEquals(2, data.split("A").length - 1);
        assertEquals("M 10 0 A 10 10 0 0 1 -10 0 A 10 10 0 0 1 10 0", data);
    }

    @Test
    void pathRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Path2D().moveTo(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new Path2D().arc(0, 0, -1, 0, 1, false));
    }

    @Test
    void paintAndTextStylesValidateAndCopy() {
        Paint paint = Paint.fill("#fff").withStroke("#000", 2).withOpacity(0.5);
        assertEquals("#fff", paint.fill());
        assertEquals(2, paint.strokeWidth());
        assertEquals(0.5, paint.opacity());
        assertThrows(IllegalArgumentException.class, () -> paint.withOpacity(2));
        assertEquals("center", TextStyle.label("#fff", 12).centered().align());
        assertEquals(700, TextStyle.label("#fff", 12).bold().weight());
    }
}
