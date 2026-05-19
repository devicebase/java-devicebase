package cn.devicebase.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Point class.
 */
class PointTest {

    @Test
    void constructor_shouldCreatePointWithCoordinates() {
        Point point = new Point(100, 200);

        assertEquals(100, point.getX());
        assertEquals(200, point.getY());
    }

    @Test
    void defaultConstructor_shouldCreatePointAtOrigin() {
        Point point = new Point();

        assertEquals(0, point.getX());
        assertEquals(0, point.getY());
    }

    @Test
    void toMap_shouldReturnCorrectMap() {
        Point point = new Point(100, 200);

        Map<String, Integer> map = point.toMap();

        assertEquals(100, map.get("x"));
        assertEquals(200, map.get("y"));
    }

    @Test
    void fromMap_shouldCreateCorrectPoint() {
        Map<String, Object> map = Map.of("x", 150, "y", 250);

        Point point = Point.fromMap(map);

        assertEquals(150, point.getX());
        assertEquals(250, point.getY());
    }

    @Test
    void fromMap_withNull_shouldReturnOrigin() {
        Point point = Point.fromMap(null);

        assertEquals(0, point.getX());
        assertEquals(0, point.getY());
    }

    @Test
    void fromMap_withMissingKeys_shouldReturnDefaultValues() {
        Map<String, Object> map = Map.of();

        Point point = Point.fromMap(map);

        assertEquals(0, point.getX());
        assertEquals(0, point.getY());
    }

    @Test
    void equals_withSameCoordinates_shouldBeEqual() {
        Point point1 = new Point(100, 200);
        Point point2 = new Point(100, 200);

        assertEquals(point1, point2);
        assertEquals(point1.hashCode(), point2.hashCode());
    }

    @Test
    void equals_withDifferentCoordinates_shouldNotBeEqual() {
        Point point1 = new Point(100, 200);
        Point point2 = new Point(100, 201);

        assertNotEquals(point1, point2);
    }

    @Test
    void toString_shouldReturnReadableFormat() {
        Point point = new Point(100, 200);

        String result = point.toString();

        assertTrue(result.contains("Point"));
        assertTrue(result.contains("x=100"));
        assertTrue(result.contains("y=200"));
    }
}