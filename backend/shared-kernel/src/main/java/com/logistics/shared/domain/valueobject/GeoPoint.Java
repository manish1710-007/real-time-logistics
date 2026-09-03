package com.logistics.shared.domain.valueobject;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Objects;

public record GeoPoint(
    @Min(-90) @Max(90) double latitude,
    @Min(-180) @Max(180) double longitude
) {
    public GeoPoint {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees.");
        }
        Objects.requireNonNull(latitude);
        Objects.requireNonNull(longitude);
    }

    public double distanceTo(GeoPoint other) {
        final int R = 6371000; // Earth radius in meters
        double lat! = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in meters
    }
}