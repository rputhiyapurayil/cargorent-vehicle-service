package com.cargorent.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One GPS fix for a vehicle (the "GPS Location Tracking" entity in the ER diagram). */
@Entity
@Table(name = "vehicle_locations")
@Getter
@Setter
@NoArgsConstructor
public class VehicleLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "speed_kmph")
    private double speedKmph;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public VehicleLocation(Long vehicleId, double latitude, double longitude, double speedKmph) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmph = speedKmph;
        this.recordedAt = LocalDateTime.now();
    }
}
