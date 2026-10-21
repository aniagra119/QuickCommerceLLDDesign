package com.quickcommerce.model;

import java.time.Instant;
import java.util.Objects;

public class LocationPing {
    private final String partnerId;
    private final Location location;
    private final Instant timestamp;

    public LocationPing(String partnerId, Location location, Instant timestamp) {
        this.partnerId = Objects.requireNonNull(partnerId, "partnerId cannot be null");
        this.location = Objects.requireNonNull(location, "location cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    public String getPartnerId() {
        return partnerId;
    }

    public Location getLocation() {
        return location;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
