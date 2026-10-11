package com.cargorent.vehicle.dto;

import com.cargorent.vehicle.entity.VehicleLocation;

import java.time.LocalDateTime;

public record LocationResponse(Long id, Long vehicleId, double latitude, double longitude, double speedKmph,
                               LocalDateTime recordedAt, String locationUrl) {

    public static LocationResponse from(VehicleLocation l) {
        return new LocationResponse(l.getId(), l.getVehicleId(), l.getLatitude(), l.getLongitude(),
                l.getSpeedKmph(), l.getRecordedAt(),
                "https://www.google.com/maps?q=" + l.getLatitude() + "," + l.getLongitude());
    }
}
