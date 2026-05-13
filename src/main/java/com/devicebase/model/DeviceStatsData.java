package com.devicebase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Device statistics data.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @author Richie
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DeviceStatsData {

    private final int total;
    private final int free;
    private final int busy;
    private final int offline;

    public DeviceStatsData(int total, int free, int busy, int offline) {
        this.total = total;
        this.free = free;
        this.busy = busy;
        this.offline = offline;
    }

    public int getTotal() {
        return total;
    }

    public int getFree() {
        return free;
    }

    public int getBusy() {
        return busy;
    }

    public int getOffline() {
        return offline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceStatsData that = (DeviceStatsData) o;
        return total == that.total && free == that.free && busy == that.busy
            && offline == that.offline;
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, free, busy, offline);
    }

    @Override
    public String toString() {
        return "DeviceStatsData{total=" + total + ", free=" + free + ", busy=" + busy
            + ", offline=" + offline + "}";
    }
}