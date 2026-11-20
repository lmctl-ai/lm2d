package com.lmctl.lm2d.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lmctl.lm2d.chart.BarChart;
import com.lmctl.lm2d.lmcode.Lm2dHost;
import io.lmctl.lmcode.Context;
import io.lmctl.lmcode.Registry;
import io.lmctl.lmcode.Value;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class LmcodeChartsTest {
    @Test
    void releaseHandlerBuildsFourBars() {
        AtomicReference<List<BarChart.Bar>> result = new AtomicReference<>();
        Lm2dHost host = LmcodeCharts.run("release", result::set);
        assertTrue(host.used());
        assertEquals(4, result.get().size());
        assertEquals("Core", result.get().get(0).label());
        assertEquals(68, result.get().get(1).value());
        assertEquals("#ff7f9f", result.get().get(3).color());
    }

    @Test
    void componentsHandlerUsesDifferentData() {
        AtomicReference<List<BarChart.Bar>> result = new AtomicReference<>();
        LmcodeCharts.run("components", result::set);
        assertEquals("Scales", result.get().get(0).label());
        assertEquals(73, result.get().get(2).value());
    }

    @Test
    void missingAnnotationIsRejected() {
        assertThrows(IllegalStateException.class, () -> LmcodeCharts.run("missing", bars -> { }));
    }

    @Test
    void failureAfterStagingIsReported() {
        AtomicReference<List<BarChart.Bar>> staged = new AtomicReference<>();
        assertThrows(RuntimeException.class, () -> LmcodeCharts.run("failure", staged::set));
        assertEquals(1, staged.get().size());
    }

    @Test
    void emptyUseIsRejected() {
        Registry registry = new Registry(false);
        new Lm2dHost(registry, bars -> { });
        var use = registry.resolve("fn lm2d_use() -> void").callback;
        assertThrows(IllegalStateException.class,
                () -> use.call(new Context(), Value.none(), new Value[0]));
    }
}
