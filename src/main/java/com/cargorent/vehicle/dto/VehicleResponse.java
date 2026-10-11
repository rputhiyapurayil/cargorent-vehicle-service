package com.cargorent.vehicle.dto;

import com.cargorent.vehicle.entity.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VehicleResponse(Long id, String registrationNumber, String type, String brand, String model,
                              Integer year, Integer seats, String fuelType, String transmission,
                              BigDecimal pricePerDay, String status, String imageUrl, String city,
                              String description, LocalDateTime createdAt) {

    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getRegistrationNumber(), v.getType().name(), v.getBrand(),
                v.getModel(), v.getYear(), v.getSeats(), v.getFuelType(), v.getTransmission(),
                v.getPricePerDay(), v.getStatus().name(), v.getImageUrl(), v.getCity(), v.getDescription(),
                v.getCreatedAt());
    }
}
