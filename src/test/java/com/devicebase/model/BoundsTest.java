package com.devicebase.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Bounds class.
 */
class BoundsTest {

    @Test
    void constructor_shouldCreateBoundsWithCoordinates() {
        Bounds bounds = new Bounds(10, 20, 100, 200);

        assertEquals(10, bounds.getX1());
        assertEquals(20, bounds.getY1());
        assertEquals(100, bounds.getX2());
        assertEquals(200, bounds.getY2());
    }

    @Test
    void toMap_shouldReturnCorrectMap() {
        Bounds bounds = new Bounds(10, 20, 100, 200);

        Map<String, Integer> map = bounds.toMap();

        assertEquals(10, map.get("x1"));
        assertEquals(20, map.get("y1"));
        assertEquals(100, map.get("x2"));
        assertEquals(200, map.get("y2"));
    }

    @Test
    void fromMap_shouldCreateCorrectBounds() {
        Map<String, Object> map = Map.of("x1", 15, "y1", 25, "x2", 150, "y2", 250);

        Bounds bounds = Bounds.fromMap(map);

        assertEquals(15, bounds.getX1());
        assertEquals(25, bounds.getY1());
        assertEquals(150, bounds.getX2());
        assertEquals(250, bounds.getY2());
    }

    @Test
    void fromMap_withNull_shouldReturnDefaultBounds() {
        Bounds bounds = Bounds.fromMap(null);

        assertEquals(0, bounds.getX1());
        assertEquals(0, bounds.getY1());
        assertEquals(0, bounds.getX2());
        assertEquals(0, bounds.getY2());
    }

    @Test
    void equals_withSameCoordinates_shouldBeEqual() {
        Bounds bounds1 = new Bounds(10, 20, 100, 200);
        Bounds bounds2 = new Bounds(10, 20, 100, 200);

        assertEquals(bounds1, bounds2);
        assertEquals(bounds1.hashCode(), bounds2.hashCode());
    }

    @Test
    void equals_withDifferentCoordinates_shouldNotBeEqual() {
        Bounds bounds1 = new Bounds(10, 20, 100, 200);
        Bounds bounds2 = new Bounds(10, 20, 100, 201);

        assertNotEquals(bounds1, bounds2);
    }

    @Test
    void toString_shouldReturnReadableFormat() {
        Bounds bounds = new Bounds(10, 20, 100, 200);

        String result = bounds.toString();

        assertTrue(result.contains("Bounds"));
        assertTrue(result.contains("x1=10"));
        assertTrue(result.contains("y1=20"));
        assertTrue(result.contains("x2=100"));
        assertTrue(result.contains("y2=200"));
    }
}