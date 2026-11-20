package com.lmctl.lm2d.chart;

import java.util.List;

public final class BandScale {
    private final List<String> categories;
    private final double rangeMin;
    private final double rangeMax;
    private final double padding;

    public BandScale(List<String> categories, double rangeMin, double rangeMax, double padding) {
        if (categories == null || categories.isEmpty() || !Double.isFinite(rangeMin) || !Double.isFinite(rangeMax)
                || rangeMax <= rangeMin || !Double.isFinite(padding) || padding < 0 || padding >= 1) {
            throw new IllegalArgumentException("Bands require categories, a range, and padding in [0, 1)");
        }
        this.categories = List.copyOf(categories);
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.padding = padding;
    }

    public double bandwidth() {
        return step() * (1 - padding);
    }

    public double start(int index) {
        return rangeMin + index * step() + step() * padding / 2;
    }

    public double center(int index) {
        return start(index) + bandwidth() / 2;
    }

    public List<String> categories() {
        return categories;
    }

    private double step() {
        return (rangeMax - rangeMin) / categories.size();
    }
}
