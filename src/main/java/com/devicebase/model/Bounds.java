package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a rectangular area or swipe path on the device screen.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Bounds {

    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

    /**
     * Creates a new Bounds with the specified coordinates.
     *
     * @param x1 starting x-coordinate (or left edge)
     * @param y1 starting y-coordinate (or top edge)
     * @param x2 ending x-coordinate (or right edge)
     * @param y2 ending y-coordinate (or bottom edge)
     */
    public Bounds(
            @JsonProperty("x1") int x1,
            @JsonProperty("y1") int y1,
            @JsonProperty("x2") int x2,
            @JsonProperty("y2") int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Returns the starting x-coordinate.
     *
     * @return x1 coordinate
     */
    public int getX1() {
        return x1;
    }

    /**
     * Returns the starting y-coordinate.
     *
     * @return y1 coordinate
     */
    public int getY1() {
        return y1;
    }

    /**
     * Returns the ending x-coordinate.
     *
     * @return x2 coordinate
     */
    public int getX2() {
        return x2;
    }

    /**
     * Returns the ending y-coordinate.
     *
     * @return y2 coordinate
     */
    public int getY2() {
        return y2;
    }

    /**
     * Converts this bounds to a Map for JSON serialization.
     *
     * @return map containing x1, y1, x2, y2 coordinates
     */
    public Map<String, Integer> toMap() {
        return Map.of("x1", x1, "y1", y1, "x2", x2, "y2", y2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Bounds bounds = (Bounds) o;
        return x1 == bounds.x1 && y1 == bounds.y1 && x2 == bounds.x2 && y2 == bounds.y2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, x2, y2);
    }

    @Override
    public String toString() {
        return "Bounds{x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + "}";
    }

    /**
     * Creates a Bounds from a Map.
     *
     * @param map the map containing x1, y1, x2, y2 values
     * @return a new Bounds instance
     */
    @SuppressWarnings("unchecked")
    public static Bounds fromMap(Map<String, ?> map) {
        if (map == null) {
            return new Bounds(0, 0, 0, 0);
        }
        Object x1Obj = map.get("x1");
        Object y1Obj = map.get("y1");
        Object x2Obj = map.get("x2");
        Object y2Obj = map.get("y2");
        int x1 = x1Obj instanceof Number ? ((Number) x1Obj).intValue() : 0;
        int y1 = y1Obj instanceof Number ? ((Number) y1Obj).intValue() : 0;
        int x2 = x2Obj instanceof Number ? ((Number) x2Obj).intValue() : 0;
        int y2 = y2Obj instanceof Number ? ((Number) y2Obj).intValue() : 0;
        return new Bounds(x1, y1, x2, y2);
    }
}