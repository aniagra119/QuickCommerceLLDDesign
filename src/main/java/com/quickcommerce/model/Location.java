package com.quickcommerce.model;

import java.util.Objects;

public class Location {
    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Calculates great-circle distance in kilometers using the Haversine formula.
     */
    public double distanceTo(Location other) {
        Objects.requireNonNull(other, "other location cannot be null");
        final double EARTH_RADIUS_KM = 6371.0;

        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round((EARTH_RADIUS_KM * c) * 1000.0) / 1000.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location location)) return false;
        return Double.compare(latitude, location.latitude) == 0 && Double.compare(longitude, location.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
