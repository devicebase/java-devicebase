package cn.devicebase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The account's cloud browser allowance.
 *
 * <p>Counted from the same source as the check creation performs, so the two
 * cannot disagree. Only cloud browsers count against it — browsers attached by
 * hand are not part of the allowance.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CloudBrowserQuota {

    @JsonProperty("limit")
    private final int limit;

    @JsonProperty("used")
    private final int used;

    @JsonProperty("remaining")
    private final int remaining;

    /**
     * Creates a quota reading.
     *
     * @param limit how many cloud browsers the account may have
     * @param used how many it has
     * @param remaining how many may still be created
     */
    @JsonCreator
    public CloudBrowserQuota(
            @JsonProperty("limit") int limit,
            @JsonProperty("used") int used,
            @JsonProperty("remaining") int remaining) {
        this.limit = limit;
        this.used = used;
        this.remaining = remaining;
    }

    /**
     * Returns the account's cloud browser limit.
     *
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Returns how many cloud browsers the account has.
     *
     * @return the number in use
     */
    public int getUsed() {
        return used;
    }

    /**
     * Returns how many may still be created.
     *
     * @return the remaining allowance; never negative
     */
    public int getRemaining() {
        return remaining;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloudBrowserQuota)) {
            return false;
        }
        CloudBrowserQuota other = (CloudBrowserQuota) o;
        return limit == other.limit && used == other.used && remaining == other.remaining;
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, used, remaining);
    }

    @Override
    public String toString() {
        return "CloudBrowserQuota{limit=" + limit + ", used=" + used
                + ", remaining=" + remaining + "}";
    }
}
