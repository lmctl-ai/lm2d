package com.lmctl.lm2d.chart;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LinearScale {
    private final double domainMin;
    private final double domainMax;
    private final double rangeMin;
    private final double rangeMax;

    public LinearScale(double domainMin, double domainMax, double rangeMin, double rangeMax) {
        if (!Double.isFinite(domainMin) || !Double.isFinite(domainMax) || domainMax <= domainMin
                || !Double.isFinite(rangeMin) || !Double.isFinite(rangeMax) || rangeMax == rangeMin) {
            throw new IllegalArgumentException("Scale domains must be finite and non-empty");
        }
        this.domainMin = domainMin;
        this.domainMax = domainMax;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
    }

    public double map(double value) {
        return rangeMin + (value - domainMin) / (domainMax - domainMin) * (rangeMax - rangeMin);
    }

    public List<Double> ticks(int count) {
        if (count < 2) {
            throw new IllegalArgumentException("At least two ticks are required");
        }
        double step = niceStep((domainMax - domainMin) / (count - 1));
        double value = Math.ceil(domainMin / step) * step;
        List<Double> result = new ArrayList<>();
        while (value <= domainMax + step * 0.000001) {
            result.add(Math.abs(value) < step * 0.000001 ? 0
                    : new BigDecimal(value, new MathContext(12)).doubleValue());
            value += step;
        }
        return result;
    }

    public double domainMin() {
        return domainMin;
    }

    public double domainMax() {
        return domainMax;
    }

    public static String format(double value) {
        String formatted = Math.abs(value) >= 100 ? String.format(Locale.ROOT, "%.0f", value)
                : String.format(Locale.ROOT, "%.2f", value);
        int end = formatted.length();
        while (formatted.indexOf('.') >= 0 && end > 0 && formatted.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && formatted.charAt(end - 1) == '.') {
            end--;
        }
        return formatted.substring(0, end);
    }

    private static double niceStep(double raw) {
        double power = Math.pow(10, Math.floor(Math.log10(raw)));
        double fraction = raw / power;
        double nice = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 5 ? 5 : 10;
        return nice * power;
    }
}
