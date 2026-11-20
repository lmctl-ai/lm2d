package com.lmctl.lm2d.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleTest {
    private static final double DELTA = 0.000001;

    @Test
    void linearScaleMapsAndSupportsInvertedScreenRange() {
        LinearScale scale = new LinearScale(0, 100, 500, 100);
        assertEquals(500, scale.map(0), DELTA);
        assertEquals(100, scale.map(100), DELTA);
        assertEquals(300, scale.map(50), DELTA);
    }

    @Test
    void ticksUseNiceRoundSteps() {
        assertEquals(List.of(0.0, 20.0, 40.0, 60.0, 80.0, 100.0),
                new LinearScale(0, 100, 0, 1).ticks(6));
        assertEquals(List.of(0.0, 0.2, 0.4, 0.6, 0.8, 1.0),
                new LinearScale(0, 1, 0, 1).ticks(6));
    }

    @Test
    void bandScaleUsesPaddingAndStableCenters() {
        BandScale scale = new BandScale(List.of("A", "B", "C", "D"), 100, 500, 0.2);
        assertEquals(80, scale.bandwidth(), DELTA);
        assertEquals(110, scale.start(0), DELTA);
        assertEquals(150, scale.center(0), DELTA);
        assertEquals(450, scale.center(3), DELTA);
    }

    @Test
    void invalidScalesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new LinearScale(1, 1, 0, 100));
        assertThrows(IllegalArgumentException.class, () -> new BandScale(List.of(), 0, 100, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new LinearScale(0, 1, 0, 1).ticks(1));
    }

    @Test
    void formattingTrimsUnneededDecimals() {
        assertEquals("42", LinearScale.format(42));
        assertEquals("0.25", LinearScale.format(0.25));
        assertEquals("-3.5", LinearScale.format(-3.5));
    }
}
