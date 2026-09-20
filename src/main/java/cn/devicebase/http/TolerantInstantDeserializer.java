package cn.devicebase.http;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Reads an {@link Instant} from any of the timestamp shapes the API emits.
 *
 * <p>Jackson's default {@code Instant} deserializer requires an ISO-8601 value
 * with a zone, but the API sends a naive local time for some fields — a device
 * row's {@code updated_at} is {@code 2026-09-20T15:11:31}, and envelope
 * timestamps are {@code 2026-09-20 15:11:31}. Either would fail the whole
 * response over a display field, so both are accepted and read as the local
 * zone.</p>
 *
 * @author Richie
 */
class TolerantInstantDeserializer extends JsonDeserializer<Instant> {

    /** Shapes without a zone offset, tried in order after the ISO-8601 forms. */
    private static final DateTimeFormatter[] LOCAL_FORMATS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ISO_LOCAL_DATE,
    };

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String text = parser.getValueAsString();
        if (text == null || text.isBlank() || "null".equals(text)) {
            return null;
        }
        String trimmed = text.trim();

        try {
            return Instant.parse(trimmed);
        } catch (DateTimeParseException ignored) {
            // Fall through to the zone-less shapes.
        }

        for (DateTimeFormatter format : LOCAL_FORMATS) {
            try {
                return LocalDateTime.parse(trimmed, format)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
            } catch (DateTimeParseException ignored) {
                // Try the next shape.
            }
        }
        throw new IOException("unrecognized timestamp: " + trimmed);
    }
}
