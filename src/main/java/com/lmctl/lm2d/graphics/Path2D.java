package com.lmctl.lm2d.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Path2D {
    public record Command(String type, double[] values) {
    }

    private final List<Command> commands = new ArrayList<>();

    public Path2D moveTo(double x, double y) {
        return add("M", x, y);
    }

    public Path2D lineTo(double x, double y) {
        return add("L", x, y);
    }

    public Path2D quadraticCurveTo(double controlX, double controlY, double x, double y) {
        return add("Q", controlX, controlY, x, y);
    }

    public Path2D bezierCurveTo(double control1X, double control1Y, double control2X, double control2Y,
            double x, double y) {
        return add("C", control1X, control1Y, control2X, control2Y, x, y);
    }

    public Path2D arc(double centerX, double centerY, double radius, double startAngle, double endAngle,
            boolean counterclockwise) {
        if (!Double.isFinite(radius) || radius < 0) {
            throw new IllegalArgumentException("Arc radius must be finite and non-negative");
        }
        return add("A", centerX, centerY, radius, startAngle, endAngle, counterclockwise ? 1 : 0);
    }

    public Path2D close() {
        return add("Z");
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public String toSvgPathData() {
        StringBuilder result = new StringBuilder();
        for (Command command : commands) {
            double[] v = command.values;
            switch (command.type) {
                case "M", "L" -> append(result, command.type, v[0], v[1]);
                case "Q" -> append(result, "Q", v[0], v[1], v[2], v[3]);
                case "C" -> append(result, "C", v[0], v[1], v[2], v[3], v[4], v[5]);
                case "Z" -> result.append(" Z");
                case "A" -> appendArc(result, v);
                default -> throw new IllegalStateException("Unknown path command: " + command.type);
            }
        }
        return result.toString().trim();
    }

    private Path2D add(String type, double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Path coordinates must be finite");
            }
        }
        commands.add(new Command(type, values));
        return this;
    }

    private static void appendArc(StringBuilder result, double[] v) {
        double start = v[3];
        double delta = v[4] - start;
        boolean counterclockwise = v[5] == 1;
        if (counterclockwise && delta > 0) {
            delta -= Math.PI * 2;
        } else if (!counterclockwise && delta < 0) {
            delta += Math.PI * 2;
        }
        double startX = v[0] + Math.cos(start) * v[2];
        double startY = v[1] + Math.sin(start) * v[2];
        append(result, result.length() == 0 ? "M" : "L", startX, startY);
        int segments = Math.abs(delta) >= Math.PI * 2 - 0.0000001 ? 2 : 1;
        for (int i = 0; i < segments; i++) {
            double from = start + delta * i / segments;
            double to = start + delta * (i + 1) / segments;
            double endX = v[0] + Math.cos(to) * v[2];
            double endY = v[1] + Math.sin(to) * v[2];
            int largeArc = Math.abs(to - from) > Math.PI ? 1 : 0;
            int sweep = counterclockwise ? 0 : 1;
            result.append(" A ").append(number(v[2])).append(' ').append(number(v[2]))
                    .append(" 0 ").append(largeArc).append(' ').append(sweep).append(' ')
                    .append(number(endX)).append(' ').append(number(endY));
        }
    }

    private static void append(StringBuilder result, String command, double... values) {
        result.append(' ').append(command);
        for (double value : values) {
            result.append(' ').append(number(value));
        }
    }

    public static String number(double value) {
        String formatted = String.format(Locale.ROOT, "%.4f", value);
        int end = formatted.length();
        while (end > 0 && formatted.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && formatted.charAt(end - 1) == '.') {
            end--;
        }
        String result = formatted.substring(0, end);
        return result.equals("-0") ? "0" : result;
    }
}
