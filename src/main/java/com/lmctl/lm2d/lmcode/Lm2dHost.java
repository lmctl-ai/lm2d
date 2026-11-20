package com.lmctl.lm2d.lmcode;

import com.lmctl.lm2d.chart.BarChart;
import io.lmctl.lmcode.Registry;
import io.lmctl.lmcode.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Lm2dHost {
    private final List<BarChart.Bar> bars = new ArrayList<>();
    private final Consumer<List<BarChart.Bar>> barsConsumer;
    private boolean used;

    public Lm2dHost(Registry registry, Consumer<List<BarChart.Bar>> barsConsumer) {
        if (registry == null || barsConsumer == null) {
            throw new IllegalArgumentException("LMCode registry and bars consumer must not be null");
        }
        this.barsConsumer = barsConsumer;
        registry.function("lm2d_bar", List.of("string", "float", "string"), "void", (context, self, args) -> {
            bars.add(new BarChart.Bar(string(args[0]), (float) args[1].asFloat(), string(args[2])));
            return Value.none();
        });
        registry.function("lm2d_clear", List.of(), "void", (context, self, args) -> {
            bars.clear();
            return Value.none();
        });
        registry.function("lm2d_use", List.of(), "void", (context, self, args) -> {
            if (used) {
                throw new IllegalStateException("lm2d_use may only be called once");
            }
            if (bars.isEmpty()) {
                throw new IllegalStateException("lm2d_use requires at least one bar");
            }
            barsConsumer.accept(List.copyOf(bars));
            used = true;
            return Value.none();
        });
    }

    public List<BarChart.Bar> bars() {
        return List.copyOf(bars);
    }

    public boolean used() {
        return used;
    }

    private static String string(Value value) {
        return value.asString().toString();
    }
}
