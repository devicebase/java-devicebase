package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Request payload for text input operations.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class InputTextRequest {

    private final String text;

    /**
     * Creates a new InputTextRequest.
     *
     * @param text the text string to input into the focused field
     */
    public InputTextRequest(@JsonProperty("text") String text) {
        this.text = text;
    }

    /**
     * Returns the text to input.
     *
     * @return the text string
     */
    public String getText() {
        return text;
    }

    /**
     * Converts this request to a Map for JSON serialization.
     *
     * @return map containing text
     */
    public Map<String, String> toMap() {
        return Map.of("text", text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InputTextRequest that = (InputTextRequest) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "InputTextRequest{text='" + text + "'}";
    }
}