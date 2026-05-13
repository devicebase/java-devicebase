package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a 2D coordinate point on the device screen.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Point {

    private final int x;
    private final int y;

    /**
     * Creates a new Point with the specified coordinates.
     *
     * @param x horizontal coordinate (pixels from left)
     * @param y vertical coordinate (pixels from top)
     */
    public Point(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new Point at origin (0, 0).
     */
    public Point() {
        this(0, 0);
    }

    /**
     * Returns the horizontal coordinate.
     *
     * @return x coordinate in pixels from left
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the vertical coordinate.
     *
     * @return y coordinate in pixels from top
     */
    public int getY() {
        return y;
    }

    /**
     * Converts this point to a Map for JSON serialization.
     *
     * @return map containing x and y coordinates
     */
    public Map<String, Integer> toMap() {
        return Map.of("x", x, "y", y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point{x=" + x + ", y=" + y + "}";
    }

    /**
     * Creates a Point from a Map.
     *
     * @param map the map containing x and y values
     * @return a new Point instance
     */
    @SuppressWarnings("unchecked")
    public static Point fromMap(Map<String, ?> map) {
        if (map == null) {
            return new Point(0, 0);
        }
        Object xObj = map.get("x");
        Object yObj = map.get("y");
        int x = xObj instanceof Number ? ((Number) xObj).intValue() : 0;
        int y = yObj instanceof Number ? ((Number) yObj).intValue() : 0;
        return new Point(x, y);
    }
}