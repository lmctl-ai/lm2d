package com.lmctl.lm2d.chart;

public record ChartTheme(String background, String foreground, String muted, String grid, String[] palette) {
    public ChartTheme {
        if (background == null || foreground == null || muted == null || grid == null
                || palette == null || palette.length == 0) {
            throw new IllegalArgumentException("A complete chart theme is required");
        }
        palette = palette.clone();
    }

    public static ChartTheme dark() {
        return new ChartTheme("#0b1726", "#edf5ff", "#91a5bd", "#263b53",
                new String[] {"#61d6c5", "#ffcb6b", "#a99cf5", "#ff7f9f", "#78b9ff", "#9ce07b"});
    }

    @Override
    public String[] palette() {
        return palette.clone();
    }
}
