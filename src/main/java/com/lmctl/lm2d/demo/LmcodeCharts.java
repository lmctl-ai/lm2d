package com.lmctl.lm2d.demo;

import com.lmctl.lm2d.chart.BarChart;
import com.lmctl.lm2d.lmcode.Lm2dHost;
import io.lmctl.lmcode.Context;
import io.lmctl.lmcode.Program;
import io.lmctl.lmcode.Registry;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

final class LmcodeCharts {
    private LmcodeCharts() {
    }

    static Lm2dHost run(String annotation, Consumer<List<BarChart.Bar>> consumer) {
        Registry registry = new Registry(false);
        Lm2dHost host = new Lm2dHost(registry, consumer);
        Program program = Program.load(Base64.getDecoder().decode(LmcodeChartsProgram.BYTECODE),
                LmcodeChartsProgram.SCHEMA.getBytes(StandardCharsets.UTF_8), registry);
        int entry = findEntry(program, annotation);
        program.call(entry, new Context(), registry, List.of());
        if (!host.used()) {
            throw new IllegalStateException("LMCode handler did not call lm2d_use: @" + annotation);
        }
        return host;
    }

    private static int findEntry(Program program, String annotation) {
        for (int i = 0; i < program.entries().size(); i++) {
            if (program.entries().get(i).annotation().equals(annotation)) {
                return i;
            }
        }
        throw new IllegalStateException("LMCode entry is missing: @" + annotation);
    }
}
